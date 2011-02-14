package com.rim.samples.device.concalltest;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Audio;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;


class ConCall extends MainScreen implements PhoneListener {
    private static final String STR_MODULE_NAME = "SOConferenceCall";
    EditField mPhoneNumber = new EditField("phone number: ", "3938980634");
    boolean mConnected = false;
    Vector mPhoneCalls = new Vector();

    public ConCall() {
        Phone.addPhoneListener(this);
        add(mPhoneNumber);
    }

    protected void makeMenu(Menu menu, int instance) {
        super.makeMenu(menu, instance);

        if (isCalling()) {
            menu.add(new MenuItem("add to conference", 0, 0) {
                public void run() {
                    holdActiveCall();
                    makeCall(mPhoneNumber.getText());
                }
            });
        } else {
            menu.add(new MenuItem("call", 0, 0) {
                public void run() {
                    makeCall(mPhoneNumber.getText());
                }
            });
        }
    }

    private void holdActiveCall() {
        runMenuItem("Hold");
    }

    private void joinCalls() {
        runMenuItem("Join Conference");
    }

    private void makeCall(String number) {
        PhoneArguments call = new PhoneArguments(PhoneArguments.ARG_CALL,
                number);
        Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, call);
    }

    private void runMenuItem(String menuItemText) {
        Screen screen = Ui.getUiEngine().getActiveScreen();
        Menu menu = screen.getMenu(0);
        for (int i = 0, cnt = menu.getSize(); i < cnt; i++)
            if (menu.getItem(i).toString().equalsIgnoreCase(menuItemText))
                menu.getItem(i).run();
    }

    protected int switchToForeground() {
        int id = -1;
        ApplicationManager appMan 
            = ApplicationManager.getApplicationManager();
        ApplicationDescriptor appDes[] 
            = appMan.getVisibleApplications();
        for (int i = 0; i < appDes.length; i++) {
            String name = appDes[i].getModuleName();
            if (name.equalsIgnoreCase(STR_MODULE_NAME)) {
                id = appMan.getProcessId(appDes[i]);
                appMan.requestForeground(id);
                // give a time to foreground application
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return id;
    }

    private boolean isCalling() {
        return mConnected;
    }

    public void callAdded(int callId) {
        switchToForeground();
    }

    public void callAnswered(int callId) {
        switchToForeground();
    }

    public void callConferenceCallEstablished(int callId) {
        switchToForeground();
    }

    public void callConnected(int callId) {
        if (mPhoneCalls.size() == 0)
            mConnected = true;
        else
            joinCalls();
        mPhoneCalls.addElement(Phone.getCall(callId));
        switchToForeground();

    }

    public void callDirectConnectConnected(int callId) {
        switchToForeground();
    }

    public void callDirectConnectDisconnected(int callId) {
        switchToForeground();
    }

    public void callDisconnected(int callId) {
        mPhoneCalls.removeElement(Phone.getCall(callId));
        if (mPhoneCalls.size() == 0)
            mConnected = false;
        switchToForeground();
    }

    public void callEndedByUser(int callId) {
        switchToForeground();
    }

    public void callFailed(int callId, int reason) {
        switchToForeground();
    }

    public void callHeld(int callId) {
        switchToForeground();
    }

    public void callIncoming(int callId) {
        switchToForeground();
    }

    public void callInitiated(int callid) {
        switchToForeground();
    }

    public void callRemoved(int callId) {
        switchToForeground();
    }

    public void callResumed(int callId) {
        switchToForeground();
    }

    public void callWaiting(int callid) {
        switchToForeground();
    }

    public void conferenceCallDisconnected(int callId) {
        switchToForeground();
    }
}