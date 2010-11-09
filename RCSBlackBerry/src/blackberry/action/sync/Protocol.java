//#preprocess
package blackberry.action.sync;

import blackberry.transfer.ProtocolException;

public abstract class Protocol {
    protected Transport transport;

    public boolean init(Transport transport) {
        this.transport = transport;
        return transport.initConnection();
    }

    public abstract boolean start() throws ProtocolException;

}
