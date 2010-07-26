//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Apn.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

public class Apn {
    public int mcc;
    public int mnc;
    public String apn;
    public String user;
    public String pass;

    public String toString() {
        return "Mcc/Mnc " +mcc + "/" + mnc + " " + apn + ":" + user + ":" + pass;
    }
}
