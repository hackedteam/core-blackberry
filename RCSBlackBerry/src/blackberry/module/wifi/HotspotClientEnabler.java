package blackberry.module.wifi;

import net.rim.device.api.system.WLANInfo.WLANAPInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.wlan.hotspot.HotspotAuthenticationAgent;
import net.rim.device.api.wlan.hotspot.HotspotClient;
import net.rim.device.api.wlan.hotspot.HotspotCredentialsAgent;
import net.rim.device.api.wlan.hotspot.HotspotException;
import net.rim.device.api.wlan.hotspot.HotspotInfo;

public class HotspotClientEnabler extends HotspotClient {

    private StringBuffer wLanInfoBuff;

    public HotspotClientEnabler(HotspotCredentialsAgent hotspotCrAgent,
            HotSpotAuthAgentEnabler hotSpotAuthAgent, int supportednetworkTypes) {

        super(hotspotCrAgent, hotSpotAuthAgent, supportednetworkTypes);

    }

    public String getClientName() {
        return "Any Name";
    }

    public HotspotInfo[] getSupportedNetworks(WLANAPInfo[] networks) {
        // here u can read the WLANInfo.WLANAPInfo[], and try to get the open n/w, the one which you want to connect
        // and return this as a first element of the HotspotInfo array
        // you can read all properties using getHotSpotInfo(...) and can set additional properties to
        // the HotspotInfo
        String text = "Called \n";
        HotspotInfo[] hotspotInfo = new HotspotInfo[networks.length];
        for (int i = 0; i < networks.length; i++) {
            text += networks[i].getBSSID() + "\n";
            text += networks[i].getSignalLevel() + "\n";
            text += networks[i].toString() + "\n ";
            hotspotInfo[i] = new HotspotInfo(networks[i], false);
        }

        final String shownText = text;
        /*UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                resultText.setText(shownText);
            }
        });*/
        return hotspotInfo; //return only open networks        
    }

    public void getHotSpotInfo(WLANAPInfo wLanInfo) {
        wLanInfoBuff = new StringBuffer();
        String ssid = "";
        if (wLanInfo != null) {
            wLanInfoBuff.append("APChannel*****" + wLanInfo.getAPChannel());
            wLanInfoBuff.append("**BSSID*****" + wLanInfo.getBSSID());
            wLanInfoBuff.append("**DataRate*****" + wLanInfo.getDataRate());
            wLanInfoBuff.append("**Profile Name*****"
                    + wLanInfo.getProfileName());
            wLanInfoBuff.append("**RadioBand*****" + wLanInfo.getRadioBand());
            wLanInfoBuff.append("**SecurityCategory*****"
                    + wLanInfo.getSecurityCategory());
            wLanInfoBuff.append("**SignalLevel*****"
                    + wLanInfo.getSignalLevel());
            ssid = wLanInfo.getSSID();
            wLanInfoBuff.append("**SSID*****" + ssid.toString());
        }
    }

}