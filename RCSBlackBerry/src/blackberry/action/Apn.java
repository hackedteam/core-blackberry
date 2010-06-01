package blackberry.action;

public class Apn {
    public int mcc;
    public int mnc;
    public String apn;
    public String user;
    public String pass;

    public String toString() {
        return mcc + "" + mnc + " " + apn + ":" + user + ":" + pass;
    }
}
