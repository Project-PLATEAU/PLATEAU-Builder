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
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.utils3d.geom.Vec2f;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;

import java.util.*;

public class BuildingSurfaceTypeView extends MeshView {
    private AbstractBuilding targetBuilding;

    private final List<SurfacePolygonSection> faceBufferSections = new ArrayList<>();
    private final Map<CityGMLClass, Color> colorMap = buildingSurfaceColors();

    private final SelectionOutline selectionOutline = new SelectionOutline();
    private final FaceBuffer faceBuffer = new FaceBuffer();

    public BuildingSurfaceTypeView() {
        var material = new PhongMaterial();
        material.setSelfIlluminationMap(createBuildingTypeColorImage());
        setMaterial(material);
//        setDepthTest(DepthTest.DISABLE);
        setViewOrder(-1);
    }

    private void updateVisual(LOD2SolidView lod2Solid) {
        faceBufferSections.clear();

        var mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);

        var vertexBuffer = lod2Solid.getVertexBuffer();
        mesh.getPoints().addAll(vertexBuffer.getBufferAsArray());

        var texCoordBuffer = createTexCoords();
        mesh.getTexCoords().addAll(texCoordBuffer.getBufferAsArray());

        var faceIndexOffset = 0;
        for (var boundarySurface : lod2Solid.getBoundaries()) {
            var texCoordIndex = getIndex(boundarySurface);
            for (var polygon : boundarySurface.getPolygons()) {
                var pointStartIndex = faceIndexOffset;
                var pointEndIndex = faceIndexOffset + polygon.getFaceBuffer().getPointCount() - 1;
                faceIndexOffset = pointEndIndex + 1;
                faceBufferSections.add(new SurfacePolygonSection(pointStartIndex, pointEndIndex, polygon));

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

    public void setTarget(LOD2SolidView lod2SolidView) {
        updateVisual(lod2SolidView);
    }

//    public void registerClickEvent(Scene scene) {
//        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
//            PickResult pickResult = event.getPickResult();
//            updateSelectionOutline(pickResult);
//        });
//    }

    public void updateSurfaceType(SurfacePolygonSection section, CityGMLClass clazz) {
        var mesh = (TriangleMesh)getMesh();
        for (int i = section.start; i <= section.end; ++i) {
            faceBuffer.setTexCoordIndex(i, getIndex(clazz));
        }
        mesh.getFaces().setAll(faceBuffer.getBufferAsArray());
    }

    public CityGMLClass getSurfaceType(SurfacePolygonSection section) {
        var index = faceBuffer.getTexCoordIndex(section.start);
        return getClazz(index);
    }

    public SurfacePolygonSection getSection(PickResult pickResult) {
        // 選択されたPolygonを取得
        var selectedFace = pickResult.getIntersectedFace() * 3;
        for (var section : faceBufferSections) {
            if (selectedFace >= section.start && selectedFace <= section.end) {
                return section;
            }
        }

        return null;
    }

    public void updateSelectionOutLine(PolygonView selectedPolygon, MeshView outLine) {
        if (selectedPolygon != null) {
            var original = selectedPolygon.getExteriorRing().getOriginal();
            System.out.println("ExteriorRing");
            for (var c : original.getCoord()) {
                System.out.println(c);
            }
            for (var i = 0; i < selectedPolygon.getInteriorRings().size(); i++) {
                original = selectedPolygon.getInteriorRings().get(i).getOriginal();
                System.out.println("InteriorRing" + i);
                for (var c : original.getCoord()) {
                    System.out.println(c);
                }
            }

            var selfMesh = (TriangleMesh)getMesh();
            var mesh = new TriangleMesh();
            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
            mesh.getPoints().addAll(selfMesh.getPoints());
            mesh.getTexCoords().addAll(selfMesh.getTexCoords());

            var faceBuffer = new FaceBuffer();
            faceBuffer.addFaces(selectedPolygon.getFaceBuffer().getBuffer());
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

    public static Map<CityGMLClass, Color> buildingSurfaceColors() {
        var map = new HashMap<CityGMLClass, Color>();
        map.put(CityGMLClass.BUILDING_WALL_SURFACE, Color.web("#dcdcdc"));
        map.put(CityGMLClass.BUILDING_ROOF_SURFACE, Color.web("#00008b"));
        map.put(CityGMLClass.BUILDING_GROUND_SURFACE, Color.web("#000000"));
        map.put(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE, Color.web("#f0eb8c"));
        map.put(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE, Color.web("#66cdaa"));
        return map;
    }
}
