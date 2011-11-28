//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Agent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.module;

import blackberry.TimerJob;
import blackberry.Trigger;
import blackberry.config.ConfModule;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

/**
 * The Class Agent.
 */
public abstract class BaseModule extends TimerJob {
   
    //#ifdef DEBUG
    private static Debug debug = new Debug("Module", DebugLevel.VERBOSE);
    //#endif

    private ConfModule conf;
    private Trigger trigger;

    protected abstract boolean parse(ConfModule conf);

    public String getType() {
        //#ifdef DBC
        Check.requires(conf != null, "null conf");
        //#endif
        return conf.getType();
    }

    public boolean setConf(ConfModule conf) {
        //#ifdef DBC
        Check.requires(conf != null, "null conf");
        //#endif
        this.conf = conf;
        return parse(conf);
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
    
    public final String getId(){
        return conf.getType();
    }

    //#ifdef DEBUG
    public String toString() {
        return "Module <" + conf.getType().toUpperCase() + "> " + (isRunning() ? "running" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    //#endif

}
