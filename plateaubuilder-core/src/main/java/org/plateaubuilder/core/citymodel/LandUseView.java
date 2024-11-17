package org.plateaubuilder.core.citymodel;

import java.time.ZonedDateTime;
import java.util.List;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.editor.Editor;

public class LandUseView extends AbstractMultiSurfaceView<LandUse> implements IFeatureView {
    public LandUseView(LandUse gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }

    public String getFeatureType() {
        return "luse:LandUse";
    }

    @Override
    public List<String> getSupportedLODTypes() {
        return List.of("LOD1");
    }

    @Override
    public List<ADEComponent> getADEComponents() {
        return getGML().getGenericApplicationPropertyOfLandUse();
    }

    @Override
    public Object getAttribute(String[] attributePaths) {
        var names = attributePaths[0].split(":");
        if (names.length != 2) {
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
        var name = names[1];
        var gml = getGML();
        switch (name) {
        case "class":
            return gml.getClazz();
        case "function":
            var functionList = gml.getFunction();
            return functionList.size() == 0 ? null : functionList.get(0);
        case "usage":
            var usageList = gml.getUsage();
            return usageList.size() == 0 ? null : usageList.get(0);
        case "creationDate":
            return gml.getCreationDate();
        case "terminationDate":
            return gml.getTerminationDate();
        default:
            var attributeIndex = name.split("-");
            if (attributeIndex.length == 2) {
                return getAttribute(attributeIndex[0], Integer.parseInt(attributeIndex[1]));
            }
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
    }

    private Object getAttribute(String name, int index /* 1-based */) {
        var gml = getGML();
        switch (name) {
        case "function":
            var functionList = gml.getFunction();
            return functionList.size() < index ? null : functionList.get(index - 1);
        case "usage":
            var usageList = gml.getUsage();
            return usageList.size() < index ? null : usageList.get(index - 1);
        default:
            throw new IllegalArgumentException("Invalid name: " + name);
        }
    }

    @Override
    public void setAttribute(String[] attributePaths, Object value) {
        var names = attributePaths[0].split(":");
        if (names.length != 2) {
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
        var name = names[1];
        var gml = getGML();
        switch (name) {
        case "class":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setClazz((Code) value);
            break;
        case "function":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var functionList = gml.getFunction();
            if (functionList.size() > 0) {
                functionList.remove(0);
            }
            functionList.add(0, (Code) value);
            break;
        case "usage":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var usageList = gml.getUsage();
            if (usageList.size() > 0) {
                usageList.remove(0);
            }
            usageList.add(0, (Code) value);
            break;
        case "creationDate":
            if (!(value instanceof ZonedDateTime)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setCreationDate((ZonedDateTime) value);
            break;
        case "terminationDate":
            if (!(value instanceof ZonedDateTime)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setTerminationDate((ZonedDateTime) value);
            break;
        default:
            var attributeIndex = name.split("-");
            if (attributeIndex.length == 2) {
                setAttribute(attributeIndex[0], Integer.parseInt(attributeIndex[1]), value);
            }
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
    }

    private void setAttribute(String name, int index /* 1-based */, Object value) {
        var gml = getGML();
        switch (name) {
        case "function":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var functionList = gml.getFunction();
            if (functionList.size() < index) {
                functionList.remove(index - 1);
            }
            functionList.add(index - 1, (Code) value);
            break;
        case "usage":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var usageList = gml.getUsage();
            if (usageList.size() < index) {
                usageList.remove(index - 1);
            }
            usageList.add(index - 1, (Code) value);
            break;
        default:
            throw new IllegalArgumentException("Invalid name: " + name);
        }
    }

    @Override
    public boolean isSetAttribute(String[] attributePaths) {
        var names = attributePaths[0].split(":");
        if (names.length != 2) {
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
        var name = names[1];
        var gml = getGML();
        switch (name) {
        case "class":
            return gml.isSetClazz();
        case "function":
            return gml.isSetFunction();
        case "usage":
            return gml.isSetUsage();
        case "creationDate":
            return gml.isSetCreationDate();
        case "terminationDate":
            return gml.isSetTerminationDate();
        default:
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
    }
}
