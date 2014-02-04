//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.interfaces;

import net.rim.blackberry.api.mail.Message;

public interface MailObserver extends Observer {

    void onNewMail(Message message, int maxMessageSize, String string);

}
