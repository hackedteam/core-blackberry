package blackberry.record;

import net.rim.device.api.system.RuntimeStore;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;

public class AudioRecorderDispatcher implements Singleton{
    private static final long GUID = 0x9becbb51492752d2L;
    //#ifdef DEBUG
    private static Debug debug = new Debug("AudioRecDisp", DebugLevel.VERBOSE);
    //#endif
    private static AudioRecorderDispatcher instance;
    private static AudioRecorder recorder;   
    
    public static synchronized AudioRecorderDispatcher getInstance() {
        if (instance == null) {
            instance = (AudioRecorderDispatcher) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final AudioRecorderDispatcher singleton = new AudioRecorderDispatcher();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    
    private AudioRecorderDispatcher(){
        
    }
    
    public synchronized void start(){
        
        if(recorder!=null && recorder.isStarted()){
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
    }
    
    public synchronized void stop(){
        if(recorder==null){
            //#ifdef DEBUG_ERROR
            debug.error("null recorder");
            //#endif
            return;
        }
        
        //#ifdef DEBUG_INFO
        debug.info("STOP");
        //#endif
        
        recorder.stop();
    }
    
    public synchronized byte[] getAvailable(){
        return recorder.getAvailable();
    }    
}
