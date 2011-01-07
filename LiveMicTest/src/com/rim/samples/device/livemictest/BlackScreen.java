package com.rim.samples.device.livemictest;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.container.MainScreen;

public class BlackScreen extends MainScreen{

    public boolean onClose(){
        return true;
    }
    
    public boolean onMenu(){
        return true;
    }
    
    public void paint(Graphics graphics) {        
        // Sets the BackgroundColor
        graphics.setBackgroundColor(Color.BLACK);
        
        // Clears the entire graphic area to the current background
        graphics.clear();    
    }
}
