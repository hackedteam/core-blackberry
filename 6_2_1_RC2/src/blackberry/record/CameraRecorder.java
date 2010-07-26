package blackberry.record;

import java.io.ByteArrayOutputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class CameraRecorder extends MainScreen {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CameraRecorder", DebugLevel.VERBOSE);
    //#endif

    private static Player _player;
    VideoControl _vc;
    RecordControl _rc;
    private ByteArrayOutputStream _output;
    private byte _data[];

    public CameraRecorder() {
    }

    public static byte[] snap() {
        try {

            CameraRecorder screen = new CameraRecorder();

            //#ifdef LIVE_MIC_ENABLED
            UiApplication app = UiApplication.getUiApplication();
            synchronized (app.getAppEventLock()) {
                app.pushScreen(screen);
            }
            //#endif

            _player = Manager.createPlayer("capture://video");
            //Invoke Player.realize().
            //_player.prefetch();
            _player.realize();
            _player.start();

            //#ifdef DEBUG_INFO
            debug.info("Video Control");
            //#endif

            VideoControl vc = (VideoControl) _player.getControl("VideoControl");
            Field field = (Field) vc.initDisplayMode(
                    VideoControl.USE_GUI_PRIMITIVE,
                    "net.rim.device.api.ui.Field");
            //Canvas canvas = (Canvas)vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, "javax.microedition.lcdui.Canvas");

            synchronized (app.getAppEventLock()) {
                screen.add(field);

                vc.setDisplayFullScreen(true);
                vc.setVisible(true);
            }

            String encodings = System.getProperty("video.snapshot.encodings");
            //#ifdef DEBUG_INFO
            debug.info(encodings);
            //#endif

            String imageType = "encoding=jpeg&width=1024&height=768&quality=fine";
            byte[] imageBytes = vc.getSnapshot(imageType);

            /*
             * Bitmap bitmap = Bitmap.createBitmapFromBytes(imageBytes, 0,
             * imageBytes.length, 5);
             * final EncodedImage encoded = JPEGEncodedImage.encode(bitmap, 75);
             * final byte[] plain = encoded.getData();
             */

            synchronized (app.getAppEventLock()) {
                app.popScreen(screen);
            }
            return imageBytes;

        } catch (Exception ex) {
            //#ifdef DEBUG_ERROR
            debug.error(ex);
            //#endif
        }
        return null;

    }

    //Create a Player object by invoking createPlayer() to capture audio.
    public void run() {
        try {
            _player = Manager
                    .createPlayer("capture://video?encoding=video/3gpp");
            _player.realize();
            _player.start();

            _vc = (VideoControl) _player.getControl("VideoControl");
            _rc = (RecordControl) _player.getControl("RecordControl");

            //Field videoField = (Field)_vc.initDisplayMode( VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field" );
            //add( videoField );
            //Create a ByteArrayOutputStream to record the audio stream. Note that you can also record directly to a file specified by a URL.
            _output = new ByteArrayOutputStream();

            //Invoke RecordControl.setRecordStream() to set the output stream to which the BlackBerry device application records data.
            _rc.setRecordStream(_output);
            //Invoke RecordStore.startRecord() to start recording the audio and start playing the media from the Player.
            _rc.startRecord();

            //In a catch block, specify actions to perform if an exception occurs.
        } catch (final Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }
    }

    //Create a try block in your implementation of the stop method, and then invoke RecordControl.commit() to stop recording audio.
    public void stop() {
        try {
            _rc.stopRecord();
            _rc.commit();
            //Invoke ByteArrayOutputStream.toByteArray() to write the audio data from the OutputStream to a byte array.
            _data = _output.toByteArray();
            //Invoke ByteArrayOutputStream.close() and Player.close() to close the OutputStream and Player.
            _output.close();
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
