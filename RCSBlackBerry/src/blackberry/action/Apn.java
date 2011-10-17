//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Apn.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class Apn {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Apn", DebugLevel.VERBOSE);
    //#endif
    public int mcc;
    public int mnc;
    public String apn;
    public String user = "";
    public String pass = "";

    //#ifdef DEBUG
    public String toString() {
        return "Mcc/Mnc " + mcc + "/" + mnc + " " + apn + ":" + user + ":"
                + pass;
    }
    //#endif

    public boolean isValid() {
        
        return apn!=null && apn.length() > 0;
    }
}
