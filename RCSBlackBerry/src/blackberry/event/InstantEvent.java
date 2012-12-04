package blackberry.event;

import blackberry.Status;
import blackberry.action.Action;
import blackberry.config.ConfEvent;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event.Future;

public abstract class InstantEvent extends Event {
    //#ifdef DEBUG
    private static Debug debug = new Debug("InstantEvent", DebugLevel.VERBOSE);
    //#endif
    
    protected void onEnter() {
        
        //#ifdef DEBUG
        debug.info("onEnter");
        //#endif

        triggerStartAction();
    }

}
