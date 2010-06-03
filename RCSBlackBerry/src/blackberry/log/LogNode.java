//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : LogNode.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.log;

// TODO: Auto-generated Javadoc
/**
 * The Class LogNode.
 */
public class LogNode {

    String dirName;
    boolean onSD;

    public int numElem;

    /**
     * Instantiates a new log node.
     * 
     * @param dirName_
     *            the dir name_
     * @param onSD_
     *            the on s d_
     */
    public LogNode(final String dirName_, final boolean onSD_) {
        dirName = dirName_;
        onSD = onSD_;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return dirName + ": " + numElem;
    }
}
