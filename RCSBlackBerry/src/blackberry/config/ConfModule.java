package blackberry.config;

import rpc.json.me.JSONObject;

public class ConfModule extends JSONConf {

    /**
     * Instantiates a new agent.
     * 
     * @param moduleId
     *            the id
     * @param jmodule
     *            the params
     */
    public ConfModule(final String moduleType, final JSONObject jmodule) {
        super(moduleType, jmodule);
    }

    public String getId() {
        return getType();
    }

}
