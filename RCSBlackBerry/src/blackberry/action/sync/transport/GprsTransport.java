package blackberry.action.sync.transport;

import blackberry.action.sync.Transport;

public class GprsTransport extends Transport {

    public GprsTransport(String host, int port) {
        super(host, port);
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
