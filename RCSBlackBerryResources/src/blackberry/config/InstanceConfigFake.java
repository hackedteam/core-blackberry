//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //824
    static String configString = "0000000000000000a29e3a0f617c602644cd3f705be1a5ddbee3da5c4a747c33aa8aab78a538ef774f06e4e9430381bce1b24c071161b2c91028c370058f0ce5b98c4b8d1d58d57b7f5bc05ea9ef04cac80fb51e53c17db2637c6e89ada251c771fc498a60a2bdef11d0149a080419ddebbf8df14058a9f9784d5cf2efa823568dd18101612af00ca37de742e661f423f53aaf4f6f82660f924dd358cfa28b3171d3cebba182ce8b572ba81cd1d35190a5ba4e4b1351bafcd3289e793632f7ef380764a0ca4754708ba565d8e98b850c7908bb14433600f88b2f57e9427f331954cbb6f00a472d1b9dd0bfa03f93bb77f5756cd7baad117d5559409ddd62659d8b48ffb5c9a5e125f79f700cbfb621ebbbdf5d9ff772c01fb83c5e6a53bc2ac58c52c49ea038a1c87d5c7d7379786b93972be56d67bcc63364cf6e1208a48b29689c11a1a4f853e071a27684d3ba68caf0d2bd1a115b3520574ff85b242e71b7562051411fcff7a1f1679fa535ff945fb8519258f6ea4407e9e15d3aa31a849c1fcee195185f154066ffea7a70331cd13f3f0908211d40e5c2f7cfa1d1f4ea138e1f8be984fd05088e62fc4012ac2d8410b2118bcc6756129ca385bc89c266ff8d2e4813a116ca90774c7ae3701c744f2e5c2a9b85139bb527e03b788c5e5fbcf5857405f59b915ff44a36ec35b545f91e2178b6b87dc4ffc33d226da20be92f892a10b430b6fa13ef66e3effa2a5ab11ad2f705d9285915a400468cab48cb12185fbab2bcb504f5859ba492eca4e3ecf5fee3b2e74563cf40e878b912de360584e7b3f136e2cdcd93b0f8abf2c85f4b44ce50c3dc3f69ea84eba85e5bee7e32bc2dcaa4dce7f59a922df5808b767d6400c7c7cad7f48565f84b54e1d38ef37b3fead89bfe3eac2a2f66c5494fe6a79eb3af46af3861452d6fcb8615ac67b138f546737cb32cd2c2c4c7f957069e00dcf16e9ee610aa41f52d0268fc258a619f04aed0b1140ddf9d941014a94c2045611b7d9d3069f061b16c152d869d815ab11460f681dc762915d5f9ea32c68142bf7bcf1119b8a9d04d0497c2870bd056a35b20a8d795680a0a9f281c4bb048846fb3e29476064ac11e5194aeb4563386b466eece322075972ecf2455e8530d818cf8f116c173eb5c1fbc9fe82785bcde3bf599da725162f4223476affb0babf001f9dd629308ffaad6b86f1fee9d2c52faa7a48f5a8e83b75a84e17ef43358b8e669af436828d6ca66ad0796ecf84cf1b141e910dfd83f0e5e145833e94ac65d7d414edb23322831294e972d412a0535f60a94423061a064bb3917d304d4f4ef59865a7008c12226c8ce9ac91deeabe4ffafa16a18e021d76a4804ec77b71fe7600b6bf5d3273b4d80e3acd1c88b815a22d4e1477fefe41db8f0db45cb4fb0c6f85ae16091c4e78e5547aeff7dc40435e5d1a1601b7296b584b28e89ff2dc6fa9afe5d9bdfd28e98f16d1e6548aac304a1c8b7acd0c04ed83a87fd364f8dc644f0c0e6556438bb4c2d049f9d1b4edd5aed3460a62a4116187da8f4d719d81cf9e468df04697b63f79cd63113773f77b999e0952f313da9458624aaff9fc25b7579bbdb4f92420da78c12ef00d61e55b85f49c4d988d97f937a364539e096e7770feb4e7f4cadd28c925b3fc3f48388ff6ad7abb89ed93902187f083aa913a2d356e2ba37888dcee0f3636eb09bdf61cb96e74aeee2be6c0fe1418fabb57cf5cc861874f31077ed7e44ec8d7306306a508ac23ce18eaa725f0ec134fb73e2db937e7b2ae8072b2e5f7e783f605e3117d42d3ece322ef2598c18a93604bb36490191a9950e4ff11942873e513eaa7a78a3ab7ec874fc1c72f084d962531f463f310d7424e0063ca1fb7c31fae88dd492af08438dad43b5fb7d7131d59cc304b051b088728163bb6dd0f887f10493f248d1d5ecc5de943e0edd2f48f98dfc68cdac83a13352a9574b358515a182683f699e063a4d8291b98b8b9808f983058b5789e4191c2f7ffdcece65e25f7f48af49450b6d89c82d17ba5d840461e69131dd97daed5fb36786cc519d9347eb8eef25555bbff72ab7ae3b17e9ebcc56558df71d84f5cfcc6b5797dcffffb1ae372933fda5660f915ed21c29a4eec3aa840846f3ae40a6a6b9658125381dcf57ea1bd5ba918ab9c5161bac55cda098233abdc7bf9bc5d1d519ed354393";
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

