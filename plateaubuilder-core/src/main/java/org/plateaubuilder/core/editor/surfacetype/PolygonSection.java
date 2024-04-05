package org.plateaubuilder.core.editor.surfacetype;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingModuleComponent;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.plateaubuilder.core.utils3d.polygonmesh.FaceBuffer;

/**
 * FaceBuffer内での１つのポリゴンに該当するセクションを表します。
 */
public class PolygonSection {
    private BuildingModuleComponent property;
    private BuildingModuleComponent feature;
    private AbstractSurface surface;
    private FaceBuffer faceBuffer;
    private FaceBufferSection section;

    public PolygonSection(FaceBufferSection section, BoundarySurfaceProperty boundedBy, AbstractSurface surface, FaceBuffer faceBuffer) {
        this.property = boundedBy;
        this.feature = boundedBy.getFeature();
        this.surface = surface;
        this.section = section;
        this.faceBuffer = faceBuffer;
    }

    public PolygonSection(FaceBufferSection section, OpeningProperty opening, AbstractSurface surface, FaceBuffer faceBuffer) {
        this.property = opening;
        this.feature = opening.getFeature();
        this.surface = surface;
        this.section = section;
        this.faceBuffer = faceBuffer;
    }

    public OpeningProperty getOpening() {
        return (OpeningProperty) property;
    }

    public BoundarySurfaceProperty getBoundedBy() {
        return (BoundarySurfaceProperty) property;
    }

    public BuildingModuleComponent getComponent() {
        return property;
    }

    public BuildingModuleComponent getFeature() {
        return feature;
    }

    public void setProperty(BuildingModuleComponent property) {
        this.property = property;
        if (property instanceof OpeningProperty)
            feature = ((OpeningProperty) property).getOpening();
        else
            feature = ((BoundarySurfaceProperty) property).getBoundarySurface();
    }

    public AbstractSurface getSurface() {
        return surface;
    }

    public FaceBuffer getFaceBuffer() {
        return faceBuffer;
    }

    public void setFaceBuffer(FaceBuffer faceBuffer) {
        this.faceBuffer = faceBuffer;
    }

    public FaceBufferSection getSection() {
        return section;
    }

    public void setSection(FaceBufferSection section) {
        this.section = section;
    }

    public CityGMLClass getCityGMLClass() {
        if (property instanceof OpeningProperty)
            return ((OpeningProperty) property).getOpening().getCityGMLClass();
        else
            return ((BoundarySurfaceProperty) property).getBoundarySurface().getCityGMLClass();
    }

    public boolean isOpening() {
        return property instanceof OpeningProperty;
    }
}
