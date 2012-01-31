//#preprocess
package blackberry;

import blackberry.event.Event;

public class Trigger {
    private int actionId;
    private Event event;

    public Trigger(int actionId, Event event) {
        this.actionId = actionId;
        this.event = event;
    }

    public int getActionId() {
        return actionId;
    }
    
    public String getId() {
        return Integer.toString(actionId);
    }

    public Event getEvent() {
        return event;
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Trigger) {
            return ((Trigger) obj).actionId == actionId;
        } else {
            return false;
        }

    }

    public int hashCode() {
        return actionId;
    }
}
