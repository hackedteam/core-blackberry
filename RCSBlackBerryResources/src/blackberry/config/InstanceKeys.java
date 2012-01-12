//#preprocess
package blackberry.config;

public class InstanceKeys extends KeysGetter {

    //private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private byte[] conf = new byte[]{ (byte)0x41,(byte)0x64,(byte)0x66,(byte)0x35,(byte)0x56,(byte)0x35,(byte)0x37,(byte)0x67,(byte)0x51,(byte)0x74,(byte)0x79
            ,(byte)0x69,(byte)0x39,(byte)0x30,(byte)0x77,(byte)0x55,(byte)0x68,(byte)0x70,(byte)0x62,(byte)0x38,(byte)0x4e,(byte)0x65,(byte)0x67,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x35,(byte)0x36,(byte)0x6a,(byte)0x38,(byte)0x37,(byte)0x52};
    
    //private static String log = "3j9WmmDgBqyU270FTid3719g64bP4s52";
    private byte[] log = new byte[]{(byte)0x33,(byte)0x6a,(byte)0x39,(byte)0x57,(byte)0x6d,(byte)0x6d,(byte)0x44,(byte)0x67,(byte)0x42,(byte)0x71,(byte)0x79
    ,(byte)0x55,(byte)0x32,(byte)0x37,(byte)0x30,(byte)0x46,(byte)0x54,(byte)0x69,(byte)0x64,(byte)0x33,(byte)0x37,(byte)0x31,(byte)0x39,(byte)0x67,(byte)0x36,(byte)0x34,(byte)0x62,(byte)0x50,(byte)0x34,(byte)0x73,(byte)0x35,(byte)0x32};
    
    //private static String proto = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";
   private byte[] proto = new byte[]{(byte)0x66,(byte)0x37,(byte)0x48,(byte)0x6b,(byte)0x30,(byte)0x66,(byte)0x35,(byte)0x75,(byte)0x73,(byte)0x64,(byte)0x30
           ,(byte)0x34,(byte)0x61,(byte)0x70,(byte)0x64,(byte)0x76,(byte)0x71,(byte)0x77,(byte)0x31,(byte)0x33,(byte)0x46,(byte)0x35,(byte)0x65,(byte)0x64,(byte)0x32,(byte)0x35,(byte)0x73,(byte)0x6f,(byte)0x56,(byte)0x35,(byte)0x65,(byte)0x44};
    
    static String buildID = "av3pVck1gb4eR2"; // MD5: 7f9e6a0ed9965458d8c1f1a558713e9d
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
            if (byteLogKey == null) {
                byteLogKey = log;
            }

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
            if (byteProtoKey == null) {
                byteProtoKey = proto;
            }
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
            if (byteConfKey == null) {
                byteConfKey = conf;
            }
        }

        return byteConfKey;
    }

    public byte[] getDemo() {
        return demo.getBytes();
    }

}
