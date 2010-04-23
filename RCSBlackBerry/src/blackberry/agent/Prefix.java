package blackberry.agent;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

class Prefix {
    //#debug
    static Debug debug = new Debug("Prefix", DebugLevel.NOTIFY);

    static final int LEN = 4;

    static final byte TYPE_IDENTIFICATION = 1;
    static final byte TYPE_FILTER = 2;
    static final int TYPE_HEADER = 64;
    static final int TYPE_KEYWORD = 1;

    int length;
    byte type;

    int payloadStart;
    //public byte[] payload;

    private boolean valid;

    public Prefix(final byte[] conf, final int offset) {
        final DataBuffer databuffer = new DataBuffer(conf, offset, conf.length
                - offset, false);
        try {
            final byte bl0 = databuffer.readByte();
            final byte bl1 = databuffer.readByte();
            final byte bl2 = databuffer.readByte();
            type = databuffer.readByte();

            length = bl0 + (bl1 << 8) + (bl2 << 16);

            //payload = new byte[length];
            //databuffer.read(payload);

            payloadStart = offset + LEN;

            //#mdebug
            debug.trace("Token type: " + type + " len: " + length + " payload:"
                    + Utils.byteArrayToHex(conf, payloadStart, length));
            //#enddebug
            valid = true;
        } catch (final EOFException e) {
            //#debug
            debug.error("cannot parse Token: " + e);
            valid = false;
        }
    }

    public boolean isValid() {
        return valid;
    }
}
