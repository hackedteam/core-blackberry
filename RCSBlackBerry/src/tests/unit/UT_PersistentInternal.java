//#preprocess
package tests.unit;

import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.internal.firewall.FirewallImpl;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;

public class UT_PersistentInternal extends TestUnit {

    public UT_PersistentInternal(String name, Tests tests) {
        super(name, tests);
    }

    public boolean run() throws AssertException {
        Internals();
        return true;
    }

    private void Internals() {
        //#ifdef DEBUG_INFO
        debug.info("phoneutilities");
        //#endif
        long key = 1197739752382153834L;
        PersistentObject obj = PersistentStore.getPersistentObject(key);
        
      //#ifdef DEBUG_INFO
        debug.info("firewallimpl");
        //#endif
        key = -6336176786833674023L;
        obj = PersistentStore.getPersistentObject(key);
        Object content = obj.getContents();
        ControlledAccess access =  obj.getControlledAccess();
        String str = content.toString();
       
        Class cl = content.getClass();
        //#ifdef TEST
        FirewallImpl.convert(content);
        //#endif
        
        //#ifdef DEBUG_INFO
        debug.info("passwordkeeper");
        //#endif
        key = 5195381784668223364L;
        obj = PersistentStore.getPersistentObject(key);
      
        
        
    }

}
