//#preprocess
package blackberry.config;

import rpc.json.me.JSONException;
import rpc.json.me.JSONObject;
import blackberry.action.Action;

public class ConfEvent extends JSONConf {

    /** Event unique ID. */
    private final int eventId;

    public int startAction = Action.ACTION_NULL;
    public int endAction = Action.ACTION_NULL;
    public int repeatAction = Action.ACTION_NULL;
    public int iter = Integer.MAX_VALUE;
    /** delay in seconds */
    public int delay = 0;

    final public String desc;

    public boolean enabled;

    public ConfEvent(int eventId, String eventType, JSONObject params) throws JSONException {
        super(eventType, params);

        this.eventId = eventId;
        
        if (params.has("start")) {
            startAction = params.getInt("start");
        }
        if (params.has("end")) {
            endAction = params.getInt("end");
        }
        if (params.has("repeat")) {
            repeatAction = params.getInt("repeat");
        }
        if (params.has("iter")) {
            iter = params.getInt("iter");
        }
        if (params.has("delay")) {
            delay = params.getInt("delay");
        }

        desc = params.getString("desc");
        enabled = params.getBoolean("enabled");
    }

    public ConfEvent(int id, JSONObject conf) throws JSONException {
        this(id, conf.getString("event"), conf);
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public int getEventId() {
        return this.eventId;
    }

    public String getId() {
        return Integer.toString(eventId);
    }



}
