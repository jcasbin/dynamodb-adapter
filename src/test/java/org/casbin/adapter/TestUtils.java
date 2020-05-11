package org.casbin.adapter;

import java.util.Arrays;
import java.util.List;

public class TestUtils {

    public static <T> boolean arrayEquals(List<T> a, List<T> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return a.isEmpty() && b.isEmpty();
        }
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i ++) {
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }
        return true;
    }

     /**
      * Compare if two sets of arrays are equal 
      * E.g. [[1,2], [3]] is equal to [[3], [1,2]],
      * but [[1,2], [3]] is not equal to [3], [2,1]]
      * @param <T>: The type of the Object.
      * @param a: The first set.
      * @param b: The second set.
      * @return: If they are equal to each other.
      */

    public static <T> boolean setOfArrayEquals(List<List<T>> a, List<List<T>> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return a.isEmpty() && b.isEmpty();
        }
        if (a.size() != b.size()) {
            return false;
        }
        Boolean[] flag = new Boolean[a.size()];
        Arrays.fill(flag, false);
        for (List<T> ele : a) {
            Boolean updated = false;
            for (Integer i = 0; i < b.size(); ++i) {
                if (!flag[i] && arrayEquals(ele, b.get(i))) {
                    flag[i] = true;
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                return false;
            }
        }
        return true;
    }
}