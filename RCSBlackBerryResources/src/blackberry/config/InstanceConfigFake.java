//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //824
    static String configString = "0000000000000000ee50ff6c4618c0418b08c0679a8bbb41849d50c2e70eb1ac4caf3dd099bef4bee6f7dc75c5732fa5ab6494944ef8944cab36c73190cf932ae6badfd547a72e99ff3679a842917caf92ffbe65f96dbac1e37521d987fd0a6526917cc3fb6d06039640d48bcab159f311fa5002b1cd8551959cdcc56956a80b9b5ddcef6b16a66dfb86b7c098d28a2a7fc4d50c598716a39e4be707d8347f3019ac142724c382f21b1be06259c7ef7818b484e678a067003ed158a55a01928ea8a4e6ebdf3529b09aa43368141d421b0dae0a8f36d020038df41ebd2d45113facad7b9f9bd5bea5b53fe32ab20797988ca15881e4422e440466957e3e9693f3f6f5b5651d09d9bb35b4ce6592c5fa6c8addc794913f943fdeff55a67076cd102742e1007c7500c35ef286e6aaf71638398748f95aec123422507f7bd14b3d8ebcd6991562b133ab6146e5a3a93e730a8bbc0a9c61335b65ae0efb8c28fa2a2e43e5cf7894f848fcaaedb921cdf70fe1cc80f07d56cc2c2ae4c2400343d9f38234a5176b0a41dd877e846917c989498185982a9f9d486712ed88c84a2f26632f93011a32486625c4a51a97a8416d52a50db9dafb86057089ed20a8e93d7645ea546b71e11e95185fe5e245a85e04d64f4a9408d556fcd4c8133503582cc4b8a52422136288b9c524cee8c8fb6d6df03849bae7d608832faf1ca117c97becacbe5b31d118112a897fbb7292bfe1cb5232dc714260a517564065a50eb792790ddf4e7b6e2182662ea1b12fb11edd4a8a561ad1ba53ebca6506164000281dab3e92531d57c53a468083dfc85f066f31d976781aa7014e7f25bd7a98ed5a3872fd57c0e5ef3d9b53df040fe3c6e9b512b773cefdd6ae4ae51deed87a767d61a2d464f58de35a94fc5e58d51c696c9716b00b6645198c4cd5e345ec0904cde7354899c7ef712bcc6e5de1521d99e20d9e3a4cd892463f8fbf0bd9a2f42846c0556a60f14fb0f06897d669c82a8da663b397a6cdb1244eaf3d1b613a6c82be8f4833d0c2f8e7da64761dfec2f8e50504c22b278000d3bc374d04bca90dd0a57a69e5ee766bd458836917586d7b7d24bad599139bfd35ff07cf7e5af6087eb8904e27b4e66a93927c5ec23a35a10f01baa3c5a008744f94a5b4b5213cafdbef2ed0a5a9168f159087b867478ba90d4254f591dbdbe02193de9e8a5381a252e4bf6d820fa07d7c591c00b560f659408346daeacf2fe96efaf6f4745e06fbe586012ff237008f3c948c77cda79f23490899f52f585a693f2edbacc489fc69f266bea8b19e06b82cba4e26ccecec260cd19a7e39c346c9572e06840b84043fecd03c5975c7907f988dc4eaaefcb9b25587c45e6e050914b4c613cb35ed561f4e3f8be4a8bc1ea7f07e7e3226e5080cc8b6f9807fde3ca79d1623db44a2e4d2561412a1be41be924b8efa07cb3e1ca9467b92cafb92566cb1f13887e5816d98fa21dc93e55453cfdb57c1dbaca66626b2b0c4289d953c2dcfb0e3c81feecb98ed54d82de851a74d8d878895da85d1eed7cd3562e0e90f2a3a7eb8e6f3cfeb0d5441b6a7548099eba8f6f162665b349767458ecde3b3b51ae8572a15e358ab3b10e930d75cba0c86afdc1dc2288e251d62fa387bfe87f3cd6c0f9218c4e1c7016282ebf337e1d8547a61db3200ba831a3de548d93233bd68987c16320e311d28e22832db3310b2293324aed52d578dcf630a2049baca79d70b7491aff25ca07f7ff2c927ba6e585aa9a7a86bf8e4357b7750b549ba80c5b60c1f1432daea9463eb2ba62ce1eb43983c08e8d50b9c105eb9173dfc30b3cd4d5d209d360843480034fe0d8375b99d917c5843934a4b0319eb9b4d9692b2f089de3cecc35a33221e074a4ea382724c66ec33943eb4b1545d080fc83334af8445e759ffdc4e7be9bd74e050ac56d600f3717fc5bbba78603bc680a7283fda1fc73faf96e693c11ac061bd8f87e3181a391b9d8ff136aa31f4565001107028a458be07a5b4c9db";
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

