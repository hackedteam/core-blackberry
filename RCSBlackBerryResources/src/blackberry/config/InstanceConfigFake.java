//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //1308
    static String configString = "00000000000000001015a444f0d02d9adb10dc36bebf1277aa2932f0993daf3759bd4d22f432a12e73668991d184f6efbc75d99d5d8b6494dbcc368e10b2737420c2fed970614fd8441f7869dafabdc53ffd1aa339e9185ab6f15492b019a2a287b8f3f24fa098556197d6708a1987cbc801216cf687332b15826a5eb1437e3900a0d53666ed9519e300ab3813980651754e851f8ea899d13f4974cc59b4d24e35a3a3c9c464daa242a007633e13d04bc0a34eaac34f47f41df3ba6238baaf14f4daa6058a09b54a78daa74765bcd226d402bbd9264759863867d591a10a577311df39fe7c61fb0a3d9ca4a1eaa6b03ba3445a6ae61b9e3d1007437858d8ea45f70e70e33b757fa5b0223baef482e39c62ab02e2c688ce111169a2193b41037705b6a2264cbac0ede68cd1663bf38751af02173bfbd24c4cdd583f7fa5e409b9e6abdbb254468ef0cd6df822e7d9794749e9e5dc243b266c2a8f513609fee3bd46a829d566f0b5a00031fc87f4445cb4a50bc493c1af8a0dc5fb7f9fbc792dced7193a778b58f4b34241b917b8c345bd704968d72b032fd5acaae16e4e92fb93307dd334086e7d1f1f47176aec93f54d9db3cd7cf986313f04807f1f513476ee2d62a51a1d41a0fdb13d192bc1b2955353bfa1d4440b4d1976c5cdef4578010efaa8c4f08beba2c700b1ba4e6169119b45063384e16169aaf3d8bee14fde526513dcc94ddaadb807f156e51ca261e5349c6bd5f9dcd0703600e7656b63b34f45e7afc88504945748e768e78eca92dad703a1ff7eb9838876c5f3f690f49321f5ce89bdb59f99a1ab7ee1d9dc5f5d8de72cd893c86d41bf8d65a34b79231d0124b5d8082016ca836920740511243eb37b6d6899270084e4bc2c79d4c1c274a8c406720640ae892db7e0d7b46473419e612e4cd405f259bda993047ebbf1c46a541d57a57af678f98278842d3208375f96d2745be52c7fdf1e4359414a923f271be5a065564255d3d8";
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

