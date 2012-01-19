//#preprocess
package blackberry.config;

import java.util.Date;

import net.rim.device.api.io.http.HttpDateParser;
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
        
        if(dateToParse.length()==18){
            //#ifdef DEBUG
            debug.trace("getDate " + dateToParse);
            //#endif
            dateToParse=dateToParse.substring(0,11) + "0" + dateToParse.substring(11);
        }
        
        Date formatter = new Date(HttpDateParser.parse(dateToParse));
       
        return formatter;
        
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
    
    public int getSafeInt(String key, int defaultValue) throws ConfigurationException {
        try {
            if(!params.has(key)){
                throw new ConfigurationException();
            }
            return params.getInt(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
    
    //TODO: verificare che sia giusto
    public ChildConf getChild(String child) {
        JSONObject c = null;
        try {
            c = params.getJSONObject(child);
        } catch (JSONException e) {
            
        }
        
        ChildConf conf= new ChildConf(c);
        return conf;
    }

    public boolean has(String name) {
        return params.has(name);
    }

    public String toString() {
        return params.toString();
    }
}
