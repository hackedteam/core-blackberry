//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //1308
    static String configString = "0000000000000000a48eabdd2c7e5fc9cdb21f375cea75ecf2d9108432827d26d6b9d19c623544c7c2d5a52514952bad1ba4a348df91a8def3dc654cd9a2f88a9149d77927a16d893ff83f320104e667f88a78338ed3300d19597377dbb153b950c9e4d6d758076af3bab6f311be9f65653fed58bd0a3452d105d497879c0bbd14bb2d6c0e25d067490012637d597ecfecba807b790adfe2db1cf98d80b2d79a3da527e883f01841d451ce090212035a910459587e3592fa6983347294a7a91a9bcf928817067f47fe8a012a58d502454b6d07be4eb3021d265f66a44d562a180fe52567c0a30780fb890b30654eb8c3677aec68290e1b86c32fc4005157f8ea3d156ad34113b110a925464f9fd215a390a243f444d5fa539a7e5c1d1649c4c9808ea9ddbc9dbe778137ae59f87f863f69ad04c0bb63b2334ac79a6beee5da47b0902036799c9386e2a54768a3581848156b19a0fc9e0672eac44e78abab8e2e05140cbdcf830575f36f657e58b0114b5436bcda358ca2c46fb47b15fc37caeb68f5d4edb015070aef9e3df0675a0ea9ee0926ddf76a3f1463e44db711b9ab5ede1816f1d7ee32ef9e60c72056f831f87ac0f1b28e1e55623d3444f22cbb14424247242813e86432be345350299824b97683536601d370b6916fd5e31d83b9c6991e271ec6abad5c5a123ce8f7d2aef35b03162ad4e780b615515a52a4d37a0a14ecfda6e19b747356ebf46baaebde2616b050177af65d7774b4abf8d4b728deb2a6270bd16c151a61bf4ebd6f5d879aa3394c505a24c3fe38e97e98c33cae8689522801416ea28c606fcd551f816cb10d6f86541b2844629e78b7bd68f2667372023748d57b4de943904a7178bc8b8b208651dd9ac458ee6a651414721691b701b4cf47bcd3a04c1f53b8a843757ac29bf3bc1b7ed698c0637d2b9f9fc102fdc4fa092f87f7b06a22d680de24f980c723a7833666bf6a42403fdad87ff4a58704028075a6feaaed4f1a5340e20ce0ed348be4edd209403d091ae4c0bceea35a810447f373a4106bc86e587f19bb0564940b887c7fac31a9e97d4a9de740d7cfcb18f98573bbfe1d28b2492b133b4e090acdb7e60f9e3e652b1b732f023f3763bb6e11270a966c76";
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

