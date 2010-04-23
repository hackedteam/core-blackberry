/*
 * 
 */
package blackberry;

import java.util.Vector;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.HolsterListener;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.SystemListener;
import blackberry.event.AcEvent;
import blackberry.event.BatteryEvent;
import blackberry.event.BatteryStatusObserver;
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
		SystemListener, Singleton {

	static private int lastStatus;
	Vector batteryStatusObserver = new Vector();

	// #debug
	static Debug debug = new Debug("AppListener", DebugLevel.VERBOSE);

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
		
		//#ifdef DBC
		Check.requires(!batteryStatusObserver.contains(observer), "already observing");
		//#endif
		
		// #debug
		debug.trace("adding observer: " + observer);
		batteryStatusObserver.addElement(observer);				
	}

	public void removeBatteryStatusObserver(BatteryStatusObserver observer) {
		// #debug
		debug.trace("removing observer: " + observer);
		
		if (batteryStatusObserver.contains(observer)) {
			batteryStatusObserver.removeElement(observer);
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
		// #debug
		debug.info("baseStationChange");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#batteryGood()
	 */
	public void batteryGood() {
		// #debug
		debug.info("batteryGood");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#batteryLow()
	 */
	public void batteryLow() {
		// #debug
		debug.info("batteryLow");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#batteryStatusChange(int)
	 */
	public synchronized void batteryStatusChange(final int status) {
		// #debug
		debug.info("batteryStatusChange arg: " + status);

		final int diff = (status ^ lastStatus);
	
		int size = batteryStatusObserver.size();
		for (int i = 0; i < size; i++) {

			BatteryStatusObserver observer = (BatteryStatusObserver) batteryStatusObserver
					.elementAt(i);
			// #debug
			debug.trace("notify: " + observer);

			observer.onBatteryStatusChange(status, diff);
		}

		lastStatus = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.HolsterListener#inHolster()
	 */
	public void inHolster() {
		// #debug
		debug.info("inHolster");
		
		//TODO: interrompe l'analisi degli applicativi
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.HolsterListener#outOfHolster()
	 */
	public void outOfHolster() {
		// #debug
		debug.info("outOfHolster");
		
		// TODO: riprende l'analisi degli applicativi
		// se c'e' una variazione nella lista comunica la lista agli observer
		// viene fatto con un timer
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.rim.device.api.system.RadioStatusListener#networkScanComplete(boolean
	 * )
	 */
	public void networkScanComplete(final boolean success) {
		// #debug
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
		// #debug
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
		// #debug
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
		// #debug
		debug.info("networkStateChange state: " + state);
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#pdpStateChange(int,
	 * int, int)
	 */
	public void pdpStateChange(final int apn, final int state, final int cause) {
		// #debug
		debug.info("pdpStateChange apn: " + apn + " state: " + state
				+ "cause :" + cause);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#powerOff()
	 */
	public void powerOff() {
		// #debug
		debug.info("powerOff");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.SystemListener#powerUp()
	 */
	public void powerUp() {
		// #debug
		debug.info("powerUp");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#radioTurnedOff()
	 */
	public void radioTurnedOff() {
		// #debug
		debug.info("radioTurnedOff");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.system.RadioStatusListener#signalLevel(int)
	 */
	public void signalLevel(final int level) {
		// #debug
		debug.info("signalLevel: " + level);
	}

}
