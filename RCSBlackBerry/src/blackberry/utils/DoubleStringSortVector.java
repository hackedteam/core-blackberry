package blackberry.utils;

import java.util.Vector;

import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;
import blackberry.utils.StringSortVector.StringCompator;

public class DoubleStringSortVector  extends SimpleSortingVector {

		class StringPair {
			public String key;
			public String value;
			StringPair(String first, String value){
				this.key=first;
				this.value=value;				
			}
		}
			
		class StringCompator implements Comparator {

			public int compare(Object o1, Object o2) {
				StringPair s1 = (StringPair) o1;
				StringPair s2 = (StringPair) o2;
				return s1.key.compareTo(s2.key);
			}
		}

		static StringCompator stringCompator = null;

		public DoubleStringSortVector() {
			super();
			if (stringCompator == null) {
				stringCompator = this.new StringCompator();
			}
			setSortComparator(stringCompator);
		}
		
		public synchronized void addElement(String key, String value){
			super.addElement(this.new StringPair(key, value));
		}
		
		public synchronized Vector getValues(){
			reSort();
			int size = size();
			Vector values = new Vector(size);
			for(int i = 0; i< size; i++){
				StringPair sp = (StringPair)elementAt(i);
				values.addElement(sp.value);
			}
							
			return values;			
		}
}
