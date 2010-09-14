//#preprocess
package blackberry.config;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

public class InstanceConfigFake {
    //#ifdef FAKECONF
     
    //1308
    static String configString = "000000000000000017df3d665b6316c9c2d3692eae9b720b50604b83b73efdef855ed0c8514f36b5a739153777adf46ca4097bee495b41de130b1e7633040194c1957a3957687fe08dc587b2213aca67d863acb73baf900d3aa41cdbfbcb905fa19ac703fbd1f568f5c4aabf430721f37c454f00533235080c5ead233e85edb663112be8f037bf1a7eed823bbe32dae7311295655aab6e2d38a060b6cc6ba93fd62bbe38b94aff42b861008687d5d3cc5e968ee3d8cd3754def66e79510086c092f1e71d22c3354d165118a600c84392c5b76c1510c9f87764928a98152f61021ef10b95fa284bfb391241ac4298cd4c4430f5327f329ea96bfd2bd7936f4c11254bfafeba0711d077edceadaf363836f8532a4f81af604af2ee8b517eca69267c6e33d9d150202cd6d7f794ca70d8a983514e3284c8d93a525f679287e1b85d3f5ee721d292ee76f50708216b398d30534378ad4bd8c7308a866bebb59a2d02136ff0acb2cb13bb30a958d4ff909e18722fe509954522a3da99ef2724c74b285cf4781c5bea994214cce254e926289e8caf1bd89100b5a559fd22729536959da6c78c74a479004ca64f5cc587204cae19daf0326449f6a22ea0bcbd5b9e7ed75d3ba0d5b12e1f549f4b094a4532c1d197f27f72f5092dae14c9c024e72778877ae39157c623fc4a6171f7e84d10d843c43ed0abe9a5b076beba7a8f6f6e1ed76a3661d20c4313435b0a39a5b998e9916f386cd441cecba4bb4d606c9d82ea6cb8726830ec3c5f57bc41819996b850a81707776b60be03f46515ff66d65485e9fe6d4d21b591a41cb04624a2f5ffe048c5c7db9998df93b244ebceeec382021da8ca09e5b0215766faa50dc2f3a38743cd0f20154e692bd0634c564da055327c466c2faf3950dc0c4efe6771dd483e9d062f4757b346e563bc835a5ffe8911d0ec81dc589a11cb98fe4f389adce8bc3a2850bff9b1579f94d59977b3774c33f62cbac67164bdf636c84a6cda6a88231ea1f3d54e8628ec35840c3003e882d09d02fa22349fa709e6193a8c9516031f3592b050abf3c32c0fdcd6b6f0b453a377e4c1b1175f190fe5910cde79b06491658e54e8a45ccbae739d441ef45487440f42dc15f9a19f497dcd0aa42a532b293907d59b86f811dc6d429b1ee485f1c35a2afb1d9c7ce29f7fa84d6c5c103c4f4c13c611da9fccc50160ca8aa5ef6e0999d7a0cbcdb87d87c84b8327a239c2af73866d82503ea3c9483fe7a82aaed1495e60f1168434b04ec3fc4866719221eb628ccdd53ffea1df2426df90ea5337051248fc70e92578c4ccab65992deb206458c4a4ad331b81e8a99f8ce0c72da6a9bb29972f228f866b125240ba374354f6c43da2f2d021e44577468af31c7446ca29c9ec2405c078d467e5676c37eb373f416d623115454e3757cb5bd8ff71216ea6d1b4250fcfca9e358420fb34e02c00ac2ca98af4a3a1a0ef015bcc070ec13057e67ee2a48aad7c10ed944aeab982ca5e1a841d5b6f10fa0bf3139b98bb4aa60e365b1e94cbf03607c2adc48b39ad0107976a9e749d933d93449bec048b405ae327b85224c427346cf4ad5fefd2c32dee99b4de8c31fa2ed6df29ee909291b38ae73e629ee1c5e42a2faded9d78ce775c9a4c41a6c195a894528da7de325330a2762e0fcbe4aadfc8880da737cdd1249789d426ad3a6599853509119b237c50f6cb458923c0e04dfef73d4f7517e41aaf4a9c607f3144c1702284d155fc2941c537616d704188d65ea6c4484360ac915d74029f30e448c944d861fad25f8ef7a38de1bda518643d8ef794c8036069253ef2d5dbf016a3b389c49894c61e3f098ed3e91a46db04794e70365b05a4fcb6399f335dc1b8d98ad313028d593221e7623348a6fbfeb528a839fd31cfbacdee80120a30f3ad79efad95425d48f6ed00ceb0f6c4d7b4f9af5732c37efeac5771164ee30e6a45062376a6fbc3f4bde3b8a8588d9552ab3e6290c27f694a5210a6ca2e86b586bb494304e82c1343195ed62f7e030e679de1c47e7a3ebf7354bcbe18ea542733a954c17f9a81f16b413439eef15d3b8186c91e4524f717cd234adc2e87576d0c6195638b";
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

