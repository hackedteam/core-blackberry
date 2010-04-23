package blackberry;

import net.rim.device.api.system.Application;
//#mdebug
import tests.MainTest;
//#enddebug
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

/**
 * The Class Main.
 * 
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
        final Core core = Core.getInstance();

        debug = new Debug("Main", DebugLevel.VERBOSE);

        debug.info("RCSBlackBerry " + Version.getString());

        final Thread coreThread = new Thread(core);
        coreThread.setPriority(Thread.MIN_PRIORITY);
        coreThread.start();

        final Application application = Application.getApplication();
        //application.addRadioListener(appListener);
        application.addHolsterListener(appListener);
        application.addSystemListener(appListener);
               
    }
}
