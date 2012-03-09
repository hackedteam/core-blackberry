package blackberry.injection.injectors.group;

import blackberry.injection.injectors.AInjector;


public abstract class UrlGroupInjector extends AInjector {


    public boolean enabled(){
        return enabledGroup && enabled;
    }
    
    static boolean  enabledGroup;
    static public boolean enabledGroup() {
        return enabledGroup;
    }
    static public void enableGroup(boolean value) {
        enabledGroup=value;
    }

}
