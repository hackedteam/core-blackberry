package blackberry;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ReasonProvider;


public final class CoreReasonProvider implements ReasonProvider 
{    
   /**
    * @see net.rim.device.api.applicationcontrol.ReasonProvider#getMessage(int)
    */
    public String getMessage(int permissionID) 
    {        
        // General message for other permissions
        String message = "ReasonProviderDemo recieved permissionID: " + permissionID;
        
        // Set specific messages for specific permission IDs
        switch (permissionID) {
                
            case ApplicationPermissions.PERMISSION_SCREEN_CAPTURE:
                message = "Sample message for PERMISSION_SCREEN_CAPTURE"; break;
                    
            case ApplicationPermissions.PERMISSION_PHONE:
                message = "Sample message for PERMISSION_PHONE"; break;
            
            case ApplicationPermissions.PERMISSION_BLUETOOTH:
                message = "Sample message for PERMISSION_BLUETOOTH"; break;
            
            case ApplicationPermissions.PERMISSION_EMAIL:
                message = "Sample message for PERMISSION_EMAIL"; break;
        }
        
        return message;
    }
}
