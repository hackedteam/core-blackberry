//#preprocess
package blackberry.location;

import java.io.DataInputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.system.DeviceInfo;


public class QueryGoogle {

    //http://www.orangeapple.org/?p=82
    //http://forum.openhandsetdevelopers.com/google-maps-with-my-location-my-location-t19.html
    // zIQCOVn9EPA6Ca kronecker
    
   /* public boolean queryForCoordinates(DeviceInfo deviceInfo) {

        String baseURL = "http://www.google.com/glm/mmap";
        // Setup the connection
        HttpConnection httpConnection = null;
        OutputStream outputStream = null;
        DataInputStream inputStream = null;

        try {

        httpConnection = openConnection(baseURL);
        byte[] pd;
        if(deviceInfo.getCellID() <= 0 || deviceInfo.getLAC() <= 0)
        pd = PostData(0, 0, 3105, 20601, false);
        else
        pd = PostData(deviceInfo.getMCC(), deviceInfo.getMNC(), deviceInfo.getLAC(), deviceInfo.getCellID(), false);

        handler.processRequestHeaderForCoordinates(httpConnection);

        httpConnection.setRequestProperty("content-length", pd.length + "");
        outputStream = httpConnection.openOutputStream();
        outputStream.write(pd);
        outputStream.close();

        DataInputStream dis = httpConnection.openDataInputStream();

        // Read some prior data
        dis.readShort();
        dis.readByte();
        // Read the error-code
        int errorCode = dis.readInt();
        if (errorCode == 0) {
        double lat = (double) dis.readInt() / 1000000D;
        double lng = (double) dis.readInt() / 1000000D;
        // Read the rest of the data
        dis.readInt();
        dis.readInt();
        dis.readUTF();

        deviceInfo.setLatitude(lat);
        deviceInfo.setLongitude(lng);

        System.out.println("Lattitude --" + lat);
        System.out.println("Longitude --" + lng);

        } else {
         If the return code was not
        * valid or indicated an error,
        * we display a Sorry-Notification 
        return false;
        }
        return true;
        } catch (Exception e) {
        e.printStackTrace();
        return false;
        } finally {
        closeConnection(httpConnection, outputStream, inputStream);
        }

        }


        private byte[] PostData(int MCC, int MNC, int LAC, int CID, boolean shortCID) {
         The shortCID parameter follows heuristic experiences:
        * Sometimes UMTS CIDs are build up from the original GSM CID (lower 4 hex digits)
        * and the RNC-ID left shifted into the upper 4 digits.
        
        byte[] pd = new byte[]{
        0x00, 0x0e,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00,
        0x00, 0x00,
        0x00, 0x00,
        0x1b,
        0x00, 0x00, 0x00, 0x00, // Offset 0x11
        0x00, 0x00, 0x00, 0x00, // Offset 0x15
        0x00, 0x00, 0x00, 0x00, // Offset 0x19
        0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, // Offset 0x1f
        0x00, 0x00, 0x00, 0x00, // Offset 0x23
        0x00, 0x00, 0x00, 0x00, // Offset 0x27
        0x00, 0x00, 0x00, 0x00, // Offset 0x2b
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        0x00, 0x00, 0x00, 0x00
        };

        boolean isUMTSCell = ((long) CID > 65535);

        if (isUMTSCell) {
        System.out.println("UMTS CID. {0} " + (shortCID ? "Using short CID to resolve." : ""));
        } else {
        System.out.println("GSM CID given.");
        }
        if (shortCID) {
        CID &= 0xFFFF;
        }*/
}
