package org.plateaubuilder.core.citymodel;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.geometry.AbstractLODSolidMeshView;
import org.plateaubuilder.core.citymodel.geometry.ILODSolidView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.LOD1SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD3SolidView;
import org.plateaubuilder.core.editor.Editor;

import javafx.scene.Node;

public class BuildingView extends ManagedGMLView<AbstractBuilding> implements IFeatureView {
    private LOD1SolidView lod1Solid;
    private LOD2SolidView lod2Solid;
    private LOD3SolidView lod3Solid;

    private List<BuildingInstallationView> buildingInstallationViews = new ArrayList<>();

    public BuildingView(AbstractBuilding original) {
        super(original);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int)newValue);
        });
    }

    public String getFeatureType() {
        return "bldg:Building";
    }

    public void toggleLODView(int lod) {
        var meshViews = new AbstractLODSolidMeshView[] {
                lod1Solid, lod2Solid, lod3Solid
        };
        for (int i = 1; i <= 3; ++i) {
            var meshView = meshViews[i - 1];
            if (meshView == null) {
                continue;
            }

            meshView.setVisible(lod == i);
        }

        // BuildingInstallation
        for (var buildingInstallationView : buildingInstallationViews) {
            for (int i = 2; i <= 3; i++) {
                var geometryView = buildingInstallationView.getGeometryView(i);
                if (geometryView != null) {
                    geometryView.setVisible(i == lod);
                }
            }
        }
    }

    public void setDefaultVisible() {
        if (this.lod3Solid != null) {
            this.lod3Solid.setVisible(true);
            if (this.lod2Solid != null) {
                this.lod2Solid.setVisible(false);
            }
            if (this.lod1Solid != null) {
                this.lod1Solid.setVisible(false);
            }
        } else if (this.lod2Solid != null) {
            this.lod2Solid.setVisible(true);
            if (this.lod1Solid != null) {
                this.lod1Solid.setVisible(false);
            }
        } else if (this.lod1Solid != null) {
            this.lod1Solid.setVisible(true);
        }
    }

    public Node getNode() {
        return this;
    }

    public ILODView getLODView(int lod) {
        return getSolid(lod);
    }

    public void setLODView(int lod, ILODView lodView) {
        switch (lod) {
        case 1:
            setLOD1Solid((LOD1SolidView) lodView);
            break;
        case 2:
            setLOD2Solid((LOD2SolidView) lodView);
            break;
        case 3:
            setLOD3Solid((LOD3SolidView) lodView);
            break;
        default:
            break;
        }
    }

    public ILODSolidView getSolid(int lod) {
        switch (lod) {
        case 1:
            return lod1Solid;
        case 2:
            return lod2Solid;
        case 3:
            return lod3Solid;
        default:
            return null;
        }
    }

    public void setLOD1Solid(LOD1SolidView solid) {
        if (this.lod1Solid == null) {
            this.getChildren().remove(this.lod1Solid);
        }
        this.lod1Solid = solid;
        this.getChildren().add(solid);
        solid.getTransformManipulator().updateOrigin();
    }

    public LOD1SolidView getLOD1Solid() {
        return this.lod1Solid;
    }

    public void setLOD2Solid(LOD2SolidView solid) {
        if (solid == null)
            return;

        if (this.lod2Solid != null) {
            this.getChildren().remove(this.lod2Solid);
        }
        this.lod2Solid = solid;
        this.getChildren().add(solid);
        solid.getTransformManipulator().updateOrigin();
    }

    public LOD2SolidView getLOD2Solid() {
        return this.lod2Solid;
    }

    public void setLOD3Solid(LOD3SolidView solid) {
        if (solid == null)
            return;

        if (this.lod3Solid != null) {
            this.getChildren().remove(this.lod3Solid);
        }
        this.lod3Solid = solid;
        this.getChildren().add(solid);
        solid.getTransformManipulator().updateOrigin();
    }

    public LOD3SolidView getLOD3Solid() {
        return this.lod3Solid;
    }

    public void addBuildingInstallationView(BuildingInstallationView buildingInstallationView) {
        if(buildingInstallationView == null)
            return;

        this.buildingInstallationViews.add(buildingInstallationView);
        this.getChildren().add(buildingInstallationView);
    }

    public void addBuildingPart(BuildingView buildingPart) {
        if (buildingPart == null)
            return;

        this.getChildren().add(buildingPart);
    }

    public Envelope getEnvelope() {
        return this.getGML().getBoundedBy().getEnvelope();
    }

    @Override
    public boolean isSetMeasuredHeight() {
        return getGML().isSetMeasuredHeight();
    }

    @Override
    public Length getMeasuredHeight() {
        return getGML().getMeasuredHeight();
    }

    @Override
    public void setMeasuredHeight(Length length) {
        getGML().setMeasuredHeight(length);
    }

    @Override
    public void unsetMeasuredHeight() {
        getGML().unsetMeasuredHeight();
    }

    @Override
    public List<String> getTexturePaths() {
        var paths = new HashSet<String>();
        if (lod2Solid != null) {
            paths.addAll(lod2Solid.getTexturePaths());
        }
        if (lod3Solid != null) {
            paths.addAll(lod3Solid.getTexturePaths());
        }
        for (var buildingInstallationView : buildingInstallationViews) {
            for (int i = 2; i <= 3; i++) {
                var geometryView = buildingInstallationView.getGeometryView(i);
                if (geometryView != null) {
                    paths.addAll(geometryView.getTexturePaths());
                }
            }
        }
        return new ArrayList<String>(paths);
    }

    @Override
    public List<ADEComponent> getADEComponents() {
        return getGML().getGenericApplicationPropertyOfAbstractBuilding();
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
        case "yearOfConstruction":
            return gml.getYearOfConstruction();
        case "yearOfDemolition":
            return gml.getYearOfDemolition();
        case "roofType":
            return gml.getRoofType();
        case "measuredHeight":
            return gml.getMeasuredHeight();
        case "storeysAboveGround":
            return gml.getStoreysAboveGround();
        case "storeysBelowGround":
            return gml.getStoreysBelowGround();
        case "storeyHeightsAboveGround":
            var storeyHeightsAboveGroundList = gml.getStoreyHeightsAboveGround().getDoubleOrNull();
            return storeyHeightsAboveGroundList.size() == 0 ? null : storeyHeightsAboveGroundList.get(0);
        case "storeyHeightsBelowGround":
            var storeyHeightsBelowGroundList = gml.getStoreyHeightsBelowGround().getDoubleOrNull();
            return storeyHeightsBelowGroundList.size() == 0 ? null : storeyHeightsBelowGroundList.get(0);
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
        case "yearOfConstruction":
            if (!(value instanceof LocalDate)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setYearOfConstruction((LocalDate) value);
            break;
        case "yearOfDemolition":
            if (!(value instanceof LocalDate)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setYearOfDemolition((LocalDate) value);
            break;
        case "roofType":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setRoofType((Code) value);
            break;
        case "measuredHeight":
            if (!(value instanceof Length)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setMeasuredHeight((Length) value);
            break;
        case "storeysAboveGround":
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setStoreysAboveGround((Integer) value);
            break;
        case "storeysBelowGround":
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setStoreysBelowGround((Integer) value);
            break;
        case "storeyHeightsAboveGround":
            if (!(value instanceof DoubleOrNull)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var storeyHeightsAboveGroundList = gml.getStoreyHeightsAboveGround().getDoubleOrNull();
            if (storeyHeightsAboveGroundList.size() == 0) {
                storeyHeightsAboveGroundList.remove(0);
            }
            storeyHeightsAboveGroundList.add(0, (DoubleOrNull) value);
            break;
        case "storeyHeightsBelowGround":
            if (!(value instanceof DoubleOrNull)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var storeyHeightsBelowGroundList = gml.getStoreyHeightsBelowGround().getDoubleOrNull();
            if (storeyHeightsBelowGroundList.size() == 0) {
                storeyHeightsBelowGroundList.remove(0);
            }
            storeyHeightsBelowGroundList.add(0, (DoubleOrNull) value);
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
            var attributeIndex = names[0].split("-");
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
        case "yearOfConstruction":
            return gml.isSetYearOfConstruction();
        case "yearOfDemolition":
            return gml.isSetYearOfDemolition();
        case "roofType":
            return gml.isSetRoofType();
        case "measuredHeight":
            return gml.isSetMeasuredHeight();
        case "storeysAboveGround":
            return gml.isSetStoreysAboveGround();
        case "storeysBelowGround":
            return gml.isSetStoreysBelowGround();
        case "storeyHeightsAboveGround":
            return gml.isSetStoreyHeightsAboveGround();
        case "storeyHeightsBelowGround":
            return gml.isSetStoreyHeightsBelowGround();
        case "creationDate":
            return gml.isSetCreationDate();
        case "terminationDate":
            return gml.isSetTerminationDate();
        default:
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
    }
}
