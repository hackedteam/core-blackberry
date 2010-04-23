package blackberry.event;

public interface BatteryStatusObserver {
	void onBatteryStatusChange(final int status, final int diff );
}
