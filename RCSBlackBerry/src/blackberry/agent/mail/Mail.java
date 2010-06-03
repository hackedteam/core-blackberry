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
        return plainTextMessage != null;
    }

    public final boolean hasHtml() {
        return htmlMessage != null;
    }

}
