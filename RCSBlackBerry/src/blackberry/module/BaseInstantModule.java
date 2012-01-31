//#preprocessor
package blackberry.module;

import java.util.Timer;

import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class BaseInstantModule extends BaseModule {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BaseInsMod", DebugLevel.VERBOSE);

    //#endif

    public synchronized void run() {
        try {
            //#ifdef DBC
            Check.requires(getDelay() == SOON, "run, delay not SOON");
            //#endif

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

    // Override
    public synchronized void addToTimer(final Timer timer) {
        //#ifdef DEBUG
        debug.trace("addToTimer");
        //#endif
        run();
    }

    public final void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualLoop");
        //#endif
    }

    public final void actualStop() {
        scheduled = false;
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
    }

}
