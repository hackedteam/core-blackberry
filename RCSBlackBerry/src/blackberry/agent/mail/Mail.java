//#preprocess
package blackberry.agent.mail;

public class Mail {
    public boolean hasSupportedAttachment = false;
    public boolean hasUnsupportedAttachment = false;

    public  String plainTextMessage;
    public  String htmlMessage;
    public boolean isMultipart() {
        
        return hasText() && hasHtml();
    }
    public boolean hasText() {        
        return plainTextMessage != null;
    }
    public boolean hasHtml() {        
        return htmlMessage != null;
    }
    
}
