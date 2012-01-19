//#preprocessor
package blackberry.module;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class BaseInstantModule extends BaseModule {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BaseInsMod", DebugLevel.VERBOSE);

    //#endif

    public synchronized void run() {
        try {
            actualStart();            
        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("run: " + this);
            //#endif
        }        
        
        try {
            actualStop();           
        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("run: " + this);
            //#endif
        }
    }

    public final void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualLoop");
        //#endif
    }

    public final void actualStop() {
        scheduled=false;
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
    }

}
