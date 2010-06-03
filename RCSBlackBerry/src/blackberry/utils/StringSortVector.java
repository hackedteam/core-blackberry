//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.utils
 * File         : StringSortVector.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.utils;

import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;

// TODO: Auto-generated Javadoc
/**
 * The Class StringSortVector.
 */
public final class StringSortVector extends SimpleSortingVector {

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
            final String s1 = (String) o1;
            final String s2 = (String) o2;

            return s1.compareTo(s2);
        }
    }

    static StringCompator stringCompator = null;

    /**
     * Instantiates a new string sort vector.
     */
    public StringSortVector() {
        super();
        if (stringCompator == null) {
            stringCompator = this.new StringCompator();
        }
        setSortComparator(stringCompator);
    }
}
