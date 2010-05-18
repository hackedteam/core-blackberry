package blackberry.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class InstanceConfig {
    public static InputStream getConfig()
    {
        final InputStream inputStream = InstanceConfig.class.getResourceAsStream("config.bin");
        if(inputStream == null){
        
        }
        return inputStream;
    } 
    
    //#ifdef FAKE323
    public static InputStream getConfig323()
    {
        ByteArrayInputStream istream = new ByteArrayInputStream(InstanceConfig323.getBytes());       
        return istream;
    }  
    //#endif
}
