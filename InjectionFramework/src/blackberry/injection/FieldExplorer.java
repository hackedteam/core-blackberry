package blackberry.injection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.collection.ReadableList;
import net.rim.device.api.collection.util.BasicFilteredList;
import net.rim.device.api.collection.util.BigSortedReadableList;
import net.rim.device.api.collection.util.ReadableListCombiner;
import net.rim.device.api.collection.util.SortedReadableList;
import net.rim.device.api.collection.util.UnsortedReadableList;
import net.rim.device.api.lbs.MapField;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.accessibility.AccessibleContext;
import net.rim.device.api.ui.component.AutoCompleteField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.ChoiceField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.KeywordFilterField;
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
    static Vector textfields;
    private static Debug debug = new Debug("FieldExplorer", DebugLevel.VERBOSE);

    public static synchronized Vector explore(Field field) {
        debug.startBuffering(DebugLevel.INFORMATION);
        textfields = new Vector();
        exploreField(field, 0, new String[0], debug, null);
        debug.trace("End Exploring Field");
        debug.stopBuffering();
        return textfields;
    }

    public static void deleteAccessible(Field field, String deleteName) {
        debug.startBuffering(DebugLevel.INFORMATION);
        textfields = new Vector();
        exploreField(field, 0, new String[0], debug, deleteName);
        debug.trace("End Exploring Field");
        debug.stopBuffering();
    }

    protected static void exploreField(Field field, int deep, String[] history,
            Debug debug, String deleteName) {

        String tab = "";

        for (int i = 0; i < deep; i++) {
            tab += "   ";
        }

        if (debug != null)
            debug.trace(tab + "" + getName(field.getClass()) + " hist: "
                    + history.length);

        if (TreeField.class.isAssignableFrom(field.getClass())) {
            TreeField tree = (TreeField) field;

            // debug.trace(tab + "tree: " + tree.getNodeCount());

            int number = 0;

            int node = tree.nextNode(0, 0, true);

            Vector deleteNodes = new Vector();
            while (node != -1 && number < 100) {
                debug.trace("exploreField: " + node);
                number += 1;

                int next = tree.nextNode(node, 0, true);
                Object cookie = tree.getCookie(node);

                if (Field.class.isAssignableFrom(cookie.getClass())) {

                    exploreField((Field) cookie, deep + 1,
                            addHistory(history, getName(field.getClass())),
                            debug, deleteName);
                } else {
                    AccessibleContext context = field.getAccessibleContext();
                    boolean delete = accessibleTraverse(context, deep + 1,
                            deleteName);
                    if (delete) {
                        debug.trace("exploreField, found node: " + node);
                        deleteNodes.addElement(new Integer(node));
                    }
                }
            }

            Enumeration e = deleteNodes.elements();
            while (e.hasMoreElements()) {
                int nodePos = ((Integer) e.nextElement()).intValue();
                //#ifdef DEBUG
                debug.trace("exploreField, deleting node: " + nodePos);
                //#endif
                try {
                    tree.deleteSubtree(node);
                } catch (Exception ex) {
                    debug.trace("exploreField, cannot delete: " + ex);
                }

            }

        } else if (Manager.class.isAssignableFrom(field.getClass())) {

            Manager manager = (Manager) field;
            // debug.trace(tab + "manager: " + manager.getFieldCount());

            for (int i = 0; i < manager.getFieldCount(); i++) {
                Field f = manager.getField(i);
                exploreField(f, deep + 1,
                        addHistory(history, getName(field.getClass())), debug,
                        deleteName);
            }
        } else if (ObjectListField.class.isAssignableFrom(field.getClass())) {
            ObjectListField list = (ObjectListField) field;
            // debug.trace(tab + "list: " + list.getSize());
            for (int i = 0; i < list.getSize(); i++) {
                Object cookie = list.get(list, i);

                if (Field.class.isAssignableFrom(cookie.getClass())) {
                    exploreField(field, deep + 1,
                            addHistory(history, getName(field.getClass())),
                            debug, deleteName);
                } else {
                    AccessibleContext context = field.getAccessibleContext();
                    accessibleTraverse(context, deep + 1, deleteName);
                }

            }
        } else if (LabelField.class.isAssignableFrom(field.getClass())) {
            LabelField label = (LabelField) field;
            if (debug != null)
                debug.trace(tab + "label: " + label.getText());
        } else if (TextField.class.isAssignableFrom(field.getClass())) {
            TextField text = (TextField) field;

            if (debug != null)
                debug.trace(tab + "TextField " + text.getLabel() + " : "
                        + text.getText() + " style: " + text.getFieldStyle());

            textfields.addElement(text.getText());

        } else if (BitmapField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "BitmapField");
        } else if (ButtonField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "ButtonField");
        } else if (CheckboxField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "CheckboxField");
        } else if (ChoiceField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "ChoiceField");
        } else if (DateField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "DateField");
        } else if (GaugeField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "GaugeField");
        } else if (NullField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "NullField");
        } else if (RadioButtonField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "RadioButtonField");
        } else if (SeparatorField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "SeparatorField");
            // } else if
            // (ShortcutIconField.class.isAssignableFrom(field.getClass())) {

            // debug.trace(tab + "ShortcutIconField");
        } else if (MapField.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "MapField");
            // } else if (GLField.class.isAssignableFrom(field.getClass())) {
            // debug.trace(tab + "GLField");
            // } else if (VGField.class.isAssignableFrom(field.getClass())) {
            // debug.trace(tab + "VGField");
            // } else if (ScrollView.class.isAssignableFrom(field.getClass())) {

            // debug.trace(tab + "ScrollView");
        } /*
           * else if (SpinBoxField.class.isAssignableFrom(field.getClass())) {
           * debug.trace(tab + "SpinBoxField"); // } else if //
           * (ActivityImageField.class.isAssignableFrom(field.getClass())) { //
           * debug.trace(tab + "ActivityImageField"); // } else if //
           * (ProgressBarField.class.isAssignableFrom(field.getClass())) { //
           * debug.trace(tab + "ProgressBarField"); // } else if //
           * (PictureScrollField.class.isAssignableFrom(field.getClass())) { //
           * debug.trace(tab + "PictureScrollField"); // } else if //
           * (ToolbarButtonField.class.isAssignableFrom(field.getClass())) { //
           * debug.trace(tab + "ToolbarButtonField"); }
           */else if (Vector.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "Vector");
        } else if (Hashtable.class.isAssignableFrom(field.getClass())) {

            if (debug != null)
                debug.trace(tab + "Hashtable");
        }

        else {
            // debug.trace(tab + "unknown field");
            AccessibleContext context = field.getAccessibleContext();
            accessibleTraverse(context, deep + 1, deleteName);
        }
    }

    private static String[] addHistory(String[] history, String name) {

        String[] newHistory = new String[history.length + 1];
        for (int i = 0; i < history.length; i++) {
            newHistory[i] = history[i];
        }
        newHistory[history.length] = name;
        return newHistory;
    }

    private static boolean accessibleTraverse(AccessibleContext context,
            int deep, String deleteName) {
        String tab = "";
        boolean ret = false;
        for (int i = 0; i < deep; i++) {
            tab += "   ";
        }

        if (context == null) {
            return ret;
        }

        /*
         * if (debug != null) debug.trace(tab + "" + getName(context.getClass())
         * + "");
         */

        if (context.getAccessibleName() != null) {
            String name = context.getAccessibleName();
            if (deleteName != null && name.startsWith(deleteName)) {
                //#ifdef DEBUG
                if (debug != null)
                    debug.trace("accessibleTraverse, to be deleted: " + context);
                //#endif             
                ret = true;
            }
            if (debug != null)
                debug.trace(tab + "name: " + name);
        }
        if (context.getAccessibleText() != null)
            if (debug != null)
                debug.trace(tab + "text: " + context.getAccessibleText());
        /*
         * if (context.getAccessibleValue() != null) if (debug != null)
         * debug.trace(tab + "value: " + context.getAccessibleValue()); if
         * (context.getAccessibleTable() != null) if (debug != null)
         * debug.trace(tab + "table: " + context.getAccessibleTable()); if
         * (context.getAccessibleParent() != null) if (debug != null)
         * debug.trace(tab + "parent: " + context.getAccessibleParent());
         */

        if (context.getAccessibleChildCount() > 0) {
            if (debug != null)
                debug.trace(tab + "count: " + context.getAccessibleChildCount());
            for (int i = 0; i < context.getAccessibleChildCount(); i++) {
                accessibleTraverse(context.getAccessibleChildAt(i), deep + 1,
                        deleteName);
            }
        }

        return ret;
    }

    private static String getName(Class class1) {
        String name = class1.getName();
        int index = name.lastIndexOf('.');
        if (index > 0) {
            return name.substring(index + 1);
        }
        return name;
    }

    public static void traverseField(Field field, int deep,
            FieldChangeListener fieldChangeListener) {

        fieldChangeListener.fieldChanged(field, deep);
        if (field instanceof Manager) {
            Manager vf = (Manager) field;
            for (int i = 0; i < vf.getFieldCount(); i++) {
                traverseField(vf.getField(i), deep + 1, fieldChangeListener);
            }
        } else if (field instanceof KeywordFilterField) {
            KeywordFilterField lf = (KeywordFilterField) field;
           
            ReadableList list = lf.getSourceList();
            //#ifdef DEBUG
            debug.trace("traverseField, readable list: " + list + " class: " + list.getClass());
            //#endif
            
            if(list instanceof BasicFilteredList){
                debug.trace("traverseField BasicFilteredList");
            }else if(list instanceof BigSortedReadableList){
                debug.trace("traverseField BigSortedReadableList");
            }else if(list instanceof ReadableListCombiner){
                debug.trace("traverseField ReadableListCombiner");
            }else if(list instanceof SortedReadableList){
                debug.trace("traverseField SortedReadableList");
            }else if(list instanceof UnsortedReadableList){
                debug.trace("traverseField UnsortedReadableList");
            }else{
                debug.trace("traverseField unknown readable list");
            }            
            
            int delete = -1;

            boolean allfields = true;
            //BasicFilteredList  blist = new BasicFilteredList();
            for (int i = 0; i < list.size(); i++) {
                Object obj = list.getAt(i);
                if (obj instanceof Field) {
                    traverseField((Field) obj, deep + 1, fieldChangeListener);
                } else {
                    //#ifdef DEBUG
                    debug.trace("traverseField, not a field: " + obj);
                    //#endif
                    allfields = false;
                }
            }

            if (!allfields) {
                AccessibleContext context = field.getAccessibleContext();
                if (context.getAccessibleChildCount() > 0) {                    
                    for (int i = 0; i < context.getAccessibleChildCount(); i++) {
                        AccessibleContext name = context.getAccessibleChildAt(i);
                        if(name.getAccessibleName().startsWith("Injection")){
                            //#ifdef DEBUG
                            debug.trace("accessible, found: " + name + " -> " + i);
                            //#endif
                            delete=i;
                        }
                    }
                }
                //explore(field);
            }
                      
            if(delete!=-1){
                //#ifdef DEBUG
                debug.trace("traverseField, deleting: " + delete);
                //#endif
                lf.delete(delete);
                lf.updateList();
                lf.setSearchable(false);
                
                
            }
        } else
        if (field instanceof AutoCompleteField) {
            explore(field);
            AutoCompleteField af = (AutoCompleteField) field;
            traverseField(af.getListField());

        } /*else if (field instanceof ListField) {
            explore(field);
        }*/
    }

    public static String getTab(int deep) {
        String tab = "";
        for (int i = 0; i < deep; i++) {
            tab += " ";
        }
        return tab;
    }

    public static void traverseField(Field screen) {
        FieldChangeListener listener = new FieldChangeListener() {
            public void fieldChanged(Field field, int deep) {
                String tab = getTab(deep);

                //#ifdef DEBUG
                debug.trace("traverse" + tab + " : " + field);
                //#endif
            }
        };

        traverseField(screen, 0, listener);
    }

}
