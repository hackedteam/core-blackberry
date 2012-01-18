//#preprocess
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