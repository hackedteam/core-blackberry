//#preprocess
package blackberry.action;

import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.manager.ModuleManager;

public final class StartModuleAction extends ModuleAction {

    //#ifdef DEBUG
    static Debug debug = new Debug("StartModAct", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new start agent action.
     * 
     * @param params
     *            the conf params
     */
    public StartModuleAction(final ConfAction params) {
        super(params);
    }

    /*
     * (non-Javadoc)
     * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
     */
    public boolean execute(Trigger trigger) {
        //#ifdef DEBUG
        debug.trace("execute");
        //#endif

        final ModuleManager agentManager = ModuleManager.getInstance();
        agentManager.start(moduleId);
        return true;
    }

    //#ifdef DEBUG
    public String toString() {
        return "Start " + moduleId;
    }
    //#endif
}
