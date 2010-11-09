//#preprocess
package blackberry.action.sync;

public abstract class Protocol {
    protected Transport transport;

    public void init(Transport transport) {
        this.transport = transport;
    }

    public abstract boolean start();

}
