//#preprocess
package blackberry.config;

import java.io.InputStream;

public class InstanceConfig {
    public static InputStream getConfig() {
        try {
            final InputStream inputStream = InstanceConfig.class
                    .getResourceAsStream("config.bin");
            if (inputStream == null) {
            }
            return inputStream;
        } catch (Exception ex) {
            return null;
        }
    }

}
