package blackberry.agent.sms;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

import net.rim.blackberry.api.sms.OutboundMessageListener;

public class SMSOUTListener implements OutboundMessageListener{
  //#ifdef DEBUG
    private static Debug debug = new Debug("SMSOUTListener", DebugLevel.VERBOSE);
    //#endif
    
    public void notifyOutgoingMessage(Message arg0) {
        //#ifdef DEBUG_INFO
        debug.info(arg0.getAddress());
        //#endif
        
    }

    public void notifyIncomingMessage(MessageConnection arg0) {
        //#ifdef DEBUG_INFO
        debug.info(arg0.toString());
        //#endif
        
    }

    

}
