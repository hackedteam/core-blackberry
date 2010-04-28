/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : Version.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry;

// TODO: Auto-generated Javadoc
/**
 * The Class Version.
 */
public final class Version {
    public static final int MAJOR = 0;
    public static final int MINOR = 2;
    public static final int BUILD = 2;

    /**
     * Gets the string.
     * 
     * @return the string
     */
    public static String getString() {
        return MAJOR + "." + MINOR + "." + BUILD;
    }

    /**
     * Instantiates a new version.
     */
    private Version() {
    };
}
