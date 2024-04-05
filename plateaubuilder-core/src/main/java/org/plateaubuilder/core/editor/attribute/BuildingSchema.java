package org.plateaubuilder.core.editor.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BuildingSchema {
    private Map<String, String> typeMap = new HashMap<>();

    public BuildingSchema() {
        typeMap.put("measuredHeight", "gml:MeasureType");
    }

    public String getType(String tagName) {
        for (var key : typeMap.keySet()) {
            if (key.contains(tagName))
                return typeMap.get(key);
        }
        return null;
    }

    public Set<String> getAllElements() {
        return typeMap.keySet();
    }
}
