package com.rim.samples.device.bbminjectdemo;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
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
	        Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();        
	        activeScreen.getUiEngine().suspendPainting(true);
	        BBMInjectDemo.getApplication().requestForeground();
	    }
	    
	    protected void onExposed(){  
	        debug.info("Exposed");
	        BBMInjectDemo.getUiApplication().popScreen(this);
	        Screen activeScreen = BBMInjectDemo.getUiApplication().getActiveScreen();
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
	    