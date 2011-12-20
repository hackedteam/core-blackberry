//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.evidence;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public class DictMarkup extends Markup {

    //#ifdef DEBUG
    static Debug debug = new Debug("DictMarkup", DebugLevel.VERBOSE);
    //#endif

    private static final int MARKUP_SIZE = 35 * 100;
    private static final int MAX_DICT_SIZE = 100;
    private Hashtable dictionary = null;

    public DictMarkup(int agentId, byte[] aesKey) {
        super(agentId, aesKey);
        initDictMarkup();
    }

    protected synchronized void initDictMarkup() {
        //#ifdef DEBUG
        debug.trace("initDictMarkup");
        //#endif
        dictionary = new Hashtable();

        if (!isMarkup()) {
            writeMarkup(Utils.intToByteArray(0));
            return;
        }

        byte[] plain;
        try {
            plain = readMarkup();

            final DataBuffer dataBuffer = new DataBuffer(plain, 0,
                    plain.length, false);

            final int size = dataBuffer.readInt();
            for (int i = 0; i < size; i++) {
                final String key = new String(dataBuffer.readByteArray());
                final byte[] value = dataBuffer.readByteArray();
                dictionary.put(key, value);
                //#ifdef DEBUG
                debug.trace("initDictMarkup unserialize: " + key);
                //#endif
            }
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("initDictMarkup");
            removeMarkup();
            //#endif
        }
    }

    protected synchronized boolean writeMarkup(Hashtable dict) {
        final DataBuffer dataBuffer = new DataBuffer(false);
        final Enumeration enumeration = dict.keys();
        //#ifdef DEBUG
        debug.trace("writeMarkup size: " + dict.size());
        //#endif
        dataBuffer.writeInt(dict.size());

        while (enumeration.hasMoreElements()) {
            try {
                final String key = (String) enumeration.nextElement();
                final byte[] data = (byte[]) dict.get(key);

                //#ifdef DEBUG
                debug.trace("writeMarkup key: " + key + " value: " + data);
                //#endif
                dataBuffer.writeByteArray(key.getBytes());
                dataBuffer.writeByteArray(data);
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error("writeMarkup");
                //#endif
                return false;
            }
        }
        return writeMarkup(dataBuffer.toArray());

    }

    public synchronized boolean put(String key, byte[] data) {
        if (key == null || data == null) {
            //#ifdef DEBUG
            debug.error("key==null || value==null");
            //#endif
            return false;
        }

        //#ifdef DBC
        Check.requires(key != null, "put key null");
        Check.requires(data != null, "put value null");
        //#endif

        Object prev = dictionary.put(key, data);
        //#ifdef DEBUG
        debug.info("put key: " + key + " total dict size: " + dictionary.size());
        //#endif

        if (!data.equals(prev)) {
            if (dictionary.size() > MAX_DICT_SIZE) {
                shrinkDictionary();
            }
            return writeMarkup(dictionary);
        }

        return true;

    }

    private void shrinkDictionary() {
        //#ifdef DEBUG
        debug.warn("shrinkDictionary");
        //#endif
        if (dictionary.size() > 0) {
            final Object key = dictionary.keys().nextElement();
            dictionary.remove(key);
        }
    }

    public synchronized byte[] get(String key) {
        if (dictionary.containsKey(key)) {
            try {
                final byte[] data = (byte[]) dictionary.get(key);
                //#ifdef DEBUG
                debug.info("get key: " + key + " data: " + data);
                //#endif
                return data;
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error("get");
                //#endif
                return null;
            }

        } else {
            return null;
        }
    }

    public synchronized void removeMarkup() {
        super.removeMarkup();
        if (dictionary != null) {
            dictionary.clear();
        }
    }
}
