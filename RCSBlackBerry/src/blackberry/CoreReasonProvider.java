//#preprocess
/*
 * 
 */
package blackberry;

import net.rim.device.api.applicationcontrol.ReasonProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class CoreReasonProvider.
 */
public final class CoreReasonProvider implements ReasonProvider {

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
        final String message = "Reason: " + permissionID;
        return message;
    }
}
