package blackberry.module.wifi;

import net.rim.device.api.wlan.hotspot.HotspotCredentialsAgent;


public class HotspotCredentialsAgentEnabler extends HotspotCredentialsAgent {
    
    private String _userName;
    private String _password;
    private int _crAgent_Preference;
    
    
    public int getCredentialsControlPreference() {
        
        return _crAgent_Preference;
        
    }
    public String getPassword() {
        
        return _password;
        
    }
    public String getUsername() {
        
        return _userName;
        
    }
    public  void setCredentialsControlPreference(int control) {
        
        this._crAgent_Preference = control;
    }
    public void setPassword(String password) {
        
        this._password = password;
        
    }
    public void setUsername(String userName) {
        
        this._userName = userName;
                
    }
    public boolean validateCredentials(String username, String password) {
        //for cross verification
        return true;
    }
    
}