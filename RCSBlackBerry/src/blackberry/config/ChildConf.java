//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.config;

import rpc.json.me.JSONObject;

public class ChildConf extends JSONConf {

    public ChildConf(JSONObject params) {
        super("child", params);
    }

    public String getId() {
        return "child";
    }
    
}
