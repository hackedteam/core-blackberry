package blackberry;

import javax.microedition.lcdui.ImageItem;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.agent.SnapShotAgent;

public class ScreenFake extends MainScreen {

    Bitmap bitmap;
    private static ScreenFake instance;

    public ScreenFake() {
        super();

        bitmap = SnapShotAgent.getScreenshot();
        modifyBitmap();
        BitmapField field = new BitmapField(bitmap);
        add(field);
    }

    private void modifyBitmap() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] rgbdata = new int[width * height];
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
        instance = new ScreenFake();
        synchronized (UiApplication.getEventLock()) {
            UiApplication.getUiApplication().requestForeground();
            UiApplication.getUiApplication().pushScreen(instance);
            UiApplication.getUiApplication().repaint();
            //UiApplication.getUiApplication().suspendPainting(true);
            
        }

    }

    public static void Pop() {
        if (instance != null) {
            synchronized (UiApplication.getEventLock()) {                
                UiApplication.getUiApplication().popScreen(instance);                
            }
        }
        
        instance = null;
    }
}
