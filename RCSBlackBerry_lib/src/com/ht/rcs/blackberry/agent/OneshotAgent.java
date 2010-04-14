package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public abstract class OneshotAgent extends Agent{

    private static Debug debug = new Debug("OneshotAgent", DebugLevel.VERBOSE);
    
    protected OneshotAgent(int agentId, boolean agentEnabled, boolean logOnSD,
            String name) {
        super(agentId, agentEnabled, logOnSD, name);
        
    }

    
   
}
