package org.plateaubuilder.core.citymodel.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.plateaubuilder.core.citymodel.IFeatureView;

/**
 * 複数地物間で共通の属性を管理するクラス
 */
public class CommonAttributeItem extends AttributeItem {
    private List<AttributeItem> relatedAttributes = new ArrayList<>();
    private Map<IFeatureView, AttributeItem> featureToAttributeMap = new HashMap<>();

    public CommonAttributeItem(AttributeItem baseAttribute, IFeatureView baseFeature) {
        super(baseAttribute.getAttributeHandler());
        featureToAttributeMap.put(baseFeature, baseAttribute);
    }

    public void addRelatedAttribute(IFeatureView feature, AttributeItem attribute) {
        relatedAttributes.add(attribute);
        featureToAttributeMap.put(feature, attribute);
    }

    @Override
    public String getValue() {
        String baseValue = super.getValue();
        // 関連する全ての属性の値を比較
        for (AttributeItem attr : relatedAttributes) {
            String attrValue = attr.getValue();
            if (attrValue == null || !baseValue.equals(attrValue)) {
                return "*";
            }
        }
        return baseValue;
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        for (AttributeItem attr : relatedAttributes) {
            attr.setValue(value);
        }
    }

    @Override
    public void setCodeSpace(String codeSpace) {
        super.setCodeSpace(codeSpace);
        for (AttributeItem attr : relatedAttributes) {
            attr.setCodeSpace(codeSpace);
        }
    }

    @Override
    public void setUom(String uom) {
        super.setUom(uom);
        for (AttributeItem attr : relatedAttributes) {
            attr.setUom(uom);
        }
    }

    public Set<IFeatureView> getRelatedFeatures() {
        return featureToAttributeMap.keySet();
    }

    public AttributeItem getAttributeForFeature(IFeatureView feature) {
        return featureToAttributeMap.get(feature);
    }

    public Set<Map.Entry<IFeatureView, AttributeItem>> getFeatureAttributeEntries() {
        return featureToAttributeMap.entrySet();
    }
}