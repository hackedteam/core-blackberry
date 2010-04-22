package blackberry;
public class Version {
    public static final int Major = 0;
    public static final int Minor = 2;
    public static final int Build = 1;

    public static String getString() {
        return Major + "." + Minor + "." + Build;
    }
}
