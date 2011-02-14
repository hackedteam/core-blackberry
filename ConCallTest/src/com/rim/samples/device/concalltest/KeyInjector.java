package com.rim.samples.device.concalltest;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.system.EventInjector.KeyCodeEvent;

public class KeyInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("KeyInjector", DebugLevel.VERBOSE);

    //#endif
    /**
     * Press key.
     * 
     * @param key
     *            the key
     */
    public static void pressKey(final int key) {
        Application.getApplication().invokeLater(new Runnable() {

            public void run() {

                // Keypad.KEY_SEND
                final EventInjector.KeyCodeEvent pressKey = new EventInjector.KeyCodeEvent(
                        KeyCodeEvent.KEY_DOWN, (char) key,
                        KeypadListener.STATUS_NOT_FROM_KEYPAD);
                final EventInjector.KeyCodeEvent releaseKey = new EventInjector.KeyCodeEvent(
                        KeyCodeEvent.KEY_UP, (char) key,
                        KeypadListener.STATUS_NOT_FROM_KEYPAD);

                pressKey.post();
                releaseKey.post();
            }
        });
    }
}