package blackberry;

import java.util.Hashtable;

import net.rim.device.api.system.RuntimeStore;
import blackberry.interfaces.iSingleton;

public class Singleton {
    private static final long GUID = 0xfff1c1a301a4f332L;
    private static Singleton instance;

    private Hashtable hashtable;
    
    private Singleton(){
        hashtable=new Hashtable();
    }
    
    public static synchronized Singleton self() {
        if (instance == null) {
            instance = (Singleton) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final Singleton singleton = new Singleton();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    public iSingleton get(long guid) {        
        return (iSingleton) hashtable.get(new Long(guid));
    }

    public void put(long guid, iSingleton singleton) {
        hashtable.put(new Long(guid), singleton);
    }
    
    public void clear(){
        hashtable.clear();
        RuntimeStore.getRuntimeStore().remove(GUID);
    }

}
