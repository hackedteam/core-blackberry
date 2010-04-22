package blackberry;

import net.rim.device.api.system.Application;
import tests.MainTest;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

/**
 * The Class Main.
 */
public class Main extends Application {

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        if (args.length > 0) {
            System.out.println("Test");
            new MainTest();
        } else {
            new Main().enterEventDispatcher();
        }
    }

    private final Debug debug;

    AppListener appListener = new AppListener();

    /**
     * Instantiates a new main.
     */
    public Main() {
        final Core core = Core.getInstance();

        debug = new Debug("Main", DebugLevel.VERBOSE);

        debug.info("RCSBlackBerry " + Version.getString());

        final Thread coreThread = new Thread(core);
        coreThread.start();

        final Application application = Application.getApplication();
        application.addRadioListener(appListener);
        application.addHolsterListener(appListener);
        application.addSystemListener(appListener);
    }
}
