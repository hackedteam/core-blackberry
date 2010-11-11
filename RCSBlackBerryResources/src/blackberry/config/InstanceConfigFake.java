//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //824
    static String configString = "0000000000000000e03b513613a27e6546e7f4c1e9f44dfb9838664b843ebeb9f29b4f9fd4650983a2d10a6e4925f607f3603c4a33a09336a4e5dc23cd5a6957720a390f318c104469c7b110c26c6ba842832a5fd3270806b46ecf387b2a234d76db85286774a15d7729e0d5ddb1054e564f5c955510e6d485e76970c02af7013e41676c3e0e87de65f6e40cfc8023722cea368d86fc2c410611410d515b6febb912ab1a048536b9039cf934a08b7b3dc90dd23fc31254e1757764d75db883c9fe49f45bf759fa0cf51a2cc411c75e3e5edd6828b96a1988e7e2a9df620e3c1d2c0938c62919a3e6ed477252aada9bfa7483a959611eebf8d2b65f85d3739af6e3849776cd851ae076e15d7e28699440aa72628f6861bf7f453912c8230ead7d2a56b0242ea9bcb206b1b46ce675335696bc91324a8a085860fb6a2118bfc7e82ff7fa1f0239e68d563419df4cb7bc87afea6fa57ebc10e6c9f353608ac95f02a5210fa258bc43a81a80cce70df81abb97064084a39eedc3939463d53d95d836e04e95994d0ab61a88c6c957dcfe5f92cf3d7fe1413c8a140f8a1821db1ec22373e824e19bf0b31152bae879c5fd866fb1f6f7b89927597738435ad8e8852d4012503d5a8641f205c46044421d83ba6def051530de508e5b775985f525dd09f7f2371f79b4085798e7c75624764ef8c759612454c378cfbe21972a78c2b7f2e1160d8b2f9ab4c65ddca432479134272f48009b31fe54c286dedbc9f0a51e06b64f26f65dd379476069a3cf6b65c905599e239ca513585c7952fc54dc297674285b43ca0344b9c9d3870dad460bd26c3fed2e2f1e1100e4c37a8dded5789831471cf3da8d1cc315d245ad2969b7424d1d88e60f73492c44236b0cfa05ac3fe686c850a3fb67ec4eb43e9650dabe0baf11406ca5e15f3d6eccbdebfc12bd437a6942d0b7483895997b5a8d608c4b8c1058bb1d951468d6d7716cc365f24089046f77305a3492520f8a0e2ae63aba62e4de03e4aa0af8c566a0925d80bff37ba42e3d708abe0f3daa6a3703da47b63db58eccbe37991c67f2bd73aa62cfe37e25d5e09cc55e0992eb853118a55a103d04ab1c583aaa70b576916a87ff3472dad9c678b16730535bd799a4e4c8ee9b3b60135ac0fc4070cc3e088df850922417728e4c97c2cf93c22a047669a8aea8fbc865f00c4fcce73de4829e7b70c2ec1ff5502d0ae4aa91280888fce171f33024f2f214e6dea0727aa3384d98a05353212f3f6e1c982bbae9397aafdf376958e51b4d06d691f036d6fde2990a556dd9987e1a7158b460d11e9e10fa262a2be5c563a4f843d055459f49009d78ab0c29d611f2de90476ec16b3419eb6e597de03c2bc3865f5162031ecf07c6c6c3e754c96b73cff598b406908f55800f9fb09f0d2c580902f55216679e2ba3282317777c4261c2adbfdd6f0783497c8e7b6681994c7ceefeea3fc22ca14762c6fa9b71bfc08d54ef3669df4980ab44a5dddb0a7c9af9c017ee14edbef0b5b0f388e4707be450f2742f6866a16f2d77ffd068e253b6979b230777382fa37ba9b6be2b7a966f901c3c290721e92e36f6f6ac0d5998b18d1d11d4a72f442a49a8232f9b0e65817c2752dfea3b001c73a5836f7da33e6fd4e961eb3c7c5d471a2512fe8b352955a08f9013ebcfc226cbec580cafddcbd4ffe8d8156113fa9ba63b4c8efd0671157c688a5df89297b4d0915d3e9ab6ac00d299ab1c72aa974f78a34905966413d87fad3a0875bc8168f75272fd02425f78fae6552c287ca9b6c788cd146ab62e4192c0ff508b1ff648702b1361bbb4bb75bfb3c4d18348457b7efb4346b5cf8a474cec611f3a78ba35ada3874de289c3be5ae702ec84d955bac6bf91fb13f37a6c808df964e8a19e12306d04e28334f154a732b6be1b7f0fd046603b24139253a95f62fcc8dbc78164913cec242ea2774a27b851f9ff58fd17621b5dd5eb64429da2d173b988a490e051fb00db7678e375385387007045b3af040f281defcc9d0bf9baa457204ba732d5719473909f676fcdb144b2c9368da951b3011b4871d495a4cc3dc4a54f5ba5a2db97a53eb61483b9775c12feff22b2c3cf9d9abfc671bc5364a6e62c4c8bdfde";
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

