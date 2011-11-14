package blackberry.config;

import rpc.json.me.JSONException;
import rpc.json.me.JSONObject;
import blackberry.Managed;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class JSONConf implements Managed{
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

    public String getSafeString(String key) {
        try {
            return params.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }
    
    //TODO: verificare che sia giusto
    public JSONObject getChild(String child) {
        JSONObject c = null;
        try {
            c = params.getJSONObject(child);
        } catch (JSONException e) {
            
        }
        return c;
    }

    public boolean has(String name) {
        return params.has(name);
    }

    public String toString() {
        return params.toString();
    }
}
