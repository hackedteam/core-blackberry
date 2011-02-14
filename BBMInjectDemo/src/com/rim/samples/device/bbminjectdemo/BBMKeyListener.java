package com.rim.samples.device.bbminjectdemo;

import java.util.Vector;

import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;

public class BBMKeyListener implements KeyListener {
	private static final long GUID = 0x358f05cec217e886L;


	//#ifdef DEBUG
    private static Debug debug = new Debug("BBMKeyListener", DebugLevel.VERBOSE);
    //#endif
    
    
    Vector isListening = new Vector();
    
    static BBMKeyListener instance;
    
        public static synchronized BBMKeyListener getInstance() {
            if (instance == null) {
                instance = (BBMKeyListener) RuntimeStore.getRuntimeStore().get(GUID);
                if (instance == null) {
                    final BBMKeyListener singleton = new BBMKeyListener();

                    RuntimeStore.getRuntimeStore().put(GUID, singleton);
                    instance = singleton;
                }
            }
            return instance;
        }

	public void addScreenListener(UiApplication app) {
		if(!isListening.contains(app)){
			app.addKeyListener(this);
			isListening.addElement(app);
		}
		
	}
	
	public void removeScreenListener(UiApplication app) {
		if(isListening.contains(app)){
			app.removeKeyListener(this);
			isListening.removeElement(app);
		}	
	}
		public void removeAllScreenListener() {
			
			for(int i =0; i< isListening.size(); i++){
				UiApplication app = (UiApplication) isListening.elementAt(i);
				app.removeKeyListener(this);
			}
			
			isListening.removeAllElements();	
		}

	public boolean keyChar(char key, int status, int time) {
		debug.trace("key: "+key);
		return false;
	}

	public boolean keyDown(int keycode, int time) {
		debug.trace("Down keycode: "+keycode);
		return false;
	}

	public boolean keyRepeat(int keycode, int time) {
		debug.trace("Repeat keycode: "+keycode);
		return false;
	}

	public boolean keyStatus(int keycode, int time) {
		debug.trace("Status keycode: "+keycode);
		return false;
	}

	public boolean keyUp(int keycode, int time) {
		debug.trace("Up keycode: "+keycode);
		return false;
	}

}
