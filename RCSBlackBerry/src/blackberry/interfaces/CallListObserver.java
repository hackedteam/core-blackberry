package blackberry.interfaces;

import java.util.Date;

public interface CallListObserver extends Observer{
    public void callLogAdded(String number, String name, Date date, int duration, boolean outgoing, boolean missed);
    
}
