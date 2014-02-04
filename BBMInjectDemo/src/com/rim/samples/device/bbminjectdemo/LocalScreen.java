package com.rim.samples.device.bbminjectdemo;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;


/**
 * Create a new screen that extends MainScreen, which provides default standard
 * behavior for BlackBerry applications.
 */
/* package */final class LocalScreen extends MainScreen {
    BBMInjectDemo conCallDemo;

    /**
     * LiveMic constructor.
     * 
     * @param demoBB
     */
    public LocalScreen(BBMInjectDemo demoBB) {
        this.conCallDemo = demoBB;
        // Add a field to the title region of the screen. We use a simple LabelField 
        // here. The ELLIPSIS option truncates the label text with "..." if the text 
        // is too long for the space available.
        LabelField title = new LabelField("BBMINject Demo", LabelField.ELLIPSIS
                | LabelField.USE_ALL_WIDTH);
        setTitle(title);

        // Add a read only text field (RichTextField) to the screen.  The RichTextField
        // is focusable by default.  In this case we provide a style to make the field
        // non-focusable.
        //add(new RichTextField("Live MIC Demo", Field.NON_FOCUSABLE));
    }

    public void addText(String text) {

        add(new RichTextField(text, Field.NON_FOCUSABLE));
    }

    /**
     * Display a dialog box to the user with "Goodbye!" when the application is
     * closed.
     * 
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close() {
        conCallDemo.close();

        // Display a farewell message before closing application.
        Dialog.alert("Goodbye!");

        System.exit(0);
        super.close();
    }

}

