package com.ht.rcs.blackberry.transfer;

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
        return id + " len:" + size();
    }
}
