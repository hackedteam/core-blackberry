//#preprocess
package blackberry;

import java.util.Vector;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Observer;
import blackberry.utils.Check;

public abstract class Listener {

    //#ifdef DEBUG
    static Debug debug = new Debug("Listener", DebugLevel.INFORMATION);

    //#endif

    /**
     * Adds the observer.
     * 
     * @param observers
     *            Vector of observers
     * @param observer
     *            the observer
     */
    public synchronized static void addObserver(final Vector observers,
            final Observer observer) {

        //#ifdef DBC
        Check.requires(!observers.contains(observer), "already observing");
        //#endif

        //#ifdef DEBUG
        debug.trace("adding observer: " + observer);
        //#endif
        if (!observers.contains(observer)) {
            observers.addElement(observer);
        }
    }

    /**
     * Removes observer.
     * 
     * @param observers
     *            Vector of observers
     * @param observer
     *            the observer
     */
    public synchronized static void removeObserver(final Vector observers,
            final Observer observer) {
        //#ifdef DEBUG
        debug.trace("removing observer: " + observer);
        //#endif

        if (observers.contains(observer)) {
            observers.removeElement(observer);
        } else {
            //#ifdef DEBUG
            //debug.error("removing observer not present: " + observer);
            //#endif
        }
    }
}
