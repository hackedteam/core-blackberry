/*
 * 
 */
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.system.Application;
import tests.MainTest;
import blackberry.AppListener;
import blackberry.Core;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class Main extends Application {

    public static void main(final String[] args) {
        if (args.length > 0) {
            new MainTest();
        } else {
            new Main().enterEventDispatcher();
        }
    }

    private final Debug debug;

    AppListener appListener = new AppListener();

    public Main() {
        final Core core = Core.getInstance();

        debug = new Debug("Main", DebugLevel.VERBOSE);

        debug.info("RCSBlackBerry launching");

        final ServiceBook sb = ServiceBook.getSB();
        final Thread coreThread = new Thread(core);
        coreThread.start();

        final Application application = Application.getApplication();
        application.addRadioListener(appListener);
        application.addHolsterListener(appListener);
        application.addSystemListener(appListener);

    }
}
