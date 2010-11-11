//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //824
    static String configString = "000000000000000032d449bf6e0b9924b7392403940c9d8148f0cf0b67d12fd50232f39adcca041c15859ba6f2aaf8c5e7ec60d71618253a628839162b2bfea776b9b2ca1716ca55d8867f77c3662e9678a11e484495a54296cc7937ce1a13d36d83119da429c522d041c9f0c410aaad1eb027f47a13c77ccb6c4f3e01ab94bfd0f4264f3fc8fdbf4f0c3787505fac2026d0f0acedb88ab1e30f7e76bdd2265ff0f8d8cc2493a54f1c6642dbeaa97a9b7d39303dc1d6917962caa6e4c54f8d6ac874a5b4e573767200d10e7f893a66d66831c58f6db78360628334dca727fc516f6623cee3a6e3a1ae8dd9cb94b15f1918a3b5dd8663fe288a2a4414b01c02cdf1e5507c6ac1635e01281ec55b5894f46fb2ccc9aa66f2246ff452992e5a3b275927eb4b0c2328abfd64961ec478b1f23f1a3bce2f3a2e6ad35ccea158820a402a79e64faebab0550e501e93e7a265dded5bfb753800df6436336767fe69b98762f227053d9b766c8f11952ca7372f1c3069252ce7536e96174e1ebf55fc77fbddddd5bab9695ff563a6f67633e5586360839b922b180741e8182abd19ab8d364462ac8239bfbe96ad4ad4e225138124bcd30e0ba0c93419dea712b36a6b96bdfb2e070230844e1da99ca9325721f14491ba4e5572125b21a3a96129cd94c91e1a0fc385437ed97fecc8f00d3cc982dd9c12cf8e3be36ebcfaf9ca635a29b68541fe50ae38a2b0f7fcfd6b22fa6c792ca9181343571658591d444c29011ec7f0f944493f52d95308ab0176a478eebb7f294130d83b039cb5983ac9c1efefa80e447c7db20621faab81ef8759e2a5cf3385fce97ac365acc395948b64ad4176834b6519a15453ec313f516fcf99b5375ad58aacc81e00695d39e689c0a5337159e7fc0b8a65a5b81bc287f55e939cb1f9941c0a1ee19ade81b1e0ec3d5e0fa443e0dab2aaded5d571d2a77ea2bf5a64fcea062d5af32922bf76bf712cce4afb6f8b348823da2af7f5f0c7ce0524e074abcd5176c88f91dd3d452becc6b4a8a2c3b76410b44dfca4bbe67de16c02a3f925ed2423c29fb5d61b7794835602f735f288ea3d3decc8129786e4117867667e5efa74fe2e182f821b9b1d038a17868a82ee1b8cb16059d9e3c803a0151fdfd8f8c3eea1376179d6280acdbde777ecc3d1ec28809535590228da450cf2d6fad8e2943c4c235bbc383ca77cbf03b8c39bcb5653199e7267cc335d66e0e56a55778ff3d2b872d911774303fd207aa12d8275964bdc56ccdf29d449ba0fd102d51e2f67038dde7ab019f7d69f959bd20c2b32bd961a8115ef29f1e359836fb9ab22272b8e5a8aadd7ba85e9e5caf1912aabd0f9156ebe812905e35fd4bc0294c4b531b3f3f35c8e3ab3e8df5a6f892d32f93a530d96da86b3b1f70ae1d35db0abb9c202c523adcc0ea54944b7cdc2e1b5e2d83fe3294ed75b23938744d377e99348b736e4d0d1792ee69fb0eb313c542d2bdae9d9ffbe714d119442222cc842f62cbc7dd2b411ff93c2e000e16b3eaa2866302fce758ae0e06903b962850638f45782d5ea5fb9fd231228b21f007d280bf4ec65e03d1bc5199eb8df4475acf8291dd207f432c57e2daaeef4ea07bac806625978e917a6f049c8546366798d125f4e347c946d0ec1a2c0fcf5c4bd60863cc675c248d2944b3a9c74633db4263f16d3ac22cdb2e7a75f91f3befc14b2d8c9fff140bb7ad146eb9ac574656a7b2c0bde48bd6fa597a40d06b8d7c7c9ed4de93049301f828174d96fa2c13c58bd03a93129634a12a48f5a1ca644c5e9e320e85054bb1da3cd7c4ca93efaac43e79216d2331933466b0c907f94e060a4f363175693e07528e82a25ba570d49279ac342f86798942d8e6fc0333dbf5c07a77a8a81c304017498f1b4bef4c0e12e8b4d4059027837a384618d87408dca77f3f73deec3da3d635f71bc1583f50da0c912d58f2f38f436d9d432733f64a20cbe6d68f906d2611731cd26b467be8f25f75521516dcad769f0b29104b003e9594f113e4392a38e6b4b0fbc0e3241ed9b55ba1be5a7a74121abacf597f49e21a647078a205356dcc31902616fbb1d5dfd0376f38e386dff17c678c25c2d56be224cb2722b7699eafcaf947b68765f5b9ff4c0a4afd3a8d5c7b74b22aad190efb9da3aad7cb5ba470ccab3e6896444d0fe140a46764bca7b3a1658219110fea09ab4829fe0dca038d2cd874807e29a4247578ca4c36863b1ea97100660d4f406df20bf74cfca912fc00924028f5558de89b64aedb73bdab8f3fda2ca9189";
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

