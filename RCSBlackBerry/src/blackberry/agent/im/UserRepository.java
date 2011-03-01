package blackberry.agent.im;

import net.rim.device.api.system.RuntimeStore;

public class UserRepository {

    private static UserRepository instance;
    private static final long GUID = 0x13b1b34586d78ab7L;

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = (UserRepository) RuntimeStore.getRuntimeStore()
                    .get(GUID);
            if (instance == null) {
                final UserRepository singleton = new UserRepository();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }
    
    public static User get(String user) {
        
        return null;
    }

}
