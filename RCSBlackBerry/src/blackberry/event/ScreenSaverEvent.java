package blackberry.event;

public class ScreenSaverEvent extends Event {

    public ScreenSaverEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_SCREENSAVER, actionId, confParams);
    }

    protected boolean parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

    protected void actualRun() {
        // TODO Auto-generated method stub

    }

}
