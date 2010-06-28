package blackberry.record;

import blackberry.interfaces.Singleton;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class AudioRecorderDispatcher implements Singleton{
    //#ifdef DEBUG
    private static Debug debug = new Debug("AudioRecDisp", DebugLevel.VERBOSE);
    //#endif
    private static AudioRecorderDispatcher instance;
    private static AudioRecorder recorder;
    
    public synchronized static AudioRecorderDispatcher getInstance()
    {
        if(instance == null){
            instance = new AudioRecorderDispatcher();
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
        if(recorder==null || !recorder.isStarted()){
            //#ifdef DEBUG_ERROR
            debug.error("stopped recorder");
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
