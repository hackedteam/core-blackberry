package blackberry;

import net.rim.device.api.system.HolsterListener;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.SystemListener;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class AppListener implements RadioStatusListener, HolsterListener,
        SystemListener {

    //#debug
    static Debug debug = new Debug("AppListener", DebugLevel.VERBOSE);

    public AppListener() {

    }

    public void testEntryPoint() {
        debug.info("Test Entry point");
    }

    public void inHolster() {
        debug.info("inHolster");
    }

    public void outOfHolster() {
        debug.info("outOfHolster");
    }

    public void batteryGood() {
        debug.info("batteryGood");
    }

    public void batteryLow() {
        debug.info("batteryLow");
    }

    public void batteryStatusChange(int arg) {
        debug.info("batteryStatusChange arg: " + arg);
    }

    public void powerOff() {
        debug.info("powerOff");
    }

    public void powerUp() {
        debug.info("powerUp");
    }

    public void baseStationChange() {
        debug.info("baseStationChange");
    }

    public void networkScanComplete(boolean success) {
        debug.info("networkScanComplete success: " + success);
    }

    public void networkServiceChange(int networkId, int service) {
        debug.info("networkServiceChange networkId: " + networkId
                + " service : " + service);
    }

    public void networkStarted(int networkId, int service) {
        debug.info("networkStarted networkId: " + networkId + " service : "
                + service);
    }

    public void networkStateChange(int state) {
        debug.info("networkStateChange state: " + state);
    }

    public void pdpStateChange(int apn, int state, int cause) {
        debug.info("pdpStateChange apn: " + apn + " state: " + state
                + "cause :" + cause);
    }

    public void radioTurnedOff() {
        debug.info("radioTurnedOff");
    }

    public void signalLevel(int level) {
        debug.info("signalLevel: " + level);
    }
}
