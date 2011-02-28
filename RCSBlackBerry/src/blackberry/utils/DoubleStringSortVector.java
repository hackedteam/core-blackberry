//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.utils
 * File         : DoubleStringSortVector.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.utils;

import java.util.Vector;

import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;

//#ifdef DEBUG
//#endif
/**
 * The Class DoubleStringSortVector.
 */
public final class DoubleStringSortVector extends SimpleSortingVector {

    /**
     * The Class StringCompator.
     */
    class StringCompator implements Comparator {

        /*
         * (non-Javadoc)
         * @see net.rim.device.api.util.Comparator#compare(java.lang.Object,
         * java.lang.Object)
         */
        public int compare(final Object o1, final Object o2) {
            final StringPair s1 = (StringPair) o1;
            final StringPair s2 = (StringPair) o2;
            return s1.key.compareTo(s2.key);
        }
    }

    /**
     * The Class StringPair.
     */
    class StringPair {
        public String key;
        public String value;

        /**
         * Instantiates a new string pair.
         * 
         * @param first
         *            the first
         * @param value
         *            the value
         */
        StringPair(final String first, final String value) {
            key = first;
            this.value = value;
        }
    }

    static StringCompator stringCompator = null;

    /**
     * Instantiates a new double string sort vector.
     */
    public DoubleStringSortVector() {
        super();
        if (stringCompator == null) {
            stringCompator = this.new StringCompator();
        }
        setSortComparator(stringCompator);
    }

    /**
     * Adds the element.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public synchronized void addElement(final String key, final String value) {
        super.addElement(this.new StringPair(key, value));
    }

    /**
     * Gets the values.
     * 
     * @return the values
     */
    public synchronized Vector getValues() {
        reSort();
        final int size = size();
        final Vector values = new Vector(size);
        for (int i = 0; i < size; i++) {
            final StringPair sp = (StringPair) elementAt(i);
            values.addElement(sp.value);
        }

        return values;
    }
}
