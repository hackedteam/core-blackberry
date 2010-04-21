package blackberry.transfer;

public class Command {
    public int id;
    public byte[] payload;

    public Command(int commandId_, byte[] payload_) {
        this.id = commandId_;
        this.payload = payload_;
    }

    public int size() {
        if (payload != null) {
            return payload.length;
        } else {
            return 0;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (id > 0 && id < Proto.LASTTYPE) {
            sb.append(Proto.strings[id]);
            sb.append(": ");
        }
        sb.append(id);
        sb.append(" len:");
        sb.append(size());
        return sb.toString();
    }
}
