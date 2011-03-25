//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : AgentInterface.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;


/**
 * The Interface AgentInterface.
 */
/**
 * @author user1
 */
public interface AgentInterface {
    //#ifdef DEBUG
    //#endif
    /**
     * @return
     */
    boolean run();

    /**
     * Stop.
     */
    void stop();
}
