// Copyright 2020 The casbin Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
