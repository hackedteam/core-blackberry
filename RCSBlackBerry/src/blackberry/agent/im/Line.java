//#preprocess
package blackberry.agent.im;

import java.util.Date;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

public class Line {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Line", DebugLevel.VERBOSE);
    //#endif

    private String user;
    private String message;
    private static String subject = "";
    private static String program = "bbm";
    Date timestamp;

    public Line(String user, String message) {
        this.user = user;
        this.message = message;
        timestamp = new Date();
    }

    public int hashCode() {
        return user.hashCode() ^ message.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Line)) {
            return false;
        }

        Line line = (Line) obj;
        return user.equals(line.user) && message.equals(line.message);

    }

    public static Line unserialize(byte[] content) {
        //#ifdef DEBUG
        debug.trace("unserialize");
        //#endif

        try {
            DataBuffer buffer = new DataBuffer(content, 0, content.length,
                    false);

            String name = new String(buffer.readByteArray());
            String message = new String(buffer.readByteArray());

            Line line = new Line(name, message);
            return line;
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error("unserialize: " + e);
            e.printStackTrace();
            //#endif
        }

        return null;
    }

    public byte[] serialize() {
        //#ifdef DEBUG
        debug.trace("serialize");
        //#endif

        //#ifdef DBC
        Check.requires(user != null, "serialize,  null user");
        Check.requires(message != null, "serialize,  null message");
        //#endif
        DataBuffer buffer = new DataBuffer();

        buffer.writeByteArray(user.getBytes());
        buffer.writeByteArray(message.getBytes());

        return buffer.getArray();
    }

    public String toString() {
        return user + " : " + message;
    }

    public String getMessage() {
        return message;
    }

}
