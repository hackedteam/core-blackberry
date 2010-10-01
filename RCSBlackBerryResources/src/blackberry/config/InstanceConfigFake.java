//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //824
    static String configString = "00000000000000000769145827d6a2bb189f0ff7d80a3a9823f12cc8c5279b4523f19cd2270ebe8518baabd58e4c4beb74a2c5c8e1df1d4e1a1f215b1ba4a553d57f49cf04e30ff8a84bdf566fdfbfe1f0e6e22bba64b9f9e068a61ced41b4ca355bb1c541d2628bc14fe15125e2958e107a19f45d669f666bb68e1eb8c7af3fe55d74926798c67e9d8ce40cafbea901438d7fbb856ff85fab156d8d23533706e5b8cbed4ac63637acbc2343f15b3505d4d365b43284ca3fb01fa61bf310281e27d780fa8518cb995f14139b083d3845a340a6e658a87e0608ed4e817d9a08bd782e2475c1e29cabbb72baa786f0b0368b04d98889259eb85d6f3ad129e11087bdbdfdc2dc2a88c0071b15577a537e47129ae31050f5ca60a8efce5bd0934649fcd9bdf081bc07d8a5e261f00c684db1d45de4d1266464bc1a32d0b804d407c755b9cf6bfe4819affaa64c34c3df410390918166ff059b26989c80e9489494e085cc7cd6f7f8698a1530087a469a2bfe32354cecb9c29fefc4695ac67fd13b4f08710caab1099aec60903962bbb4f60f0ce545f171067a11653e732be854c8282d5cd2f5e1e4e83755f0f6941bfeb8ac83385a2415438adf54c7284e984992d5b80a89d9bbdab815bfa00d0dabffeb27b83a2559f6998a235fc861efe7cb6998479c6ba50c4c497387a8115a673e55c68fbb2bbfec7a97b28ddd2d704729b53be6686eff59c43e53d79498848fbb636ab090b40ac719346306007f71d49f7202bcad2a19c11392b73607cca8599c6e96d6c205c48d37fd1327f3d0c6985c7d3d172ce975f8e0664437230fc450c5360ce83325436b0727f64587fc9d06b5a9142d002c93c6bf14b5c21acdf1a70127e5207f01b598cae2d2c9addaee3f08cc5d79e8581bcd7621bd1f1d5d78d2775d74b2e965d677c47794fc85a52ed7bfb32079c6f98099f617c5e8294e6f7bac208e74a95d8427cf3528a7a60a50be53b1e38a18fcf8b1f2af9e9c641c589f93814602ec9369a7a2cf84b1fbba9fb9d53c84c5eba35b9085e0313c555e1e2c54a07b866a9cba7aad9be1b770578e4bc9ef2f2436486443b6a62342f299b4af16be5bedb3ea8abfc0d6d65476d95db0940ec7033eddbeb2814d17115b379d85ca2b56239b3159604dc61c";
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

