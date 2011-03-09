package blackberry.interfaces;

import net.rim.blackberry.api.mail.Message;


public interface MailObserver extends Observer {

   void onNewMail(Message message, int maxMessageSize, String string);

}
