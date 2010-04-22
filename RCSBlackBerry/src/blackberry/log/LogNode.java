/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : LogNode.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.log;

public class LogNode {

    String dirName;
    boolean onSD;

    public int numElem;

    public LogNode(final String dirName_, final boolean onSD_) {
        this.dirName = dirName_;
        this.onSD = onSD_;
    }

    public String toString() {
        return dirName + ": " + numElem;
    }
}
