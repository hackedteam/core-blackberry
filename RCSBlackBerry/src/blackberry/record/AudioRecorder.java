package blackberry.record;

import java.lang.Thread;
import javax.microedition.media.Manager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import net.rim.device.api.util.Arrays;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class AudioRecorder extends Thread {
    // #!AMR\n
    public static final byte[] AMR_HEADER = new byte[] { 35, 33, 65, 77, 82, 10 };

    private static final int BUFFER_SIZE = 100 * 1024;

    //#ifdef DEBUG
    private static Debug debug = new Debug("AudioRecorder", DebugLevel.VERBOSE);
    //#endif

    private Player _player;
    private RecordControl _rcontrol;
    //private ByteArrayOutputStream _output;
    private byte _data[];

    PipedOutputStream os;
    PipedInputStream is;

    boolean started;

    public AudioRecorder() {
        started = false;
    }

    public boolean isStarted(){
        return started;
    }
    
    public  byte[] getAvailable() {
        //#ifdef DEBUG_TRACE
        debug.trace("getAvailable");
        //#endif
        return getChunk(0);
    }

    /**
     * Restituisce un blocco audio AMR
     * 
     * @param withHeader
     *            lascia l'header AMR, se c'e'.
     * @param size
     *            dimensione del chunk, 0 tutto il disponibile.
     * @return
     */
    public synchronized byte[] getChunk(int size) {

        if (!started) {
            //#ifdef DEBUG_WARN
            debug.warn("not started");
            //#endif
            return null;
        }

        //#ifdef DBC
        Check.requires(is != null, "getChunk: is null");
        Check.requires(os != null, "getChunk: os null");
        //#endif

        if (size == 0) {
            try {
                size = is.available();
                //#ifdef DEBUG_TRACE
                debug.trace("getChunk available: " + size);
                //#endif
                if (size == 0) {
                    return null;
                }
            } catch (IOException e) {
                //#ifdef DEBUG_WARN
                debug.warn("available: " + e.toString());
                //#endif
            }
        }

        byte[] buffer = new byte[size];

        try {
            for (int i = 0; i < size; i++) {
                int bb = is.read();
                if(bb == -1){
                    //#ifdef DEBUG_WARN
                    debug.warn("Cannot read");
                    //#endif
                    return null;
                }
                buffer[i] = (byte) bb;
            }
        } catch (Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
            return null;
        }

        return buffer;
    }

    //Create a Player object by invoking createPlayer() to capture audio.
    public void run() {
        try {
            //#ifdef DEBUG_TRACE
            debug.trace("Starting");
            //#endif

            os = new PipedOutputStream();
            is = new PipedInputStream(BUFFER_SIZE);

            is.connect(os);
            //os.write(new byte[]{0});

            _player = Manager.createPlayer("capture://audio?encoding=amr");
            //Invoke Player.realize().
            _player.realize();
            initRecord();
            _player.start();

            started = true;
            //In a catch block, specify actions to perform if an exception occurs.
        } catch (final Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }
    }

    /**
     * 
     */
    private void initRecord() {
        //Invoke Player.getControl() to obtain the controls for recording media from a Player.
        _rcontrol = (RecordControl) _player.getControl("RecordControl");
        //Create a ByteArrayOutputStream to record the audio stream. Note that you can also record directly to a file specified by a URL.

        //Invoke RecordControl.setRecordStream() to set the output stream to which the BlackBerry device application records data.
        _rcontrol.setRecordStream(os);
        //Invoke RecordStore.startRecord() to start recording the audio and start playing the media from the Player.
        _rcontrol.startRecord();
    }

    //Create a try block in your implementation of the stop method, and then invoke RecordControl.commit() to stop recording audio.
    public void stop() {
        try {
            started = false;

            //#ifdef DEBUG_TRACEs
            debug.trace("stop");
            //#endif            

            is.receivedLast();
            os.flush();

            //is.reset();
            _rcontrol.commit();
            //Invoke ByteArrayOutputStream.toByteArray() to write the audio data from the OutputStream to a byte array.

            //Invoke ByteArrayOutputStream.close() and Player.close() to close the OutputStream and Player.                                 

            os.close();
            //is.close();

            _player.close();
            //In a catch block, specify actions to perform if an exception occurs.
        } catch (Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }
    }

    public byte[] getData() {
        // TODO Auto-generated method stub
        return _data;
    }

}
