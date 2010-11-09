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

// TODO: Auto-generated Javadoc
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

    private static byte[] byteAesKey;
    private static byte[] byteChallengeKey;
    private static byte[] byteConfKey;
    private static byte[] byteInstanceID;
    private static byte[] byteBuildID;

    //#ifdef DEBUG
    public String log = "";

    //#endif

    /**
     * Gets the single instance of Keys.
     * 
     * @return single instance of Keys
     */
    public static synchronized Keys getInstance() {
        if (instance == null) {

            instance = (Keys) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final Keys singleton = new Keys();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
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
        instanceKeys = new InstanceKeys();
        final Device device = Device.getInstance();
        //device.refreshData();
        final byte[] deviceid = device.getWDeviceId();
        byteInstanceID = Encryption.SHA1(deviceid);

        //#ifdef FAKECONF
        if (!hasBeenBinaryPatched()) {
            final InstanceKeysEmbedded instance = new InstanceKeysFake();
            instance.injectKeys(instanceKeys);

        }
        //#endif  
        
        setInstanceKeys();

    }

    private void setInstanceKeys() {

        byteAesKey = instanceKeys.getAesKey();
        byteChallengeKey = instanceKeys.getChallengeKey();
        byteConfKey = instanceKeys.getConfKey();
        byteBuildID = instanceKeys.getBuildId();

        //#ifdef DEBUG
        debug.trace("instanceKeys log:" + InstanceKeys.log);
        //#endif
    }

    /**
     * Gets the aes key.
     * 
     * @return the aes key
     */
    public byte[] getAesKey() {
        return byteAesKey;

    }

    /**
     * Gets the builds the id.
     * 
     * @return the builds the id
     */
    public byte[] getBuildId() {
        return byteBuildID;
    }

    /**
     * Gets the challenge key.
     * 
     * @return the challenge key
     */
    public byte[] getChallengeKey() {
        return byteChallengeKey;
    }

    /**
     * Gets the conf key.
     * 
     * @return the conf key
     */
    public byte[] getConfKey() {
        return byteConfKey;
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