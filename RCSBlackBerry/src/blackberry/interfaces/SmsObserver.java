package blackberry.interfaces;

import javax.wireless.messaging.Message;

public interface SmsObserver extends Observer{

    void onNewSms(Message message, boolean incoming);

}
