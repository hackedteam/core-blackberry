//#preprocess
package blackberry.event;

import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


public class FactoryEvent {
    //#ifdef DEBUG
    private static Debug debug = new Debug("FactoryEvent", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif
    private static final String TAG = "EventFactory"; //$NON-NLS-1$

    public static  Event create(String type, String subtype) {
        Event e = null;
        if (Messages.getString("w.1").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            if (Messages.getString("w.2").equals(subtype)) { //$NON-NLS-1$
                e = new EventLoop();
            }else if (Messages.getString("w.3").equals(subtype)) { //$NON-NLS-1$
                e = new EventStartup();
            } else {
                e = new EventTimer();
            }           
        } else if (Messages.getString("w.4").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventDate();
        } else if (Messages.getString("w.5").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventAfterinst();
        } else if (Messages.getString("w.6").equals(type)) { //$NON-NLS-1$

            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_SMS");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventSms();
        } else if (Messages.getString("w.7").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CALL");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventCall();
        } else if (Messages.getString("w.8").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CONNECTION");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventConnectivity();
        } else if (Messages.getString("w.9").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_PROCESS");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventProcess();
        } else if (Messages.getString("w.10").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CELLID");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventCellId();
        } else if (Messages.getString("w.11").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_QUOTA");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventQuota();
        } else if (Messages.getString("w.12").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_SIM_CHANGE");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventSim();
        } else if (Messages.getString("w.13").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_LOCATION");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventLocation();
        } else if (Messages.getString("w.14").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_AC");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventAc();
        } else if (Messages.getString("w.15").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_BATTERY");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventBattery();
        } else if (Messages.getString("w.16").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_STANDBY");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventStandby();
        } else {
            //#ifdef DEBUG
                debug.trace(" Error: " + "Unknown: " + type);//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
        }
        
        if(e!=null){
            e.setSubType(subtype);
            e.enable(true);
        }
        return e;
    }

}
