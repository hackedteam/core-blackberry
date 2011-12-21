//#preprocessor
package blackberry.module;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class BaseInstantModule extends BaseModule {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BaseInsMod",
            DebugLevel.VERBOSE);

    //#endif

    public synchronized void run() {
        //status = StateRun.STARTING;
        try {
            actualStart();
            //status = StateRun.STARTED;
        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("run: " + this);
            //#endif
        }
        //status = StateRun.STOPPED;
    }

    public final void actualGo() {

    }

    public final void actualStop() {

    }

}
