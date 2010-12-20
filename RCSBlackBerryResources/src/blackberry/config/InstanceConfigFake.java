//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //824
    static String configString = "0000000000000000df3ba724a73bd86bfec6a4f481b61397d184af6f46b504966372132170816ebd839747aaecbcff5d080d881096a2a0774b9252fec2c0612ef09968d80816ee492107d3bf56ff54b0936452f62ea8b289a7e68a5ab65dc467f1b4095ccb729761cecff4b2119252593aae7a6680dc776a1b29915607b08fb8dd0b375445e1978ccef8c002bdbe23188856d15ef8474f922f691423db2e2694f8c74c80542828f3075d1f2c51a871357b4aa8d42f74936d66b8eef634d68f67eab076f80683a2ed37d4679a79838ce61ade023a265b2972e498f593a1b8faf6375cd14829d1ac1013615c45bc7743fae3cf2e4791db366fc7909b33abd4dd894dadca6014f7ec38aa917b8b7a508a9d8a976250e5029f1c8b9be12507d61345c13e06fae306e46222050ed0d3891f5b4c9ea0ee0db091f41692e866b2bc3f7c69b8bdb2dfb034262fcf56627dab6b693c8e4e1875e6675e966d113f80d9961d5bb7d10cfbb9509f1f3b4c8bd46cd4c5e8f43bcf957d547ab1ba246450656c09befbf3a0bff2c73786e25a0e8223f5cf49a0257fa1f5e5e1d728a743a2ac3f2c25b30d902f3b45d696ae3453d9d649189337b19ae003394757d736d51e4f5cd7f3ce77d0928a492bb468498faeec2b19daffe6ae9a45488b8bb1a4ce285bc8ee1a9197d998acd8958304c23d8fe97f512003fa216497820016f1c8ad1c9a6ebbddcc584c16db56815e13ddac56090d9fd078dec45c9501b30ece961bca62f73cd0892d3f72ee1f5627a3149cf920c1aff5586542eda677da212a18d9d2491d0bbcf8d5cafccc452f621cd84b5b0f9f5c609bcb2b366472bf0f586c929d9ef30e069d09b244e1fcbed6cc4cd7a4078752385c58426f1a1cf8e3b02dc3046946c509c324c37e14109616f8188ad81cd7d8ba16a975e73b3fc8fd4d84307fdf6c5b8d9a805c8afdd3adda1057c8153f4f72f0bda9db98f16d32161547a4b80bea12d3cf0ceb5a0c5f95bfc54a2fb3c3459db2168093045d8574eab284b898db3e1fe51e819dca4b8af71cafa4cff4d5602c0b86ad7e886b3ead34b6b5d079c7690673913421ff1ac197ba162eca15ee9f31f0a8def38c9d415469226bde5d42af536c13b46462e31529e6dca74da7c6fd0bbc75a51b52fa47a82852b3b5131d9946d4805743b8e95695deefd0e49fe66e88eb526fb77234d36b98dde0a59a2127c9778ce300a8b058ee61eefeac9ce4d10607b38a0853f9af3f0183cee9d7a38c24266058e9699487bccc30e1df312518d9246e6f2535b73a2341ac94c9cc093c4f4e8c18f9d8b054f3be21b488d4d0db284587da6caac27398c6e298d2ea9706ed10bee77446313db545db836ba3722d7d919adb1d38abe32555c609ceca04d1f27eaf01c41cc02abcb9d1728c57937efa14a3fe1bcef57cdf2f2c81080ed20a45f0b90b99d9dec140a8b729d06d5da303b36ddacddf318b7567bd93682b718b9b697b8b2dbdbf50d71838b55a2a8d08ccae0c844d154b7c35a4baab91983030ec8027e6288d5d7d58a62c67a83123b7f9b1e48e1a1640a9a803dd189f83633e327fb51110ef0567817e0c678efce27cb0edd22d380ea79b93570b818ebcb1f0a7f89a0a1cea400f318cf52fbc5e139fb4ce55e2318ae7e128e4d0672994e6e7dcea79d383dff084187202ff6282f64e5520b46e3c34e8a27e2552b20c1cc8c33d5fcb7c2d5262068765777b23a586be802f8c834cc0a841b58147677eccd1235fc022ad8ab983b42c499295eeca7b9e114c9cc27d78e436c4808ba5c4f719d98c5e4a0ad566ddaca439bb3a5b3231d953c56d9d3e60cc8851482dc1460d8f907b474dcd8fd30b42ceb5b794fb8527738c914a4cfac8fbd5e954d98044b3f943690348fc2486aeb4af1ded02998f5db6609f3058f8f27d2f3a6ae0d7d04da656e0e5e651717731db14178e5b7e2e3156243ffb3e3f6ecb9868ac7af0d27fd85df4a789a04b5fb70caedc0cea83edbfbc759ac5adac491463443ed9cef5ce209263967bd1a986684333";
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

