//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.evidence;

import java.util.Hashtable;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class LineMarkup extends DictMarkup {

    //#ifdef DEBUG
    private static Debug debug = new Debug("LineMarkup", DebugLevel.VERBOSE);
    //#endif

    private Hashtable lineHash = new Hashtable();

    public LineMarkup(String id) {
        super(id);
    }

    public synchronized boolean put(String key, String line) {

        Object last = lineHash.put(key, line);
        if (!line.equals(last)) {
            //#ifdef DEBUG
            debug.trace("put, serialize key: " + key);
            //#endif
            return put(key, line.getBytes());
        }
        return true;

    }

    public synchronized String getLine(String key) {

        //#ifdef DEBUGS
        debug.trace("getLine: " + key);
        //#endif
        if (lineHash.containsKey(key)) {
            //#ifdef DEBUG
            debug.trace("getLine memoized");
            //#endif
            return (String) lineHash.get(key);
        }

        //#ifdef DEBUG
        debug.trace("getLine: try to get from dictionary");
        //#endif
        byte[] data = get(key);
        String line = null;
        if (data != null) {
            line = new String(data);
        }

        return line;

    }

}
