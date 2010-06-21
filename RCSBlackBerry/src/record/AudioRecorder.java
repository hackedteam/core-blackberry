package record;

import java.lang.Thread;
import javax.microedition.media.Manager;
import java.io.ByteArrayOutputStream;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class AudioRecorder extends Thread {
  //#ifdef DEBUG
    private static Debug debug = new Debug("AudioRecorder", DebugLevel.VERBOSE);
    //#endif
    
    private Player _player;
    private RecordControl _rcontrol;
    private ByteArrayOutputStream _output;
    private byte _data[];

    public AudioRecorder() {
    }

    //Create a Player object by invoking createPlayer() to capture audio.
    public void run() {
        try {
            _player = Manager.createPlayer("capture://audio?encoding=amr");
            //Invoke Player.realize().
            _player.realize();
            //Invoke Player.getControl() to obtain the controls for recording media from a Player.
            _rcontrol = (RecordControl) _player.getControl("RecordControl");
            //Create a ByteArrayOutputStream to record the audio stream. Note that you can also record directly to a file specified by a URL.
            _output = new ByteArrayOutputStream();
            //Invoke RecordControl.setRecordStream() to set the output stream to which the BlackBerry device application records data.
            _rcontrol.setRecordStream(_output);
            //Invoke RecordStore.startRecord() to start recording the audio and start playing the media from the Player.
            _rcontrol.startRecord();
            _player.start();
            //In a catch block, specify actions to perform if an exception occurs.
        } catch (final Exception e) {
            //Perform actions 
        }
    }

    //Create a try block in your implementation of the stop method, and then invoke RecordControl.commit() to stop recording audio.
    public void stop() {
        try {
            _rcontrol.commit();
            //Invoke ByteArrayOutputStream.toByteArray() to write the audio data from the OutputStream to a byte array.
            _data = _output.toByteArray();
            //Invoke ByteArrayOutputStream.close() and Player.close() to close the OutputStream and Player.
            _output.close();
            _player.close();
            //In a catch block, specify actions to perform if an exception occurs.
        } catch (Exception e) {
            //Perform actions
        }
    }

    public byte[] getData() {
        // TODO Auto-generated method stub
        return _data;
    }

}
