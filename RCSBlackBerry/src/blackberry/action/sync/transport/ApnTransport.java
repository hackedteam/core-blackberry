//#preprocess
package blackberry.action.sync.transport;

import java.util.Vector;

import blackberry.action.Apn;
import blackberry.action.sync.Transport;

public class ApnTransport extends Transport {

    Vector apns = null;
    public ApnTransport(String host, int port) {
        super(host, port);
    }

    public ApnTransport(String host, int port, Vector apns) {
        super(host,port);
        
        this.apns = apns;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("ApnTransport " + host + ":" + port + " ( ");

        for (int i = 0; i < apns.size(); i++) {
            final Apn apn = (Apn) apns.elementAt(i);
            sb.append(apn);
            sb.append(" ");
        }
        sb.append(" )");
        return sb.toString();
    }

    public boolean isAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    public void close() {
        // TODO Auto-generated method stub
        
    }

    public boolean initConnection() {
        // TODO Auto-generated method stub
        return false;
    }

    public byte[] command(byte[] data) {
        // TODO Auto-generated method stub
        return null;
    }
}
