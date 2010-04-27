package blackberry.interfaces;

public interface BatteryStatusObserver {
	void onBatteryStatusChange(final int status, final int diff );
}
