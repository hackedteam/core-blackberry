package blackberry.utils;

import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;

public final class StringSortVector extends SimpleSortingVector {

	class StringCompator implements Comparator {

		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			
			return s1.compareTo(s2);
		}
	}

	static StringCompator stringCompator = null;

	public StringSortVector() {
		super();
		if (stringCompator == null) {
			stringCompator = this.new StringCompator();
		}
		setSortComparator(stringCompator);
	}	
}


