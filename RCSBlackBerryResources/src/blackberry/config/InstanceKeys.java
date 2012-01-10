//#preprocess
package blackberry.config;

import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.util.Arrays;

public class InstanceKeys extends KeysGetter {

    private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private static String log = "3j9WmmDgBqyU270FTid3719g64bP4s52";
    private static String buildID = "av3pVck1gb4eR2"; // MD5: 7f9e6a0ed9965458d8c1f1a558713e9d
    private static String proto = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";
    private static String demo = "hxVtdxJ/Z8LvK3ULSnKRUmLE"; //MD5: baba73e67e39db5d94f3c67a58d52c52
        
    private static byte[]  buildDigest = new byte[]{ (byte) 0x7f, (byte) 0x9e, (byte) 0x6a, (byte) 0xe, (byte) 0xd9, (byte) 0x96, (byte) 0x54, (byte) 0x58, (byte) 0xd8, (byte) 0xc1, (byte) 0xf1, (byte) 0xa5, (byte) 0x58, (byte) 0x71, (byte) 0x3e, (byte) 0x9d };
    private static byte[]  demoDigest = new byte[]{ (byte) 0xba, (byte) 0xba, (byte) 0x73, (byte) 0xe6, (byte) 0x7e, (byte) 0x39, (byte) 0xdb, (byte) 0x5d, (byte) 0x94, (byte) 0xf3, (byte) 0xc6, (byte) 0x7a, (byte) 0x58, (byte) 0xd5, (byte) 0x2c, (byte) 0x52};

    private static byte[] byteLogKey;
    private static byte[] byteProtoKey;
    private static byte[] byteConfKey;
    private static byte[] byteInstanceID;

    public InstanceKeys() {
    }

    /**
     * Checks for been binary patched.
     * 
     * @return true, if successful
     */
    public boolean hasBeenBinaryPatched() {
        //boolean ret = !buildID.startsWith("av3pVck1gb4eR");
        MD5Digest digest = new MD5Digest();
        digest.update(buildID.getBytes());
        boolean ret = !Arrays.equals(digest.getDigest(), buildDigest);

        //#ifdef DEBUG
        log += " buildID: " + buildID;
        //#endif
        return ret;
    }
    
    /**
     * Checks if demo.
     * 
     * @return true, if successful
     */
    public boolean isDemo() {
        MD5Digest digest = new MD5Digest();
        digest.update(demo.getBytes());
        boolean ret = Arrays.equals(digest.getDigest(), demoDigest);

        //#ifdef DEBUG
        log += " demo: " + demo;
        //#endif
        return ret;
    }

    /**
     * Gets the aes key.
     * 
     * @return the aes key
     */
    public byte[] getLogKey() {
        if (byteLogKey == null) {
            byteLogKey = keyFromString(log);
            //#ifdef DEBUG
            log += " log: " + log;
            //#endif
        }
        return byteLogKey;
    }

    /**
     * Gets the builds the id.
     * 
     * @return the builds the id
     */
    public byte[] getBuildID() {
        return buildID.getBytes();
    }

    /**
     * Gets the challenge key.
     * 
     * @return the challenge key
     */
    public byte[] getProtoKey() {
        if (byteProtoKey == null) {
            byteProtoKey = keyFromString(proto);
            //#ifdef DEBUG
            log += " challenge: " + proto;
            //#endif
        }

        return byteProtoKey;
    }

    /**
     * Gets the conf key.
     * 
     * @return the conf key
     */
    public byte[] getConfKey() {
        if (byteConfKey == null) {
            byteConfKey = keyFromString(conf);
            //#ifdef DEBUG
            log += " conf: " + conf;
            //#endif
        }

        return byteConfKey;
    }

 
}
