/*
 * Encoding.java
 *
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.rim.samples.device.camerademo;

/**
 * A wrapper for the various encoding properties available
 * for use with the VideoControl.getSnapshot() method.
 */
public final class EncodingProperties
{   
    /** The file format of the picture */
    private String _format;

    /** The width of the picture */
    private String _width;

    /** The height of the picture */
    private String _height;
    
    
    /** Booleans that indicate whether the values have been set */
    private boolean _formatSet;
    private boolean _widthSet;
    private boolean _heightSet;    

    /**
     * Set the file format to be used in snapshots
     * @param format The file format to be used in snapshots
     */
    public void setFormat(String format)
    {
        _format = format;
        _formatSet = true;
    }

    /**
     * Set the width to be used in snapshots
     * @param width The width to be used in snapshots
     */
    void setWidth(String width)
    {
        _width = width;
        _widthSet = true;
    }

    /**
     * Set the height to be used in snapshots
     * @param height The height to be used in snapshots
     */
    void setHeight(String height)
    {
        _height = height;
        _heightSet = true;
    }    

    /**
     * Return the encoding as a coherent String to be used in menus
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuffer display = new StringBuffer();

        display.append(_width);
        display.append(" x ");
        display.append(_height);
        display.append(" ");
        display.append(_format);                

        return display.toString();
    }

    /**
     * Return the encoding as a properly formatted string to
     * be used by the VideoControl.getSnapshot() method.
     * @return The encoding expressed as a formatted string.
     */
    String getFullEncoding()
    {
        StringBuffer fullEncoding = new StringBuffer();

        fullEncoding.append("encoding=");
        fullEncoding.append(_format);

        fullEncoding.append("&width=");
        fullEncoding.append(_width);

        fullEncoding.append("&height=");
        fullEncoding.append(_height);        

        return fullEncoding.toString();
    }
    
    /**
     * Checks whether all the fields been set
     * @return true if all fields have been set.
     */
    boolean isComplete()
    {
        return _formatSet && _widthSet && _heightSet;
    }
}
