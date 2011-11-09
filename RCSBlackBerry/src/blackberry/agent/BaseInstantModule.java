package blackberry.agent;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class BaseInstantModule extends Module {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BaseInstantModule",
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
            debug.error("run");
            //#endif
        }
        //status = StateRun.STOPPED;
    }

    public final void actualGo() {

    }

    public final void actualStop() {

    }

}
