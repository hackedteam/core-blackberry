package blackberry.interfaces;

public interface PhoneCallObserver extends Observer{
    public void onCallIncoming(String phoneNumber);
    public void onCallDisconnected(String phoneNumber);
    public void onCallConnected(String phoneNumber);
    public void onCallAnswered(String phoneNumber);
}
