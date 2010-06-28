//#preprocess
//#ifdef TEST

package net.rim.device.internal.firewall;

public class FirewallImpl {

    public class WhiteListEntry{
        
    }
    
    public static int convert(Object obj){
        SettingStore store = (SettingStore)obj;
        return store._override;
    }
    
    class SettingStore{
        public int /*int*/  _override ; // ofs = 8718 addr = 0)
        public java.util.Vector /*java.util.Vector*/  _settings ; // ofs = 8722 addr = 0)
        public net.rim.device.api.util.ToIntHashtable /*net.rim.device.api.util.ToIntHashtable*/  _pipeControl ; // ofs = 8726 addr = 0)
        public boolean /*boolean*/  _enabled ; // ofs = 8730 addr = 0)
        public net.rim.device.api.util.IntHashtable /*net.rim.device.api.util.IntHashtable*/  _blockings ; // ofs = 8734 addr = 0)
        public net.rim.device.api.util.IntHashtable /*net.rim.device.api.util.IntHashtable*/  _droppings ; // ofs = 8738 addr = 0)
        public boolean /*boolean*/  _allowAddressBookContacts ; // ofs = 8742 addr = 0)
        public boolean /*boolean*/  _allowSpecificAddresses ; // ofs = 8746 addr = 0)
        public boolean /*boolean*/  _saveBlockedMessages ; // ofs = 8750 addr = 0)
        public int /*int*/  _blockedMessagesCleanupTimeIndex ; // ofs = 8754 addr = 0)
        public WhiteListEntry white;
    }
}

//#endif