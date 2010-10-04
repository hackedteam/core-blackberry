package blackberry.location;

import javax.microedition.location.Location;

public interface LocationObserver {
    void newLocation(Location loc);
    void waitingForPoint(boolean b);
}
