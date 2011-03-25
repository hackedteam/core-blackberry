/*
 * EncodingPropertiesScreen.java
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

import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;

/**
 * This MainScreen class allows a user to specify an encoding to be used
 * for taking a picture.
 */
public class EncodingPropertiesScreen extends MainScreen
{
    RadioButtonGroup _radioButtonGroup;
    CameraScreen _parentScreen;
    
    /**
     * Constructs a new EncodingPropertiesScreen object
     * @param encodingProperties The array of encoding properties available
     * @param parentScreen The parent screen of the application
     * @param currentSelectedIndex The index of the encoding that is currently selected
     */
    public EncodingPropertiesScreen(EncodingProperties[] encodingProperties, CameraScreen parentScreen, int currentSelectedIndex) 
    {
        _parentScreen = parentScreen;
        _radioButtonGroup = new RadioButtonGroup();
        for(int i = 0; i < encodingProperties.length; i++)
        {
            RadioButtonField buttonField = new RadioButtonField(encodingProperties[i].toString());
            _radioButtonGroup.add(buttonField);
            this.add(buttonField);
        }
        _radioButtonGroup.setSelectedIndex(currentSelectedIndex);
    }
    
    /**
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close()
    {
        // Set the index of the selected encoding
        _parentScreen.setIndexOfEncoding(_radioButtonGroup.getSelectedIndex());
        super.close();
    }
    
    /**
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    public boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }
}
