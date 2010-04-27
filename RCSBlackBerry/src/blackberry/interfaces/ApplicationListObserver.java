package blackberry.interfaces;

import java.util.Vector;

public interface ApplicationListObserver {
	void onApplicationListChange( final Vector startedList, final Vector stoppedList );
}
