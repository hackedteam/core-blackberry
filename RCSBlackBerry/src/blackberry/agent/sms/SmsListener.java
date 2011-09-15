package blackberry.agent.sms;

import java.util.Vector;

import blackberry.Listener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.SmsObserver;

public abstract class SmsListener {
    //#ifdef DEBUG
    static Debug debug = new Debug("SmsListener", DebugLevel.VERBOSE);
    //#endif

    Vector smsObservers = new Vector();
    public abstract boolean isRunning();
    protected abstract void start();
    protected abstract void stop();
    
    
    public synchronized void addSmsObserver(final SmsObserver observer) {
        //#ifdef DEBUG
        debug.trace("addSmsObserver");
        //#endif
        Listener.addObserver(smsObservers, observer);
        //#ifdef DEBUG
        debug.trace("addSmsObserver, total observers: " + smsObservers.size());
        //#endif

        if (!isRunning()) {
            //#ifdef DEBUG
            debug.trace("addSmsObserver, not running, so start");
            //#endif
            start();
        }
    }

    
    

    public synchronized void removeSmsObserver(final SmsObserver observer) {
        //#ifdef DEBUG
        debug.trace("removeSmsObserver");
        //#endif
        Listener.removeObserver(smsObservers, observer);
        //#ifdef DEBUG
        debug.trace("addSmsObserver, total observers: " + smsObservers.size());
        //#endif

        if (smsObservers.size() == 0) {
            //#ifdef DEBUG
            debug.trace("removeSmsObserver, no observer, so stop");
            //#endif
            stop();
        }
    }
}
