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

public class ConfAction extends JSONConf {
    public int subActionId;
    public int actionId;

    public ConfAction(int actionId, int subActionId, String type,
            JSONObject params) {
        super(type, params);
        this.subActionId = subActionId;
        this.actionId = actionId;
    }

    public ConfAction(int actionId, int subActionId, JSONObject params)
            throws JSONException {
        super(params.getString("action"), params);
        this.subActionId = subActionId;
        this.actionId = actionId;
    }

    int getSubActionId() {
        return subActionId;
    }

    int getActionId() {
        return actionId;
    }

    public String getId() {
        return Integer.toString(actionId) + Integer.toString(subActionId);
    }

}
