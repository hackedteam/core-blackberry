//#preprocess
package blackberry.interfaces;

public interface MmsObserver extends Observer {
    void onNewMms(final byte[] byteMessage, String address,
            final boolean incomin);

}