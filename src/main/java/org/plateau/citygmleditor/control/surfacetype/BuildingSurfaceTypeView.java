package org.plateau.citygmleditor.control.surfacetype;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.control.FaceBufferSection;
import org.plateau.citygmleditor.control.PolygonSection;
import org.plateau.citygmleditor.utils3d.geom.Vec2f;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;

import java.util.*;

public class BuildingSurfaceTypeView extends MeshView {
    private final List<PolygonSection> faceBufferSections = new ArrayList<>();
    private AbstractBuilding building;
    private ILODSolidView solid;

    private final FaceBuffer faceBuffer = new FaceBuffer();

    private int lod;

    public BuildingSurfaceTypeView(int lod) {
        this.lod = lod;

        var material = new PhongMaterial();
        material.setSelfIlluminationMap(createBuildingTypeColorImage());
        setMaterial(material);
        setViewOrder(-1);
    }

    public void updateVisual() {
        faceBufferSections.clear();

        var mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);

        var vertexBuffer = solid.getVertexBuffer();
        mesh.getPoints().addAll(vertexBuffer.getBufferAsArray());

        var texCoordBuffer = createTexCoords();
        mesh.getTexCoords().addAll(texCoordBuffer.getBufferAsArray());

        var faceIndexOffset = 0;
        for (var boundarySurface : solid.getBoundaries()) {
            // 開口部
            for (var opening : boundarySurface.getOpenings()) {
                var openingTexCoordIndex = getIndex(opening.getOriginal().getCityGMLClass());
                faceIndexOffset = createSections(findOpeningProperty(boundarySurface.getOriginal(), opening.getOriginal()), opening.getPolygons(), faceIndexOffset, openingTexCoordIndex);
            }
            var boundaryTexCoordIndex = getIndex(boundarySurface.getOriginal().getCityGMLClass());
            faceIndexOffset = createSections(findBoundarySurfaceProperty(boundarySurface.getOriginal()), boundarySurface.getPolygons(), faceIndexOffset, boundaryTexCoordIndex);

        }
        mesh.getFaces().addAll(faceBuffer.getBufferAsArray());

        var normals = new float[faceBuffer.getPointCount() * 3];
        Arrays.fill(normals, 0);
        mesh.getNormals().addAll(normals);

        var smooths = new int[faceBuffer.getFaceCount()];
        Arrays.fill(smooths, 1);
        mesh.getFaceSmoothingGroups().addAll(smooths);

