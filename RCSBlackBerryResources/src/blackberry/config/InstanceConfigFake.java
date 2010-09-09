//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //1308
    static String configString = "0000000000000000bcb10b1cbb298dc05645e0a55bce318fa8044295dbee73bb09430596c74bac8ae27697927be58660bfac3cc9c82af4c45197159c73e64af737067b9dc3b62b3bf1392123f097056740dc2b4028668a7f44e82ad68947a4d128e8d2fbfab5f30e4065496b386056995bb8a84c9abc35b3ca630f3a6e7b604355dd878e90cc8d193e7b2da745e96aab2c334f2479b8cd5b04abc00d33f2cc7e2dc0a4f8616a7f4e0d134c544717f8f07fa226e9e769c3653ec88c81a693d42f25f6613da6c60f490adc9d3858134b4d062a3da5b5b165a18f03d6fa35927ecf32076e5bca23dcd71607e9662ee0dce11a3fb40e96bcdc08fabe6c5bb640975ff4b1458e43f62bb12dbc7d5b70ce0e3c9025eb8d3928278a46361f384fd23efcfc5739b38d16aee49da4b7500b4addb706908d9ced9293e2c72fecf29660199be6b5ef1ebaab3adebcaa8aa0447737fdec1d6dade43137412697f0aecf652fd1ede6961381a3dfbbc4b1082336fa5188e798c375028ffdd35362cf7a1fce861afbfb3a6519080fdd052d8f53a8c336c6a193d8d4edd446a167089552b6f69d4fff2f9d8e6421344040f95ffd9818ea908c44a488e20c9c1e64dd2a9d7364e33f883250310a218fe9fa2ebf71a87b05cfe7749000472300f6c5a8e08e6b8a20e3af3deb96895a870e40b89e7e5822af1ec65e85f9886c7b4b48f031bdcdca11e5abc7fe557bc62596f2cf651c71aaff7c10656ee34ec1ab566b5d666211ceeecc33bca38ffd8d90fcbc300f73675dee696394ead89850d22e23ecdb1ab9f686cbe89a5952b49cb360f1357d3f12d672332dac0c0c3f21d37b33c044da36891c19f5b12e965d795d28f1b4be0a7f857717b05c8d287a62dddb";
    static byte[] config;
    
    public int version = 2;

    public static byte[] getBytes() {        
        config = hexStringToByteArray(configString);        
        return config;
    }
    
    /**
     * Hex string to byte array.
     * 
     * @param wchar
     *            the wchar
     * @return the byte[]
     */
    public static byte[] hexStringToByteArray(final String config) {
        int offset = 4;
        int len = config.length() / 2;
        final byte[] ret = new byte[ len + 4];

        for (int i = offset; i < ret.length; i++) {
            final char first = config.charAt((i - offset) * 2);
            final char second = config.charAt((i - offset) * 2 + 1);

            int value = NumberUtilities.hexDigitToInt(first) << 4;
            value += NumberUtilities.hexDigitToInt(second);

            ret[i] = (byte) value;
        }
        
        final DataBuffer databuffer = new DataBuffer(ret, 0, 4, false);
        databuffer.writeInt(len);

        return ret;
    }
    
    //#else
    public static byte[] getBytes() {        
        return null;
    }
  //#endif
}

