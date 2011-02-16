package blackberry.debug;

public class LogLine {
    public LogLine(String message, int level, boolean error) {
        this.message=message;
        this.level=level;
        this.error=error;
    }
    String message;
    int level;
    boolean error;
}
