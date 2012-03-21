//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

/*
 * 
 */
package blackberry;

import net.rim.device.api.applicationcontrol.ReasonProvider;

/**
 * The Class CoreReasonProvider.
 */
public final class CoreReasonProvider implements ReasonProvider {
    //#ifdef DEBUG
    //#endif
    /**
     * Gets the message.
     * 
     * @param permissionID
     *            the permission id
     * @return the message
     * @see net.rim.device.api.applicationcontrol.ReasonProvider#getMessage(int)
     */
    public String getMessage(final int permissionID) {
        // General message for other permissions
        // A.3=Reason: 
        final String message = Messages.getString("A.3") + permissionID;
        return message;
    }
}
