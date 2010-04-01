package com.ht.rcs.blackberry.transfer;

public final class Proto {
    /** The Constant OK. */
    public static final int OK = 0x1; // OK

    /** The Constant NO. */
    public static final int NO = 0x2; // Comando fallito o non e' stato
    // possibile eseguirlo
    /** The Constant BYE. */
    public static final int BYE = 0x3; // Chiusura di connessione

    /** The Constant CHALLENGE. */
    public static final int CHALLENGE = 0x4; // CHALLENGE,16 byte da
    // cifrare
    /** The Constant RESPONSE. */
    public static final int RESPONSE = 0x5; // RESPONSE,16 byte cifrati

    /** The Constant SYNC. */
    public static final int SYNC = 0x6; // Mandami i log

    /** The Constant NEW_CONF. */
    public static final int NEW_CONF = 0x7; // NEW_CONF,# prendi la nuova
    // conf lunga # bytes
    /** The Constant LOG_NUM. */
    public static final int LOG_NUM = 0x8; // LOG_NUM,# stanno per
    // arrivare # logs
    /** The Constant LOG. */
    public static final int LOG = 0x9; // LOG,# questo log e' lungo #
    // bytes
    /** The Constant UNINSTALL. */
    public static final int UNINSTALL = 0xa; // Uninstallati

    /** The Constant RESUME. */
    public static final int RESUME = 0xb; // RESUME,nome,# rimandami il
    // log "nome" a partire dal byte

    /** The Constant DOWNLOAD. */
    public static final int DOWNLOAD = 0xc; // DOWNLOAD,nome: mandami il
    // file "nome" (in WCHAR, NULL
    // terminato)
    /** The Constant UPLOAD. */
    public static final int UPLOAD = 0xd; // UPLOAD,nome,directory,#: uppa
    // il file "nome" lungo # in
    // "directory"
    /** The Constant FILE. */
    public static final int FILE = 0xe; // #, Sta per arrivare un file
    // lungo # bytes
    /** The Constant ID. */
    public static final int ID = 0xf; // Id univoco della backdoor,
    // embeddato in fase di
    // configurazione
    /** The Constant INSTANCE. */
    public static final int INSTANCE = 0x10; // Id univoco che identifica
    // il dispositivo dove gira
    // la backdoor
    /** The Constant USERID. */
    public static final int USERID = 0x11; // IMSI,# byte NON paddati del
    // blocco (il blocco inviato e'
    // paddato)
    /** The Constant DEVICEID. */
    public static final int DEVICEID = 0x12; // IMEI,# byte NON paddati
    // del blocco (il blocco
    // inviato e' paddato)
    /** The Constant SOURCEID. */
    public static final int SOURCEID = 0x13; // #telefono,# byte NON
    // paddati del blocco (il
    // blocco inviato e' paddato)
    /** The Constant VERSION. */
    public static final int VERSION = 0x14; // #,bytes versione della
    // backdoor (10 byte)
    /** The Constant LOG_END. */
    public static final int LOG_END = 0x15; // La spedizione dei log e'
    // terminata
    /** The Constant UPGRADE. */
    public static final int UPGRADE = 0x16; // Tag per l'aggiornamento del
    // core
    /** The Constant ENDFILE. */
    public static final int ENDFILE = 0x17; // Tag che indica la
    // terminazione della fase di
    // download dei file
    /** The Constant SUBTYPE. */
    public static final int SUBTYPE = 0x18; // #,bytes che indicano la
    // subversion "WINMOBILE"
    
    private Proto() { };
}
