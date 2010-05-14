//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.interfaces
 * File         : BatteryStatusObserver.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.interfaces;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications
 * about BatteryStatus information as the BatteryStatus is constructed.
 */
public interface BatteryStatusObserver {

    /**
     * This method is called when information about an BatteryStatus
     * which was previously requested using an asynchronous
     * interface becomes available.
     * 
     * @param status
     *            the status
     * @param diff
     *            the diff
     */
    void onBatteryStatusChange(final int status, final int diff);
}
