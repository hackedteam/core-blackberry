//#preprocess
package blackberry.agent.mail;

public class Mail {
    public boolean hasSupportedAttachment = false;
    public boolean hasUnsupportedAttachment = false;

    public String plainTextMessage;
    public String htmlMessage;

    public final boolean isMultipart() {
        return hasText() && hasHtml();
    }

    public final boolean hasText() {
        int len = 0;

        if (plainTextMessage != null) {
            len = plainTextMessage.trim().length();
        }

        return len > 0;
    }

    public final boolean hasHtml() {
        return htmlMessage != null;
    }

}
