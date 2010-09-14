package blackberry.record;

import net.rim.device.api.system.LED;
import net.rim.device.api.system.RuntimeStore;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;

public class AudioRecorderDispatcher implements Singleton {
    private static final long GUID = 0x9becbb51492755d2L;

    public static final int COLOR_LIGHT_BLUE = 0x00C8F0FF;

    //#ifdef DEBUG
    private static Debug debug = new Debug("AudioRecDisp", DebugLevel.VERBOSE);
    //#endif
    private static AudioRecorderDispatcher instance;
    private AudioRecorder recorder;

    public static synchronized AudioRecorderDispatcher getInstance() {
        if (instance == null) {
            instance = (AudioRecorderDispatcher) RuntimeStore.getRuntimeStore()
                    .get(GUID);
            if (instance == null) {
                final AudioRecorderDispatcher singleton = new AudioRecorderDispatcher();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    private AudioRecorderDispatcher() {

    }

    public synchronized void start() {

        if (recorder != null && recorder.isStarted()) {
            //#ifdef DEBUG_ERROR
            debug.error("not stopped recorder");
            //#endif
            return;
        }

        //#ifdef DEBUG_INFO
        debug.info("Start");
        //#endif

        recorder = new AudioRecorder();
        recorder.start();

        //#ifdef DEBUG_TRACE
        debug.trace("Started: " + (recorder != null));
        //#endif

        //#ifdef DEBUG
        debug.ledStart(COLOR_LIGHT_BLUE);
        //#endif
    }

    public synchronized void stop() {
        if (recorder == null) {
            //#ifdef DEBUG_ERROR
            debug.error("Null recorder");
            //#endif
            return;
        }

        //#ifdef DEBUG_INFO
        debug.info("STOP");
        //#endif

        recorder.stop();
        //#ifdef DEBUG
        debug.ledStop();
        //#endif
    }

    public synchronized byte[] getAvailable() {
        if (recorder != null) {
            return recorder.getAvailable();
        } else {
            return null;
        }
    }
}
