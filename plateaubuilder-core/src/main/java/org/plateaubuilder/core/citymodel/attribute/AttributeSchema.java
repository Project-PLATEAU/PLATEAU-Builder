package org.plateaubuilder.core.citymodel.attribute;

import static java.util.Map.entry;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.core.RelativeToTerrain;
import org.citygml4j.model.citygml.core.RelativeToWater;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.model.xal.DependentLocality;
import org.citygml4j.model.xal.GrPostal;
import org.citygml4j.model.xal.Premise;
import org.plateaubuilder.core.editor.Editor;

public class AttributeSchema {
    private static final Set<Class> ignoreTypes = Set.of(ADEComponent.class, BuildingInstallationProperty.class, IntBuildingInstallationProperty.class,
            BoundarySurfaceProperty.class, BuildingPartProperty.class, InteriorRoomProperty.class, ModelObject.class, Premise.class, DependentLocality.class,
            AddressProperty.class, GeometricComplexProperty.class, ExternalReference.class, AbstractGenericAttribute.class, GeneralizationRelation.class,
            AppearanceProperty.class, RelativeToTerrain.class, RelativeToWater.class, TrafficAreaProperty.class, AuxiliaryTrafficAreaProperty.class,
            ImplicitRepresentationProperty.class);

    private static final Set<Class> breakTypes = Set.of(Code.class);

    private static final Map<String, String> replaceNamespaceMap = Map.ofEntries(entry("org.citygml4j.model.citygml.building", "bldg"),
            entry("org.citygml4j.model.citygml.cityfurniture", "frn"), entry("org.citygml4j.model.citygml.landuse", "luse"),
            entry("org.citygml4j.model.citygml.transportation", "tran"), entry("org.citygml4j.model.citygml.vegetation", "veg"),
            entry("org.citygml4j.model.citygml.waterbody", "wtr"), entry("org.citygml4j.model.citygml.core", "core"), entry("org.citygml4j.model.xal", "xAL"));

    private static final Map<String, String> replaceFieldMap = Map.ofEntries(entry("clazz", "class"));

    private static final Map<String, Occurs> occurEntryMap = Map.ofEntries(entry("class", new Occurs("0", null)),
            entry("function", new Occurs("0", "unbounded")),
            entry("usage", new Occurs("0", "unbounded")), entry("creationDate", new Occurs("0", null)), entry("terminationDate", new Occurs("0", null)),
            entry("yearOfConstruction", new Occurs("0", null)), entry("yearOfDemolition", new Occurs("0", null)), entry("roofType", new Occurs("0", null)),
            entry("measuredHeight", new Occurs("0", null)), entry("storeysAboveGround", new Occurs("0", null)),
            entry("storeysBelowGround", new Occurs("0", null)), entry("storeyHeightsAboveGround", new Occurs("0", null)),
            entry("storeyHeightsBelowGround", new Occurs("0", null)), entry("species", new Occurs("0", null)), entry("height", new Occurs("0", null)),
            entry("trunkDiameter", new Occurs("0", null)), entry("crownDiameter", new Occurs("0", null)));

    private final Map<String, Attributes> attributeMap = new HashMap<>();

    public Attributes getAttributes(String featureType) {
        if (attributeMap.containsKey(featureType)) {
            return attributeMap.get(featureType);
        }

        return createAttributes(featureType);
    }

    public List<String> getAttributeNames(String featureType) {
        if (attributeMap.containsKey(featureType)) {
            return attributeMap.get(featureType).getAttributeNames();
        }

        return createAttributes(featureType).getAllAttributeNames();
    }

    private Attributes createAttributes(String featureType) {
        Class clazz = null;
        switch (featureType) {
        case "bldg:Building":
            clazz = org.citygml4j.model.citygml.building.AbstractBuilding.class;
            break;
        case "frn:CityFurniture":
            clazz = org.citygml4j.model.citygml.cityfurniture.CityFurniture.class;
            break;
        case "luse:LandUse":
            clazz = org.citygml4j.model.citygml.landuse.LandUse.class;
            break;
        case "tran:Road":
            clazz = org.citygml4j.model.citygml.transportation.Road.class;
            break;
        case "veg:PlantCover":
            clazz = org.citygml4j.model.citygml.vegetation.PlantCover.class;
            break;
        case "veg:SolitaryVegetationObject":
            clazz = org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject.class;
            break;
        case "wtr:WaterBody":
            clazz = org.citygml4j.model.citygml.waterbody.WaterBody.class;
            break;
        default:
            break;
        }

        var attributes = clazz != null ? createAttributes(clazz) : createUrfAttributes(featureType);
        attributeMap.put(featureType, attributes);
        return attributes;
    }

