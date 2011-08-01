package com.rim.samples.device.bbminjectdemo;

import java.util.Vector;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.LED;
import net.rim.device.api.ui.UiApplication;

public class Debug {

	public static final int COLOR_BLUE_LIGHT = 0x00C8F0FF; // startRecorder
	public static final int COLOR_RED = 0x00ff1029; // error
	public static final int COLOR_ORANGE = 0x00ff5e1b; // crysis
	public static final int COLOR_GREEN = 0x001fbe1a;
	public static final int COLOR_GREEN_LIGHT = 0x0044DC4C; // evidence
	public static final int COLOR_YELLOW = 0x00f3f807; // sync

	public static long loggerEventId = 0x98f217b7dbfd6ae4L;
	private static LocalScreen liveMicScreen;
	private String base;
	private int level;

	public Debug(String string, int level) {
		base = string;
		EventLogger.register(loggerEventId, "BBMINJECT",
				EventLogger.VIEWER_STRING);
		EventLogger.setMinimumLevel(EventLogger.DEBUG_INFO);
		this.level = level;

		logToEvents("Start Debug: " + string, EventLogger.INFORMATION);
	}

	public void trace(String string) {

		if (level < EventLogger.DEBUG_INFO) {
			return;
		}

		System.out.println(string);

		if (buffering > 0) {
			if (bufferlevel >= EventLogger.DEBUG_INFO) {
				logToBuffer(string);
			}
		} else {
			logToEvents(base + " " + string, EventLogger.INFORMATION);
			logToScreen(base + " DEBUG " + string);
		}
	}

	public void info(String string) {

		if (level < EventLogger.INFORMATION) {
			return;
		}

		System.out.println(string);

		if (buffering > 0) {
			logToBuffer(string);
		} else {
			logToEvents(base + " " + string, EventLogger.INFORMATION);
			logToScreen(base + " INFO " + string);
		}
	}

	public void warn(String string) {
		if (level < EventLogger.WARNING) {
			return;
		}

		System.out.println(string);
		if (buffering > 0) {
			logToBuffer(string);
		} else {
			logToEvents(base + " " + string, EventLogger.WARNING);
			logToScreen(base + " WARN " + string);
		}
	}

	public void error(String string) {
		if (level < EventLogger.ERROR) {
			return;
		}

		System.out.println(string);
		if (buffering > 0) {
			logToBuffer(string);
		} else {
			logToEvents(base + " " + string, EventLogger.ERROR);
			logToScreen(base + " ERROR " + string);
		}
	}

	private void logToBuffer(String string) {

		buffer.addElement(string);
	}

	private void logToScreen(final String string) {
		//#ifdef SCREENLOGGER
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (liveMicScreen != null) {
					// synchronized (Application.getEventLock()) {
					System.out.println("log to screen");
					liveMicScreen.addText(string);
					// }
				}
			}
		});
		// #endif
	}

	private void logToEvents(final String logMessage, int level) {
		//#ifdef EVENTLOGGER
		// EventLogger.register(loggerEventId, "BBB",
		// EventLogger.VIEWER_STRING);
		String s = logMessage;
		boolean others = false;

		if (logMessage.length() > 500) {

			s = logMessage.substring(0, 500);
			others = true;

		}
		if (!EventLogger.logEvent(loggerEventId, s.getBytes(), level)) {

			System.out.println("cannot write to EventLogger");
		}

		if (others) {
			logToEvents(logMessage.substring(500), level);
		}

		// #endif
	}

	public static void setScreen(LocalScreen _liveMicScreen) {
		liveMicScreen = _liveMicScreen;
	}

	int buffering = 0;
	Vector buffer;
	int bufferlevel;

	public void startBuffering(int level) {

		buffer = new Vector();
		synchronized (buffer) {
			buffering += 1;
		}

		bufferlevel = level;
	}

	public void stopBuffering() {

		if (buffer == null) {
			return;
		}

		synchronized (buffer) {
			buffering -= 1;
			if (buffering > 0) {
				return;
			}
		}

		Application.getApplication().invokeLater(new Runnable() {

			public void run() {
				for (int i = 0; i < buffer.size(); i += 10) {

					String tens = "";

					for (int j = i; j < i + 10 && j < buffer.size(); j++) {

						tens += (String) buffer.elementAt(j) + "\n";
					}

					// dovrebbe loggare a livello bufferlevel...
					logToEvents("\n" + tens, EventLogger.INFORMATION);
					// logToScreen(tens);
				}

			}
		});

	}

	public void ledStart(int color) {
		try {
			LED.setConfiguration(LED.LED_TYPE_STATUS, 1000, 1000,
					LED.BRIGHTNESS_12);
			LED.setColorConfiguration(1000, 1000, color);
			LED.setState(LED.STATE_BLINKING);

			LED.setState(LED.STATE_OFF);

		} catch (final Exception ex) {

		}
	}

	public void ledStop() {
		try {
			LED.setState(LED.STATE_OFF);
		} catch (final Exception ex) {

		}
	}

    public void led(int color) {
        ledStart(color);
        ledStop();
        
    }

}
