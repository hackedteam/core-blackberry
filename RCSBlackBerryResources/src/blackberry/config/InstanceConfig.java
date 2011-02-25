//#preprocess
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
    
}
