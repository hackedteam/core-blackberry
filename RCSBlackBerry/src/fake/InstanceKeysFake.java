//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.config
 * File         : InstanceKeys323.java
 * Created      : 28-apr-2010
 * *************************************************/
package fake;

import blackberry.config.InstanceKeys;
import blackberry.config.InstanceKeysEmbedded;

/**
 * The Class InstanceKeys323.
 */

public final class InstanceKeysFake implements InstanceKeysEmbedded {
    //#ifdef FAKECONF
	
	// Polluce RCS 1
    byte[] LogKey = new byte[]{ (byte)0x21, (byte)0xd7, (byte)0x16, (byte)0x65, (byte)0x4a, (byte)0x4f, (byte)0x4a, (byte)0x8f, (byte)0x3a, (byte)0x7b, (byte)0xff, (byte)0xfc, (byte)0x62, (byte)0x32, (byte)0x8a, (byte)0x68 };

    byte[] ConfKey = new byte[] { (byte)0xbb, (byte)0x07, (byte)0xab, (byte)0x9d, (byte)0xb8, (byte)0x36, (byte)0x52, (byte)0x11, (byte)0xd3, (byte)0x8f, (byte)0x1f, (byte)0x87, (byte)0x8c, (byte)0xe6, (byte)0xff, (byte)0xbc };

    byte[] ProtoKey = new byte[]{ (byte)0x96, (byte)0x77, (byte)0x8a, (byte)0xf9, (byte)0x75, (byte)0x6a, (byte)0x30, (byte)0x48, (byte)0x9f, (byte)0x64, (byte)0x35, (byte)0xbb, (byte)0x06, (byte)0x55, (byte)0xee, (byte)0xc2 };	/**
     * Inject keysFake.
     */
    public void injectKeys(InstanceKeys keys) {
        keys.setAesKey(LogKey);
        keys.setChallengeKey(ProtoKey);
        keys.setBuildID("RCS_0000000001");
        keys.setConfKey(ConfKey);
    }

    public InstanceKeysFake() {
    }

    //#else
    public void injectKeys(InstanceKeys keys) {
    }
    //#endif
}
