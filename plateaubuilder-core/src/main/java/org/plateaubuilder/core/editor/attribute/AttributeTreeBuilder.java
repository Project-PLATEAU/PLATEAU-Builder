package org.plateaubuilder.core.editor.attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.CommonAttributeItem;
import org.plateaubuilder.core.citymodel.attribute.manager.BuildingSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.CityFurnitureSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.LandUseSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.PlantCoverSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.RoadSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.SolitaryVegetationObjectSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.UrbanPlanningAreaSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.WaterBodySchemaManager;
import org.plateaubuilder.core.citymodel.attribute.reader.XSDSchemaDocument;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NodeAttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.RootAttributeHandler;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.editor.Editor;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javafx.scene.control.TreeItem;

public class AttributeTreeBuilder {
    private XSDSchemaDocument uroSchemaDocument = Editor.getUroSchemaDocument();
    private static String modelType = "";
    private static final String URO_PREFIX = "uro";
    private static final String COLON_SPLIT_REGEX = ":";

    public static void attributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        AbstractCityObject feature = selectedFeature.getGML();
        if (feature instanceof AbstractBuilding) {
            addBldgAttributeToTree(selectedFeature, root);
            modelType = feature.getCityGMLClass().toString();
        } else if (feature instanceof Road) {
            addRoadAttributeToTree(selectedFeature, root);
            modelType = feature.getCityGMLClass().toString();
        } else if (feature instanceof LandUse) {
            addLandUseAttributeToTree(selectedFeature, root);
            modelType = feature.getCityGMLClass().toString();
        } else if (feature instanceof WaterBody) {
            addWaterBodyAttributeToTree(selectedFeature, root);
            modelType = feature.getCityGMLClass().toString();
        } else if (feature instanceof SolitaryVegetationObject) {
            addSolitaryVegetationObjectAttributeToTree(selectedFeature, root);
            modelType = feature.getCityGMLClass().toString();
        } else if (feature instanceof CityFurniture) {
            addCityFurnitureAttributeToTree(selectedFeature, root);
            modelType = feature.getCityGMLClass().toString();
        } else if (feature instanceof PlantCover) {
            addPlantCoverAttributeToTree(selectedFeature, root);
            modelType = feature.getCityGMLClass().toString();
        } else if (feature instanceof ADEGenericComponent) {
            modelType = ((ADEGenericComponent) feature).getNodeName().split(COLON_SPLIT_REGEX)[1];
        }
        addADEPropertyToTree(selectedFeature, root);
    }

    public static void addBldgAttributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            new BuildingSchemaManager().addAttributeToTreeView(selectedFeature, root);
    }

    public static void addRoadAttributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            new RoadSchemaManager().addAttributeToTreeView(selectedFeature, root);
    }

    public static void addLandUseAttributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            new LandUseSchemaManager().addAttributeToTreeView(selectedFeature, root);
    }

    public static void addWaterBodyAttributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            new WaterBodySchemaManager().addAttributeToTreeView(selectedFeature, root);
    }

    public static void addSolitaryVegetationObjectAttributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            new SolitaryVegetationObjectSchemaManager().addAttributeToTreeView(selectedFeature, root);
    }

    public static void addPlantCoverAttributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            new PlantCoverSchemaManager().addAttributeToTreeView(selectedFeature, root);
    }

    public static void addCityFurnitureAttributeToTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            new CityFurnitureSchemaManager().addAttributeToTreeView(selectedFeature, root);

    }

    public static void addADEPropertyToTree(IFeatureView selectedFeature, TreeItem<AttributeItem> root) {
        for (var adeComponent : getADEComponents(selectedFeature)) {
            var adeElement = (ADEGenericElement) adeComponent;
            addXMLElementToTree(adeElement.getContent(), null, root);
        }
    }

    public static void addXMLElementToTree(Node node, Node parentNode, TreeItem<AttributeItem> root) {
        String fullNodeName = node.getNodeName();
        String nodeName = fullNodeName.contains(":") ? fullNodeName.split(":", 2)[1] : fullNodeName;

        // ノード名がmodelTypeに一致する場合、このノードをスキップして子ノードを処理
        if (nodeName.equals(modelType)) {
            // 子ノードを直接処理
            var children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);
                if (!(childNode instanceof CharacterData)) {
                    addXMLElementToTree(childNode, node, root); // 親のTreeItemをそのまま渡す
                }
            }
            return;
        } else if (nodeName.matches("lod[1-3]MultiSurface")) {
            return;
        }

        String type = getType(node, parentNode);
        if (node.getChildNodes().getLength() == 1 && node.getFirstChild() instanceof CharacterData) {

            // 子の内容を属性値として登録して再帰処理を終了
            AttributeItem attributeItem = new AttributeItem(new NodeAttributeHandler(node, type));
            var item = new TreeItem<>(attributeItem);
            root.getChildren().add(item);
            return;
        }
        var item = new TreeItem<>(
                new AttributeItem(new NodeAttributeHandler(node, type)));
        item.setExpanded(true);

        // XMLの子要素を再帰的に追加
        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            var childNode = children.item(i);

            // ここでの文字列要素はタブ・改行なので飛ばす
            if (childNode instanceof CharacterData)
                continue;

            addXMLElementToTree(childNode, node, item);
        }
        root.getChildren().add(item);
    }

    public static ChildList<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
        var gml = selectedFeature.getGML();
        if (gml instanceof AbstractBuilding) {
            return (ChildList<ADEComponent>) ((AbstractBuilding) gml).getGenericApplicationPropertyOfAbstractBuilding();
        } else if (gml instanceof Road) {
            return (ChildList<ADEComponent>) ((Road) gml).getGenericApplicationPropertyOfRoad();
        } else if (gml instanceof LandUse) {
            return (ChildList<ADEComponent>) ((LandUse) gml).getGenericApplicationPropertyOfLandUse();
        } else if (gml instanceof SolitaryVegetationObject) {
            return (ChildList<ADEComponent>) ((SolitaryVegetationObject) gml)
                    .getGenericApplicationPropertyOfVegetationObject();
        } else if (gml instanceof WaterBody) {
            return (ChildList<ADEComponent>) ((WaterBody) gml)
                    .getGenericApplicationPropertyOfWaterBody();
        } else if (gml instanceof CityFurniture) {
            return (ChildList<ADEComponent>) ((CityFurniture) gml)
                    .getGenericApplicationPropertyOfCityFurniture();
        } else if (gml instanceof PlantCover) {
            return (ChildList<ADEComponent>) ((PlantCover) gml)
                    .getGenericApplicationPropertyOfVegetationObject();
        } else {
            return (ChildList<ADEComponent>) ((ADEGenericComponent) gml)
                    .getGenericApplicationPropertyOfADEGenericComponent();
        }
    }

    public static String getType(Node node, Node parentNode) {
        String type = "";
        String prefix = node.getNodeName().split(COLON_SPLIT_REGEX)[0];
        if (prefix.matches(URO_PREFIX)) {
            if (parentNode != null) {
                type = Editor.getUroSchemaDocument().getType(node.getNodeName(), parentNode.getNodeName(), URO_PREFIX);
            } else {
                type = Editor.getUroSchemaDocument().getType(node.getNodeName(), null, URO_PREFIX);
            }
        } else if (modelType.matches(modelType)) {
            type = new UrbanPlanningAreaSchemaManager()
                    .getAttributeType(node.getNodeName().split(COLON_SPLIT_REGEX)[1]);
        }
        return type;
    }

    /**
     * 複数地物から共通の属性を抽出してTreeItemを構築します
     */
    public static void commonAttributesToTree(Set<IFeatureView> selectedFeatures, TreeItem<AttributeItem> root) {
        if (selectedFeatures == null || selectedFeatures.isEmpty()) {
            return;
        }

        // 基準となる最初の地物の属性ツリーを構築
        IFeatureView firstFeature = selectedFeatures.iterator().next();
        TreeItem<AttributeItem> firstRoot = new TreeItem<>(new AttributeItem(new RootAttributeHandler(firstFeature)));
        attributeToTree(firstFeature, firstRoot);

        // 全地物の属性をマッピング
        Map<IFeatureView, Map<String, TreeItem<AttributeItem>>> featureAttributeMap = new HashMap<>();
        for (IFeatureView feature : selectedFeatures) {
            TreeItem<AttributeItem> featureRoot = new TreeItem<>(new AttributeItem(new RootAttributeHandler(feature)));
            attributeToTree(feature, featureRoot);
            featureAttributeMap.put(feature, mapAttributesByName(featureRoot));
        }

        // 最初の地物の属性をベースに共通属性ツリーを構築
        buildCommonAttributeTree(firstRoot.getChildren(), featureAttributeMap, root, firstFeature);
    }

    // 属性を名前でマッピングする
    private static Map<String, TreeItem<AttributeItem>> mapAttributesByName(TreeItem<AttributeItem> root) {
        Map<String, TreeItem<AttributeItem>> attributeMap = new HashMap<>();
        attributeMap.put(root.getValue().getName(), root);
        for (TreeItem<AttributeItem> item : root.getChildren()) {
            attributeMap.put(item.getValue().getName(), item);
            attributeMap.putAll(mapAttributesByName(item));
        }
        return attributeMap;
    }

    // 共通属性を抽出してTreeItemを構築
    private static void buildCommonAttributeTree(List<TreeItem<AttributeItem>> sourceItems,
            Map<IFeatureView, Map<String, TreeItem<AttributeItem>>> featureAttributeMap,
            TreeItem<AttributeItem> targetParent, IFeatureView firstFeature) {

        for (TreeItem<AttributeItem> sourceItem : sourceItems) {
            AttributeItem sourceAttr = sourceItem.getValue();
            CommonAttributeItem commonAttr = new CommonAttributeItem(sourceAttr, firstFeature);
            TreeItem<AttributeItem> commonItem = new TreeItem<>(commonAttr);

            // 他の地物で同じ名前の属性を探して関連付け
            boolean allFeaturesHaveAttribute = true;
            for (Map.Entry<IFeatureView, Map<String, TreeItem<AttributeItem>>> entry : featureAttributeMap.entrySet()) {
                IFeatureView feature = entry.getKey();
                if (feature == firstFeature)
                    continue;

                TreeItem<AttributeItem> matchingItem = entry.getValue().get(sourceAttr.getName());
                if (matchingItem == null) {
                    allFeaturesHaveAttribute = false;
                    break;
                }
                commonAttr.addRelatedAttribute(feature, matchingItem.getValue());
            }

            // 全ての地物が属性を持っている場合のみ追加
            if (allFeaturesHaveAttribute) {
                targetParent.getChildren().add(commonItem);

                if (!sourceItem.getChildren().isEmpty()) {
                    buildCommonAttributeTree(sourceItem.getChildren(), featureAttributeMap, commonItem,
                            firstFeature);
                }
            }
        }
    }
}
