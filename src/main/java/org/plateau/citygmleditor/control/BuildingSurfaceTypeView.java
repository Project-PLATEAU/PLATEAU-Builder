package org.plateau.citygmleditor.control;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.utils3d.geom.Vec2f;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;

import java.util.*;

public class BuildingSurfaceTypeView extends MeshView {
    private final List<PolygonSection> faceBufferSections = new ArrayList<>();
    private final Map<CityGMLClass, Color> colorMap;
    private AbstractBuilding building;
    private ILODSolidView solid;

    private final FaceBuffer faceBuffer = new FaceBuffer();

    private int lod;

    public BuildingSurfaceTypeView(int lod) {
        this.lod = lod;
        colorMap = getBuildingSurfaceColors(lod);

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
            var texCoordIndex = getIndex(boundarySurface);

            var polygons = new ArrayList<PolygonView>(boundarySurface.getPolygons());

//            for (var opening : boundarySurface.getOpenings()) {
//                polygons.addAll(opening);
//            }

            for (var polygon : polygons) {
                var pointStartIndex = faceIndexOffset;
                var pointEndIndex = faceIndexOffset + polygon.getFaceBuffer().getPointCount() - 1;
                faceIndexOffset = pointEndIndex + 1;
                faceBufferSections.add(new PolygonSection(pointStartIndex, pointEndIndex, boundarySurface.getOriginal(), polygon.getOriginal(), polygon.getFaceBuffer()));

                var polygonFaceBuffer = new FaceBuffer();
                polygonFaceBuffer.addFaces(polygon.getFaceBuffer().getBuffer());
                for (int i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
                    polygonFaceBuffer.setTexCoordIndex(i, texCoordIndex);
                }
                faceBuffer.addFaces(polygonFaceBuffer.getBuffer());
            }
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

    public void setTarget(AbstractBuilding building, ILODSolidView solid) {
        this.building = building;
        this.solid = solid;
    }

    public void setSurfaceType(PolygonSection section, CityGMLClass clazz) {
        if (section.getBoundarySurface().getCityGMLClass() == clazz)
            return;

        var mesh = (TriangleMesh)getMesh();
        for (int i = section.start; i <= section.end; ++i) {
            faceBuffer.setTexCoordIndex(i, getIndex(clazz));
        }
        mesh.getFaces().setAll(faceBuffer.getBufferAsArray());

        var boundarySurface = section.getBoundarySurface();
        boundarySurface.getLod2MultiSurface().setMultiSurface(null);
        var polygon = section.getPolygon();

        BoundarySurfaceProperty boundedBy = findBoundarySurfaceProperty(boundarySurface);
        if (boundedBy != null)
            building.getBoundedBySurface().remove(boundedBy);

        try {
            var ctor = clazz.getModelClass().getDeclaredConstructor();
            var newBoundarySurface = (AbstractBoundarySurface)ctor.newInstance();
            var surfaces = new ArrayList<AbstractSurface>();
            surfaces.add(polygon);
            newBoundarySurface.setLod2MultiSurface(new MultiSurfaceProperty(new MultiSurface(surfaces)));
            building.addBoundedBySurface(new BoundarySurfaceProperty(newBoundarySurface));
            section.setBoundarySurface(newBoundarySurface);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BoundarySurfaceProperty findBoundarySurfaceProperty(AbstractBoundarySurface boundarySurface) {
        BoundarySurfaceProperty targetBoundedBy = null;
        for (var boundedBy : building.getBoundedBySurface()) {
            if (boundedBy.getBoundarySurface() != boundarySurface)
                continue;

            targetBoundedBy = boundedBy;
            break;
        }
        return targetBoundedBy;
    }

    public CityGMLClass getSurfaceType(PolygonSection section) {
        var index = faceBuffer.getTexCoordIndex(section.start);
        return getClazz(index);
    }

    public PolygonSection getSection(PickResult pickResult) {
        // 選択されたPolygonを取得
        var selectedFace = pickResult.getIntersectedFace() * 3;
        for (var section : faceBufferSections) {
            if (selectedFace >= section.start && selectedFace <= section.end) {
                return section;
            }
        }

        return null;
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
        var colorCount = colorMap.values().size();
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
        for (var key : colorMap.keySet()) {
            if (key == clazz)
                return index;
            index++;
        }
        return 0;
    }

    private CityGMLClass getClazz(int texCoordIndex) {
        int index = 0;
        for (var key : colorMap.keySet()) {
            if (index == texCoordIndex)
                return key;
            index++;
        }
        return null;
    }

    private Image createBuildingTypeColorImage() {
        var colorCount = colorMap.values().size();
        WritableImage image = new WritableImage(colorCount, 1);
        PixelWriter writer = image.getPixelWriter();
        int index = 0;
        for (var color : colorMap.values()) {
            writer.setColor(index, 0, color);
            index++;
        }
        return image;
    }

    public static Map<CityGMLClass, Color> getBuildingSurfaceColors(int lod) {
        switch (lod) {
            case 2: return getLOD2buildingSurfaceColors();
            case 3: return getLOD3buildingSurfaceColors();
            default: throw new OutOfRangeException(lod, 2, 3);
        }
    }

    public static Map<CityGMLClass, Color> getLOD2buildingSurfaceColors() {
        var map = new HashMap<CityGMLClass, Color>();
        map.put(CityGMLClass.BUILDING_WALL_SURFACE, Color.web("#dcdcdc"));
        map.put(CityGMLClass.BUILDING_ROOF_SURFACE, Color.web("#00008b"));
        map.put(CityGMLClass.BUILDING_GROUND_SURFACE, Color.web("#000000"));
        map.put(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE, Color.web("#f0eb8c"));
        map.put(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE, Color.web("#66cdaa"));
        return map;
    }

    public static Map<CityGMLClass, Color> getLOD3buildingSurfaceColors() {
        var map = getLOD2buildingSurfaceColors();
        map.put(CityGMLClass.BUILDING_DOOR, Color.web("#00a0e9"));
        map.put(CityGMLClass.BUILDING_WINDOW, Color.web("#909090"));
        return map;
    }
}
