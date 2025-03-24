package org.plateaubuilder.core.citymodel.attribute.manager;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;

public interface AttributeSchemaManager {
    String getAttributeName(String attributeName);

    String getAttributeType(String attributeName);

    String getAttributeMin(String attributeName);

    String getAttributeMax(String attributeName);

    String getChildAttributeName(String parentAttributeName, String attributeName);

    String getChildAttributeType(String parentAttributeName, String attributeName);

    String getChildAttributeMin(String parentAttributeName, String attributeName);

    String getChildAttributeMax(String parentAttributeName, String attributeName);

    ArrayList<String> getAttributeNameList(String parentAttributeName, ArrayList<String> addedAttributeNames);

    AttributeItem addAttribute(AbstractCityObject model, String attributeName, String attributeValue);

    String getAttributeUom(String attributeKey);

    List<ADEComponent> initializeAttributes(IFeatureView selectedFeature);

    List<ADEComponent> getADEComponents(IFeatureView selectedFeature);
}