        setMesh(mesh);
    }

    private BoundarySurfaceProperty findBoundarySurfaceProperty(AbstractBoundarySurface boundarySurface) {
        for (var boundedBy : building.getBoundedBySurface()) {
            if (boundedBy.getBoundarySurface() == boundarySurface)
                return boundedBy;
        }

        return null;
    }

    private OpeningProperty findOpeningProperty(AbstractBoundarySurface parentBoundary, AbstractOpening opening) {
        for (var openingProperty : parentBoundary.getOpening()) {
            if (openingProperty.getOpening() == opening)
                return openingProperty;
        }

        return null;
    }

    private int createSections(BuildingModuleComponent component, List<PolygonView> polygons, int faceIndexOffset, int texCoordIndex) {
        for (var polygon : polygons) {
            var pointStartIndex = faceIndexOffset;
            var pointEndIndex = faceIndexOffset + polygon.getFaceBuffer().getPointCount() - 1;
            faceIndexOffset = pointEndIndex + 1;

            if (component instanceof BoundarySurfaceProperty)
                faceBufferSections.add(new PolygonSection(
                    new FaceBufferSection(pointStartIndex, pointEndIndex),
                        (BoundarySurfaceProperty)component, polygon.getOriginal(), polygon.getFaceBuffer()));
            else if (component instanceof OpeningProperty)
                faceBufferSections.add(new PolygonSection(
                        new FaceBufferSection(pointStartIndex, pointEndIndex),
                        (OpeningProperty) component, polygon.getOriginal(), polygon.getFaceBuffer()));
            else
                throw new RuntimeException();

            var polygonFaceBuffer = new FaceBuffer();
            polygonFaceBuffer.addFaces(polygon.getFaceBuffer().getBuffer());
            for (int i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
                polygonFaceBuffer.setTexCoordIndex(i, texCoordIndex);
            }
            faceBuffer.addFaces(polygonFaceBuffer.getBuffer());
        }
        return faceIndexOffset;
    }

    public void setTarget(AbstractBuilding building, ILODSolidView solid) {
        this.building = building;
        this.solid = solid;
    }

    public AbstractBuilding getTargetBuilding() {
        return building;
    }

    public int getTargetLod() {
        return lod;
    }

    public void updateView(Map<AbstractSurface, BuildingModuleComponent> propertyMap) {
        for (var section : faceBufferSections) {
            var newProperty = propertyMap.get(section.getSurface());

            if (newProperty == null)
                throw new RuntimeException();

            BuildingModuleComponent feature;
            if (newProperty instanceof BoundarySurfaceProperty)
                feature = ((BoundarySurfaceProperty)newProperty).getFeature();
            else
                feature = ((OpeningProperty)newProperty).getFeature();

            if (feature == section.getFeature()) {
                section.setProperty(newProperty);
                continue;
            }

            section.setProperty(newProperty);

            for (int i = section.getSection().getStart(); i <= section.getSection().getEnd(); ++i) {
                faceBuffer.setTexCoordIndex(i, getIndex(feature.getCityGMLClass()));
            }
        }

        // viewの更新
        var mesh = (TriangleMesh)getMesh();
        mesh.getFaces().setAll(faceBuffer.getBufferAsArray());
    }

    public PolygonSection getPolygonSection(PickResult pickResult) {
        // 選択されたPolygonを取得
        PolygonSection selectedSection = null;
        var selectedFace = pickResult.getIntersectedFace() * 3;
        for (var section : faceBufferSections) {
            if (selectedFace >= section.getSection().getStart() && selectedFace <= section.getSection().getEnd()) {
                selectedSection = section;
                break;
            }
        }

        return selectedSection;
    }

    public List<PolygonSection> getComponentSection(PolygonSection section) {
        return getComponentSection(section.getComponent());
    }

    public List<PolygonSection> getComponentSection(BuildingModuleComponent component) {
        var sections = new ArrayList<PolygonSection>();

        if (component == null)
            return sections;

        for (var eachSection : faceBufferSections) {
            if (eachSection.getComponent() == component)
                sections.add(eachSection);
        }

        return sections;
    }

    public void updateSelectionOutLine(FaceBuffer selectedFaces, MeshView outLine) {
        var selfMesh = (TriangleMesh)getMesh();
        var mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        mesh.getPoints().addAll(selfMesh.getPoints());
        mesh.getTexCoords().addAll(selfMesh.getTexCoords());

        var faceBuffer = new FaceBuffer();
        faceBuffer.addFaces(selectedFaces.getBuffer());
        for (int i = 0; i < faceBuffer.getPointCount(); ++i) {
            faceBuffer.setTexCoordIndex(i, 0);
        }
        mesh.getFaces().addAll(faceBuffer.getBufferAsArray());
        mesh.getNormals().addAll(selfMesh.getNormals());

        var smooths = new int[faceBuffer.getFaceCount()];
        Arrays.fill(smooths, 1);
        mesh.getFaceSmoothingGroups().addAll(smooths);

        outLine.setMesh(mesh);
    }

    private TexCoordBuffer createTexCoords() {
        var texCoordBuffer = new TexCoordBuffer();
        var colorCount = getLOD3BuildingComponentTypes().size();
        for (int i = 0; i < colorCount; ++i) {
            texCoordBuffer.addTexCoord(new Vec2f((float)i / colorCount, 0), false);
        }
        return texCoordBuffer;
    }

    private int getIndex(BoundarySurfaceView boundarySurfaceView) {
        var boundaryClazz = boundarySurfaceView.getOriginal().getCityGMLClass();
        return getIndex(boundaryClazz);
    }

    private int getIndex(CityGMLClass clazz) {
        int index = 0;
        for (var key : getLOD3BuildingComponentTypes()) {
            if (key == clazz)
                return index;
            index++;
        }
        return 0;
    }

    private CityGMLClass getClazz(int texCoordIndex) {
        int index = 0;
        for (var key : getLOD3BuildingComponentTypes()) {
            if (index == texCoordIndex)
                return key;
            index++;
        }
        return null;
    }

    private Image createBuildingTypeColorImage() {
        var colorCount = getLOD3BuildingComponentTypes().size();
        WritableImage image = new WritableImage(colorCount, 1);
        PixelWriter writer = image.getPixelWriter();
        int index = 0;
        for (var clazz : getLOD3BuildingComponentTypes()) {
            writer.setColor(index, 0, getBuildingSurfaceColor(clazz));
            index++;
        }
        return image;
    }

    public static Color getBuildingSurfaceColor(CityGMLClass clazz) {
        switch (clazz) {
            case BUILDING_WALL_SURFACE: return Color.web("#dcdcdcff");
            case BUILDING_ROOF_SURFACE: return Color.web("#00008bff");
            case BUILDING_GROUND_SURFACE: return Color.web("#000000ff");
            case OUTER_BUILDING_CEILING_SURFACE: return Color.web("#f0eb8cff");
            case OUTER_BUILDING_FLOOR_SURFACE: return Color.web("#66cdaaff");
            case BUILDING_DOOR: return Color.web("#00a0e9ff");
            case BUILDING_WINDOW: return Color.web("#909090ff");
        }

        throw new IllegalArgumentException();
    }

    public static List<CityGMLClass> getLOD2BuildingComponentTypes() {
        var list = new ArrayList<CityGMLClass>();
        list.add(CityGMLClass.BUILDING_WALL_SURFACE);
        list.add(CityGMLClass.BUILDING_ROOF_SURFACE);
        list.add(CityGMLClass.BUILDING_GROUND_SURFACE);
        list.add(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE);
        list.add(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE);
        return list;
    }

    public static List<CityGMLClass> getLOD3BuildingComponentTypes() {
        var list = getLOD2BuildingComponentTypes();
        list.add(CityGMLClass.BUILDING_WINDOW);
        list.add(CityGMLClass.BUILDING_DOOR);
        return list;
    }

    public static List<CityGMLClass> getOpeningComponentTypes() {
        var list = new ArrayList<CityGMLClass>();
        list.add(CityGMLClass.BUILDING_WINDOW);
        list.add(CityGMLClass.BUILDING_DOOR);
        return list;
    }
}
