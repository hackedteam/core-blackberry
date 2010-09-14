package blackberry.interfaces;

public interface PhoneCallObserver extends Observer{
    public void onCallIncoming(int callId, String phoneNumber);
    public void onCallDisconnected(int callId, String phoneNumber);
    public void onCallConnected(int callId,String phoneNumber);
    public void onCallAnswered(int callId,String phoneNumber);
    public void onCallInitiated(int callId, String phoneNumber);
}
