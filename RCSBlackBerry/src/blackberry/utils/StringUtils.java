package blackberry.utils;

import java.util.Vector;

public class StringUtils {
    public static Vector split(String inString, String delimeter) {
        boolean finished = false;
        Vector vec = new Vector();
        try {
            int indexA = 0;
            int indexB = inString.indexOf(delimeter);

            while (indexB != -1) {
                if (indexB > indexA)
                    vec.addElement(new String(inString
                            .substring(indexA, indexB)));
                indexA = indexB + delimeter.length();
                indexB = inString.indexOf(delimeter, indexA);
            }
            vec.addElement(new String(inString.substring(indexA, inString
                    .length())));
          
        } catch (Exception e) {
 
        }
        return vec;
    }
}
