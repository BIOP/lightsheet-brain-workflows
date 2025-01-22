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
