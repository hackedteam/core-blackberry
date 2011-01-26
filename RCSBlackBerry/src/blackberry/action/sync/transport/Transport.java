//#preprocess
package blackberry.action.sync.transport;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

public abstract class Transport {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Transport", DebugLevel.VERBOSE);
    //#endif

    protected final int timeout = 3 * 60 * 1000;

    protected String baseurl;
    protected String suffix;

    public Transport(String baseurl) {
        //this.host = host;
        //this.port = port;
        this.baseurl = baseurl;
    }

    public String toString() {
        return "Transport " + getUrl();
    }

    public abstract boolean isAvailable();

    public abstract byte[] command(byte[] data) throws TransportException;

    //public abstract void initConnectionUrl();
    protected abstract String getSuffix();

    public abstract void close();

    public String getUrl() {
        return baseurl + ";ConnectionTimeout="+timeout+ getSuffix();
    }

}
