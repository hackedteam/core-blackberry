package blackberry.screen;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.Main;
import blackberry.Status;

/**
 * Vedi anche ScreenFake
 * 
 * @author zeno
 * 
 */
public class BlackScreen extends MainScreen {

    private Timer timer = new Timer();
    UiApplication application;

    public BlackScreen(UiApplication application) {
        //super(Field.USE_ALL_HEIGHT|Field.FIELD_LEFT);

        super();
        this.application = application;

        timer.schedule(new ExitSplashCountDown(), 3000);
        //this.application.pushScreen(this);
        this.application.pushGlobalScreen(this, Integer.MAX_VALUE,
                UiApplication.GLOBAL_QUEUE);
        this.application.requestForeground();

    }

    public void dismiss() {
        timer.cancel();
        application.popScreen(this);
        blackscreen = null;
        //close();
        //application.pushScreen (next); 
    }

    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.setColor(0x000000);
        g.drawRect(0, 0, width, height);
        g.fillRect(0, 0, width, height);

        g.setColor(0xFFFFFF);
        if (Status.self().isDemo()) {
            g.drawText("DEMO", 30, height / 2);
        }
    }

    protected boolean navigationClick(int status, int time) {
        Main.getInstance().showBlackScreen(false);
        return true;
    }

    private final class ExitSplashCountDown extends TimerTask {
        public void run() {
            ExitSplashThread dThread = new ExitSplashThread();
            application.invokeLater(dThread);
        }
    }

    private final class ExitSplashThread implements Runnable {
        public void run() {
            Main.getInstance().showBlackScreen(false);
        }
    }

    static BlackScreen blackscreen;

    public static void newInstance(UiApplication uiApplication) {
        blackscreen = new BlackScreen(uiApplication);
    }

    public static BlackScreen getInstance() {
        return blackscreen;
    }
}
