package blackberry;

import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class InjectionFrameworkScreen extends MainScreen
{
    /**
     * Creates a new InjectionFrameworkScreen object
     */
    public InjectionFrameworkScreen()
    {        
        // Set the displayed title of the screen       
        setTitle("Injection Framework");
    }
    
    public boolean onClose(){
        Dialog.alert("Goodbye!");
        InjectionFrameworkApp app = (InjectionFrameworkApp)getApplication();
        app.onClose();
        
        return true;
    }
}
