/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.transfer
 * File         : Command.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.transfer;

// TODO: Auto-generated Javadoc
/**
 * The Class Command.
 */
public class Command {
    public int id;
    public byte[] payload;

    /**
     * Instantiates a new command.
     * 
     * @param commandId_
     *            the command id_
     * @param payload_
     *            the payload_
     */
    public Command(final int commandId_, final byte[] payload_) {
        id = commandId_;
        payload = payload_;
    }

    /**
     * Size.
     * 
     * @return the int
     */
    public final int size() {
        if (payload != null) {
            return payload.length;
        } else {
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        final StringBuffer sb = new StringBuffer();
        if (id > 0 && id < Proto.LASTTYPE) {
            sb.append(Proto.STRINGS[id]);
            sb.append(": ");
        }
        sb.append(id);
        sb.append(" len:");
        sb.append(size());
        return sb.toString();
    }
}