    private Attributes createAttributes(Class clazz) {
        var attributes = new Attributes();

        // 地物属性
        attributes.addAll(createAttribute(clazz, null));

        var namespace = clazz.getPackageName();
        namespace = replaceNamespaceMap.containsKey(namespace) ? replaceNamespaceMap.get(namespace) : namespace.substring(namespace.lastIndexOf(".") + 1);

        // uro属性
        var methods = clazz.getDeclaredMethods();
        for (var method : methods) {
            var methodName = method.getName();
            if (methodName.startsWith("getGenericApplicationPropertyOf")) {
                String name;
                if (namespace.equals("veg")) {
                    // vegetationは共通でGenericApplicationPropertyOfVegetationObjectになっている
                    name = "GenericApplicationPropertyOfVegetationObject";
                } else {
                    name = methodName.substring(3);
                }
                attributes.addAll(createUroAttributes(namespace, String.format("_%s", name)));
                break;
            }
        }

        return attributes;
    }

    private List<Attribute> createAttribute(Class clazz, Attribute parent) {
        var attributes = new ArrayList<Attribute>();

        // namespaceを変換する
        var namespace = clazz.getPackageName();
        namespace = replaceNamespaceMap.containsKey(namespace) ? replaceNamespaceMap.get(namespace) : namespace.substring(namespace.lastIndexOf(".") + 1);

        var fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            // 必要に応じてフィールド名を変換する
            var fieldName = field.getName();
            if (replaceFieldMap.containsKey(fieldName)) {
                fieldName = replaceFieldMap.get(fieldName);
            }

            Attribute attribute = null;
            var type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                var parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType().equals(java.util.List.class)) {
                    attribute = new Attribute(namespace, fieldName, parameterizedType.getRawType(), parameterizedType.getActualTypeArguments()[0]);
                } else {
                    // DEBUG用 Listの場合しか考慮していない
                    System.out.println("### error: not list ###");
                    continue;
                }
            } else {
                attribute = new Attribute(namespace, fieldName, type);
            }

            var current = (Class) attribute.getActualTypeOrType();

            // 無視するプロパティはスキップ
            if (ignoreTypes.contains(current) || current.getName().contains("org.citygml4j.model.gml.geometry")) {
                continue;
            }

            var occurs = occurEntryMap.get(fieldName);
            if (occurs != null) {
                attribute.setMinOccurs(occurs.getMin());
                attribute.setMaxOccurs(occurs.getMax());
            }

            attributes.add(attribute);
            if (parent != null) {
                parent.addChild(attribute);
            }

            if (breakTypes.contains(current)) {
                continue;
            }

            var genericSuperclass = current.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                var parameterizedSuperType = (ParameterizedType) genericSuperclass;
                var typeArgs = parameterizedSuperType.getActualTypeArguments();
                if (typeArgs != null && typeArgs.length > 0) {
                    createAttribute((Class) typeArgs[0], attribute);
                } else {
                    createAttribute((Class) parameterizedSuperType.getRawType(), attribute);
                }
            } else {
                var interfaces = current.getInterfaces();
                var isChild = false;
                var isGrPostal = false;
                for (Class i : interfaces) {
                    if (i.equals(Child.class)) {
                        isChild = true;
                    }
                    if (i.equals(GrPostal.class)) {
                        isGrPostal = true;
                    }
                }

                if (isChild && !isGrPostal) {
                    createAttribute(current, attribute);
                }
            }
        }

        var superClass = clazz.getSuperclass();
        if (superClass != null && replaceNamespaceMap.containsKey(superClass.getPackageName())) {
            attributes.addAll(createAttribute(superClass, parent));
        }

        return attributes;
    }

    private List<Attribute> createUroAttributes(String namespace, String substitutionGroup) {
        var uroSchemaDocument = Editor.getUroSchemaDocument();
        return uroSchemaDocument.getGenericApplicationProperties(namespace, substitutionGroup);
    }

    private Attributes createUrfAttributes(String featureType) {
        var attributes = new Attributes();
        var urfSchemaDocument = Editor.getUrfSchemaDocument();
        attributes.addAll(urfSchemaDocument.getUrfAttributes(featureType));
        return attributes;
    }
}
