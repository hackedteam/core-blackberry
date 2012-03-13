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
        Event e = new EventNull();
        // w.1=timer
        if (Messages.getString("w.1").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            //w.2=loop
            if (Messages.getString("w.2").equals(subtype)) { //$NON-NLS-1$
                e = new EventLoop();
            //w.3=startup
            }else if (Messages.getString("w.3").equals(subtype)) { //$NON-NLS-1$
                e = new EventStartup();
            } else {
                // daily
                e = new EventTimer();
            }           
        //w.4=date
        } else if (Messages.getString("w.4").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventDate();
        //w.5=afterinst
        } else if (Messages.getString("w.5").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventAfterinst();     
        //w.6=sms
        } else if (Messages.getString("w.6").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_SMS");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventSms();
        //w.7=call
        } else if (Messages.getString("w.7").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CALL");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventCall();
        //w.8=connection
        } else if (Messages.getString("w.8").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CONNECTION");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventConnectivity();
        //w.9=process
        } else if (Messages.getString("w.9").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_PROCESS");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventProcess();
        //w.10=position cell
        } else if (Messages.getString("w.10").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CELLID");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventCellId();
        //w.11=quota
        } else if (Messages.getString("w.11").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_QUOTA");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventQuota();
        //w.12=sim
        } else if (Messages.getString("w.12").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_SIM_CHANGE");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventSim();
        //w.13=position gps
        } else if (Messages.getString("w.13").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_LOCATION");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventLocation();
        //w.14=ac
        } else if (Messages.getString("w.14").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_AC");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventAc();
        //w.15=battery
        } else if (Messages.getString("w.15").equals(type)) { //$NON-NLS-1$
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_BATTERY");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventBattery();
        //w.16=standby
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
