package blackberry;

import javax.microedition.lcdui.ImageItem;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.agent.SnapShotAgent;

public class ScreenFake extends MainScreen {

    Bitmap bitmap = SnapShotAgent.getScreenshot();
    private static ScreenFake instance;

    public ScreenFake() {
        super();

       BitmapField field = new BitmapField(bitmap);

        add(field);
    }

    public boolean onClose() {
        return true;
    }

    public static void Push() {
        instance = new ScreenFake();
        UiApplication.getUiApplication().pushModalScreen(instance);
    }

    public static void Pop() {
        if (instance != null) {
            UiApplication.getUiApplication().popScreen(instance);
        }
        instance = null;
    }
}
