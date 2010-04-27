/*
 * 
 */
package blackberry;

import java.util.Vector;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.HolsterListener;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.system.SystemListener2;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.interfaces.Singleton;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving app events. The class that is interested
 * in processing a app event implements this interface, and the object created
 * with that class is registered with a component using the component's
 * <code>addAppListener<code> method. When
 * the app event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see AppEvent
 */
public class AppListener implements RadioStatusListener, HolsterListener,
		SystemListener, SystemListener2, Singleton {

	// #debug
	static Debug debug = new Debug("AppListener", DebugLevel.VERBOSE);
	
	static private int lastStatus;
	Vector batteryStatusObservers = new Vector();
	Vector applicationListObservers = new Vector();

	//private Timer applicationTimer;
	
	static AppListener instance;

	/**
	 * Instantiates a new app listener.
	 */
	private AppListener() {
		lastStatus = DeviceInfo.getBatteryStatus();		
	}

	public synchronized static AppListener getInstance() {
		if (instance == null) {
			instance = new AppListener();
		}
		return instance;
	}

	public synchronized void addBatteryStatusObserver(
			BatteryStatusObserver observer) {

		// #ifdef DBC
		Check.requires(!batteryStatusObservers.contains(observer),
				"already observing");
		// #endif

		// #debug debug
		debug.trace("adding observer: " + observer);
		batteryStatusObservers.addElement(observer);
	}

	public synchronized void addApplicationListObserver(
			ApplicationListObserver observer) {

		// #ifdef DBC
		Check.requires(!applicationListObservers.contains(observer),
				"already observing");
		// #endif

		// #debug debug
		debug.trace("adding observer: " + observer);
		applicationListObservers.addElement(observer);
	}

	public synchronized void removeBatteryStatusObserver(BatteryStatusObserver observer) {
		// #debug debug
		debug.trace("removing observer: " + observer);

		if (batteryStatusObservers.contains(observer)) {
			batteryStatusObservers.removeElement(observer);
		} else {
			// #debug
			debug.error("removing observer not present: " + observer);
		}
	}

	public synchronized void removeApplicationListObserver(ApplicationListObserver observer) {
		// #debug debug
		debug.trace("removing observer: " + observer);

		if (applicationListObservers.contains(observer)) {
			applicationListObservers.removeElement(observer);
		} else {
			// #debug
			debug.error("removing observer not present: " + observer);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#baseStationChange()
	 */
	public void baseStationChange() {
		// #debug info
		debug.info("baseStationChange");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#batteryGood()
	 */
	public void batteryGood() {
		// #debug info
		debug.info("batteryGood");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#batteryLow()
	 */
	public void batteryLow() {
		// #debug info
		debug.info("batteryLow");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#batteryStatusChange(int)
	 */
	public synchronized void batteryStatusChange(final int status) {
		// #debug info
		debug.info("batteryStatusChange arg: " + status);

		final int diff = (status ^ lastStatus);

		int size = batteryStatusObservers.size();
		for (int i = 0; i < size; i++) {

			BatteryStatusObserver observer = (BatteryStatusObserver) batteryStatusObservers
					.elementAt(i);
			// #debug debug
			debug.trace("notify: " + observer);

			observer.onBatteryStatusChange(status, diff);
		}

		lastStatus = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#batteryStatusChange(int)
	 */
	public void applicationListChange(final Vector startedList,
			final Vector stoppedList) {
		// #debug info
		debug.info("applicationListChange start: " + startedList.size()
				+ " stopped: " + stoppedList.size());

		int size = applicationListObservers.size();
		for (int i = 0; i < size; i++) {

			ApplicationListObserver observer = (ApplicationListObserver) applicationListObservers
					.elementAt(i);
			// #debug debug
			debug.trace("notify: " + observer);

			observer.onApplicationListChange(startedList, stoppedList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.HolsterListener#inHolster()
	 */
	public void inHolster() {
		// #debug info
		debug.info("inHolster");

		//stopApplicationTimer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.HolsterListener#outOfHolster()
	 */
	public void outOfHolster() {
		// #debug info
		debug.info("outOfHolster");

		// TODO: riprende l'analisi degli applicativi
		// se c'e' una variazione nella lista comunica la lista agli observer
		// viene fatto con un timer

		//startApplicationTimer();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.rim.device.api.system.RadioStatusListener#networkScanComplete(boolean
	 * )
	 */
	public void networkScanComplete(final boolean success) {
		// #debug info
		debug.info("networkScanComplete success: " + success);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.rim.device.api.system.RadioStatusListener#networkServiceChange(int,
	 * int)
	 */
	public void networkServiceChange(final int networkId, final int service) {
		// #debug info
		debug.info("networkServiceChange networkId: " + networkId
				+ " service : " + service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#networkStarted(int,
	 * int)
	 */
	public void networkStarted(final int networkId, final int service) {
		// #debug info
		debug.info("networkStarted networkId: " + networkId + " service : "
				+ service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.rim.device.api.system.RadioStatusListener#networkStateChange(int)
	 */
	public void networkStateChange(final int state) {
		// #debug info
		debug.info("networkStateChange state: " + state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#pdpStateChange(int,
	 * int, int)
	 */
	public void pdpStateChange(final int apn, final int state, final int cause) {
		// #debug info
		debug.info("pdpStateChange apn: " + apn + " state: " + state
				+ "cause :" + cause);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#powerOff()
	 */
	public void powerOff() {
		// #debug info
		debug.info("powerOff");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#powerUp()
	 */
	public void powerUp() {
		// #debug info
		debug.info("powerUp");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#radioTurnedOff()
	 */
	public void radioTurnedOff() {
		// #debug info
		debug.info("radioTurnedOff");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#signalLevel(int)
	 */
	public void signalLevel(final int level) {
		// #debug info
		debug.info("signalLevel: " + level);
	}

	public void backlightStateChange(boolean on) {
		// #debug info
		debug.info("backlightStateChange: " + on);

	}

	public void cradleMismatch(boolean mismatch) {
		// #debug info
		debug.info("cradleMismatch: " + mismatch);
	}

	public void fastReset() {
		// #debug info
		debug.info("fastReset");
	}

	public void powerOffRequested(int reason) {
		// #debug info
		debug.info("powerOffRequested: " + reason);

	}

	public void usbConnectionStateChange(int state) {
		// #debug info
		debug.info("usbConnectionStateChange: " + state);
	}

}
