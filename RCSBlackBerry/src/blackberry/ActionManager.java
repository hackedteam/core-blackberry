package blackberry;

import net.rim.device.api.system.RuntimeStore;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;

public class ActionManager extends Manager implements Singleton {
    private static final long GUID = 0xfa169723286585c3L;

    /** The debug instance. */
    //#ifdef DEBUG
    static Debug debug = new Debug("ActionManager", DebugLevel.VERBOSE);
    //#endif

    /** The instance. */
    static ActionManager instance = null;

    /**
     * Gets the single instance of AgentManager.
     * 
     * @return single instance of AgentManager
     */
    public static synchronized ActionManager getInstance() {
        if (instance == null) {
            instance = (ActionManager) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final ActionManager singleton = new ActionManager();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }
}
