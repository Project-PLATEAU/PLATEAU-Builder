package org.plateaubuilder.core.citymodel.attribute;

import java.util.ArrayList;
import java.util.List;

public class Attributes extends ArrayList<Attribute> {

    public List<String> getAttributeNames() {
        var attributeNames = new ArrayList<String>();
        for (var attribute : this) {
            attributeNames.addAll(attribute.getLeafFullNames());
        }
        return attributeNames;
    }

    public List<String> getAllAttributeNames() {
        var attributeNames = new ArrayList<String>();
        attributeNames.addAll(getAttributeNames());
        return attributeNames;
    }
}
