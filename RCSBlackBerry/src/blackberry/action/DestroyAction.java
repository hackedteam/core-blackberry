package blackberry.action;

import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;

public class DestroyAction extends SubAction {

    private boolean permanent;

    public DestroyAction(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(ConfAction conf) {
        //TODO messages
        try {
            permanent=conf.getBoolean("permanent");
        } catch (ConfigurationException e) {
           return false;
        }
        return true;
    }

    public boolean execute(Trigger trigger) {
        
        return false;
    }

}
