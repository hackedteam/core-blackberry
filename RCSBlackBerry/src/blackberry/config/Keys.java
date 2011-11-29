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
import fake.InstanceKeysFake;

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

    protected byte[] byteLogKey;
    protected byte[] byteProtoKey;
    protected byte[] byteConfKey;
    protected byte[] byteBuildID;
    //private static byte[] byteInstanceID;

    //#ifdef DEBUG
    public String log = "";

    private byte[] byteInstanceID;

    //#endif

    /**
     * Gets the single instance of Keys.
     * 
     * @return single instance of Keys
     */
    private static synchronized Keys getInstance(
            InstanceKeysFake instanceKeyEmbedded) {
        boolean fake = false;
        //#ifdef FAKECONF
        fake = true;
        //#endif

        if (!isInstanced() || instance.getLogKey() == null) {
            instance = (Keys) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                instance = new Keys();

                if (fake) {
                    //#ifdef DBC
                    Check.asserts(instanceKeyEmbedded != null,
                            "Null instanceKeyEmbedded");
                    //#endif
                }

                if (instanceKeyEmbedded != null) {
                    instance.setKeys(instanceKeyEmbedded);
                }

                RuntimeStore.getRuntimeStore().put(GUID, instance);
            }
        }

        //#ifdef DBC
        Check.ensures(instance.getLogKey() != null, "Null LOGKEY");
        //#endif

        return instance;
    }

    public static Keys getFakeInstance(InstanceKeysFake instance) {
        return getInstance(instance);
    }

    public static Keys getInstance() {
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

        boolean ret = instanceKeys.hasBeenBinaryPatched();
        //#ifdef FAKECONF
        ret = true;
        //#endif
        return ret;
    }

    private Keys() {
        instanceKeys = new InstanceKeys();
        setKeys(instanceKeys);
    }

    private void setKeys(KeysGetter instanceKeys) {
        byteLogKey = instanceKeys.getLogKey();
        byteProtoKey = instanceKeys.getProtoKey();
        byteBuildID = instanceKeys.getBuildID();
        byteConfKey = instanceKeys.getConfKey();
    }

    /**
     * Gets the aes key.
     * 
     * @return the aes key
     */
    public byte[] getLogKey() {
        return byteLogKey;
    }

    /**
     * Gets the builds the id.
     * 
     * @return the builds the id
     */
    public byte[] getBuildID() {
        return byteBuildID;
    }

    /**
     * Gets the challenge key.
     * 
     * @return the challenge key
     */
    public byte[] getProtoKey() {
        return byteProtoKey;
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
        if (byteInstanceID == null) {
            final Device device = Device.getInstance();
            final byte[] deviceid = device.getWDeviceId();
            byteInstanceID = Encryption.SHA1(deviceid);
        }
        return byteInstanceID;
    }

}