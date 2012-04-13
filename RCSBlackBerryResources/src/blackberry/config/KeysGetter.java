//#preprocess
package blackberry.config;

public abstract class KeysGetter {
    // AES
    public abstract byte[] getLogKey(); 
    public abstract byte[] getBuildID();
    // CHALLENGE
    public abstract byte[] getProtoKey();
    public abstract byte[] getConfKey();
    public abstract  boolean isSeven();
    
    public static String log = "";
    protected byte[] keyFromString(final byte[] stringBytes) {
        try {
            int len = 16;
            byte[] array = new byte[len];
            String string = new String(stringBytes);
            
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
            //#endif
            return null;
        }
    }
}
