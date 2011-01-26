//#preprocess
package blackberry.action.sync.transport;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class Wap2Transport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug("Wap2Transport", DebugLevel.VERBOSE);

    //#endif

    public Wap2Transport(String host) {
        super(host);
    }

    public boolean isAvailable() {
        String uid = getUid();
        return uid != null;
    }

    private String getUid() {
        String uid = null;
        final ServiceBook sb = ServiceBook.getSB();
        final ServiceRecord[] records = sb.findRecordsByCid("WPTCP");
        for (int i = 0; i < records.length; i++) {
            if (records[i].isValid() && !records[i].isDisabled()) {
                if (records[i].getUid() != null
                        && records[i].getUid().length() != 0) {
                    if ((records[i].getCid().toLowerCase().indexOf("wptcp") != -1)
                            && (records[i].getUid().toLowerCase().indexOf(
                                    "wifi") == -1)
                            && (records[i].getUid().toLowerCase()
                                    .indexOf("mms") == -1)) {
                        uid = records[i].getUid();
                        break;
                    }
                }
            }
        }
        return uid;
    }

    protected String getSuffix() {
        String uid = getUid();
        if (uid != null) {
            // WAP2 Connection
            return ";deviceside=true;ConnectionUID=" + uid;
        }

        return "";
    }

    public String toString() {
        return "Wap2Transport " + host;
    }
}
