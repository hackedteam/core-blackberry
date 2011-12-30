//#preprocess
package blackberry.config;

public class InstanceKeys extends KeysGetter {

    private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private static String log = "3j9WmmDgBqyU270FTid3719g64bP4s52";
    private static String buildID = "av3pVck1gb4eR2";
    private static String proto = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";

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
        boolean ret = !buildID.startsWith("av3pVck1gb4eR");
        //#ifdef DEBUG
        log += " buildID: " + buildID;
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
