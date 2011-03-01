package blackberry.agent.im;

import net.rim.device.api.system.RuntimeStore;

public class ImRepository {

    private static ImRepository instance;
    private static final long GUID = 0xe488768d5e21b9a5L;

    public static synchronized ImRepository getInstance() {
        if (instance == null) {
            instance = (ImRepository) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final ImRepository singleton = new ImRepository();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }
    
    public static boolean has(String partecipants, Line line) {
        
        return false;
    }

    public void add(String partecipants, Line line) {
        
        
    }

}
