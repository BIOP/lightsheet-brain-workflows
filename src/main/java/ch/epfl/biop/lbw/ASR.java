/*-
 * #%L
 * Format and preprocess whole-brain cleared brain images acquired with light-sheet fluorescence microscopy
 * %%
 * Copyright (C) 2024 - 2025 EPFL
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ch.epfl.biop.lbw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASR {

    public static Map<String, List<Map<String, Object>>> t;

    static {
        t = new HashMap<>();

        // Populate the map
        t.put("ipl", createList(
                createMap("axis", "y-axis", "angle", 180),
                createMap("axis", "x-axis", "angle", -90)
        ));
        t.put("ial", createList(
                createMap("axis", "y-axis", "angle", 180),
                createMap("axis", "x-axis", "angle", -90),
                createMap("axis", "z-axis", "angle", 180)
        ));
        t.put("ras", createList(
                createMap("axis", "y-axis", "angle", -90),
                createMap("axis", "x-axis", "angle", 90)
        ));
        t.put("sal", createList(
                createMap("axis", "y-axis", "angle", 180),
                createMap("axis", "x-axis", "angle", 90)
        ));
        t.put("psl", createList(
                createMap("axis", "y-axis", "angle", 180)
        ));
        t.put("pir", createList(
                createMap("axis", "x-axis", "angle", 180)
        ));
        t.put("lai", createList(
                createMap("axis", "y-axis", "angle", -90),
                createMap("axis", "x-axis", "angle", 90)
        ));
        t.put("iar", createList(
                createMap("axis", "x-axis", "angle", 90)
        ));
        t.put("ail", createList(
                createMap("axis", "z-axis", "angle", 180)
        ));
        t.put("asr", new ArrayList<>()); // Empty list
        t.put("rpi", createList(
                createMap("axis", "y-axis", "angle", -90),
                createMap("axis", "x-axis", "angle", -90)
        ));
        t.put("lps", createList(
                createMap("axis", "y-axis", "angle", 90),
                createMap("axis", "x-axis", "angle", -90)
        ));
        t.put("spr", createList(
                createMap("axis", "x-axis", "angle", -90)
        ));
    }


    // Helper method to create a list
    private static List<Map<String, Object>> createList(Map<String, Object>... elements) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> element : elements) {
            list.add(element);
        }
        return list;
    }

    // Helper method to create a map
    private static Map<String, Object> createMap(Object... keyValuePairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return map;
    }
}
