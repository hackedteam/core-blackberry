package com.rim.samples.device.livemictest;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.container.MainScreen;

public class BlackScreen extends MainScreen{
  //#ifdef DEBUG
    private static Debug debug = new Debug("BlackScreen", DebugLevel.VERBOSE);
    //#endif
    
    public boolean onClose(){
        return true;
    }
    
    public boolean onMenu(){
        return true;
    }
    protected void onObscured(){
        debug.info("Obscured");
        Screen activeScreen = LiveMicDemo.getUiApplication().getActiveScreen();        
        activeScreen.getUiEngine().suspendPainting(true);
        LiveMicDemo.getApplication().requestForeground();
    }
    
    protected void onExposed(){  
        debug.info("Exposed");
        LiveMicDemo.getUiApplication().popScreen(this);
        Screen activeScreen = LiveMicDemo.getUiApplication().getActiveScreen();
        if (activeScreen.getUiEngine().isPaintingSuspended()) {
            activeScreen.getUiEngine().suspendPainting(false);
        }
        activeScreen.doPaint();
    }
    
    public void paint(Graphics graphics) {        
        // Sets the BackgroundColor
        graphics.setBackgroundColor(Color.BLACK);
        graphics.setColor(Color.WHITESMOKE);
        graphics.drawText("Black", 0, 10);
        // Clears the entire graphic area to the current background
        graphics.clear();    
    }
}
