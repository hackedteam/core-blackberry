package blackberry.event;

import blackberry.config.ConfEvent;

public class EventLoop extends Event {


    protected boolean parse(ConfEvent event) {
        
        return true;
    }


    protected void actualGo() {
        
    }


    protected void actualStart() {
        onEnter();
    }


    protected void actualStop() {
        onExit();
    }

}
