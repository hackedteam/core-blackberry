package blackberry.module.wifi;

import net.rim.device.api.wlan.hotspot.HotspotAuthenticationAgent;
import net.rim.device.api.wlan.hotspot.HotspotInfo;

public class HotSpotAuthAgentEnabler extends HotspotAuthenticationAgent {
    
    public void login(HotspotInfo hotspot) {
       
    }
    public void logout(HotspotInfo hotspot) {
       
    }
    public void cancelLogin(HotspotInfo hotspot) {
       
    }
    public int getSessionState() {
      
        int sessionState = this.getSessionState();
        return sessionState;
    }
    
    public void probeNetwork(HotspotInfo hotspot){
       
    }
}