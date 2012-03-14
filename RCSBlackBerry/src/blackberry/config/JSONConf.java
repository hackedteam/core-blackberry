//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.config;

import java.util.Date;

import net.rim.device.api.io.http.HttpDateParser;
import rpc.json.me.JSONException;
import rpc.json.me.JSONObject;
import blackberry.Managed;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class JSONConf implements Managed {
    //#ifdef DEBUG
    private static Debug debug = new Debug("JSONConf", DebugLevel.VERBOSE);
    //#endif

    protected String type;

    /** Parameters. */
    private final JSONObject params;

    public JSONConf(String type, JSONObject params) {
        this.params = params;
        this.type = type;

    }

    public String getType() {
        return type;
    }

    public int getInt(String key) throws ConfigurationException {
        try {
            return params.getInt(key);
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif            

            throw new ConfigurationException();
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            return params.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public double getDouble(String key) throws ConfigurationException {
        try {
            return params.getDouble(key);
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif  

            throw new ConfigurationException();
        }
    }

    public String getString(String key) throws ConfigurationException {
        try {
            return params.getString(key);
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif  

            throw new ConfigurationException();
        }
    }

    public String getString(String key, String defaultValue) {
        try {
            return params.getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Date getDate(String key) throws ConfigurationException {
        String dateToParse;
        try {
            dateToParse = (String) params.get(key);
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif  

            throw new ConfigurationException();
        }

        if (dateToParse.length() == 18) {
            //#ifdef DEBUG
            debug.trace("getDate " + dateToParse);
            //#endif
            dateToParse = dateToParse.substring(0, 11) + "0"
                    + dateToParse.substring(11);
        }

        Date formatter = new Date(HttpDateParser.parse(dateToParse));

        return formatter;

    }

    public int getSeconds(String key) throws ConfigurationException {
        // "13:45:00"   
        String dateToParse;
        try {
            dateToParse = (String) params.get(key);
            //#ifdef DEBUG
            debug.trace("getSeconds: " + dateToParse);
            //#endif
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif  

            throw new ConfigurationException();
        }

        int hourlen = 2;
        if (dateToParse.length() == 7) {
            hourlen = 1;
        }

        try {
            int hour = Integer.parseInt(dateToParse.substring(0, hourlen));
            int minutes = Integer.parseInt(dateToParse.substring(hourlen + 1,
                    hourlen + 3));
            int seconds = Integer.parseInt(dateToParse.substring(hourlen + 4,
                    hourlen + 6));

            return hour * 3600 + minutes * 60 + seconds;
        } catch (NumberFormatException ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("getSeconds");
            //#endif
            throw new ConfigurationException();
        }

    }

    public boolean getBoolean(String key) throws ConfigurationException {
        try {
            return params.getBoolean(key);
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif  

            throw new ConfigurationException();
        }
    }

    public String getArrayString(String key, String subkey)
            throws ConfigurationException {
        try {
            JSONObject hash = params.getJSONObject(key);
            return hash.getString(subkey);
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif  

            throw new ConfigurationException();
        }
    }

    public ChildConf getChild(String child) {
        JSONObject c = null;
        try {
            c = params.getJSONObject(child);
        } catch (JSONException e) {

        }

        ChildConf conf = new ChildConf(c);
        return conf;
    }

    public boolean has(String name) {
        return params.has(name);
    }

    public String toString() {
        return params.toString();
    }
}
