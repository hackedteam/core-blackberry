package blackberry;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BlackScreen extends MainScreen{
    //#ifdef DEBUG
      private static Debug debug = new Debug("BlackScreen", DebugLevel.VERBOSE);
      //#endif
      
      public BlackScreen(){
          setTitle("Black");
      }
      
      protected void onObscured(){
          debug.info("Obscured");
         /* Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();        
          activeScreen.getUiEngine().suspendPainting(true);
          UiApplication.getApplication().requestForeground();*/
      }
      
      protected void onExposed(){  
          debug.info("Exposed");
         /* UiApplication.getUiApplication().popScreen(this);
          Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();
          if (activeScreen.getUiEngine().isPaintingSuspended()) {
              activeScreen.getUiEngine().suspendPainting(false);
          }
          activeScreen.doPaint();*/
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