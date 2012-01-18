//#preprocess
package blackberry.interfaces;

import javax.wireless.messaging.Message;

public interface MmsObserver extends Observer {
    void onNewMms(Message message, boolean incoming);

}