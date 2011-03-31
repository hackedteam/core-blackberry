//#preprocess
package blackberry.config;
//#ifdef DEBUG
//#endif
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
