package org.plateau.citygmleditor.control;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.citygml4j.model.citygml.CityGMLClass;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.utils3d.geom.Vec2f;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.world.World;

import java.util.*;

public class BuildingSurfaceTypeView extends MeshView {
    private class FaceBufferSection {
        public int start;
        public int end;
        public PolygonView polygon;

        public FaceBufferSection(int start, int end, PolygonView polygon) {
            this.start = start;
            this.end = end;
            this.polygon = polygon;
        }
    }

    private final List<FaceBufferSection> faceBufferSections = new ArrayList<>();
    private final Map<CityGMLClass, Color> colorMap = buildingSurfaceColors();

    public static BuildingSurfaceTypeView createBuildingSurfaceTypeView() {
        var newInstance = new BuildingSurfaceTypeView();

        var node = (Group) World.getRoot3D();
        node.getChildren().add(newInstance);

        return newInstance;
    }

    public BuildingSurfaceTypeView() {
        var material = new PhongMaterial();
        material.setSelfIlluminationMap(createBuildingTypeColorImage());
        setMaterial(material);
    }

    public void setTarget(LOD2SolidView lod2Solid) {
        faceBufferSections.clear();

        var mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        mesh.getPoints().addAll(lod2Solid.getVertexBuffer().getBufferAsArray());
        mesh.getTexCoords().addAll(createTexCoords().getBufferAsArray());

        var faceBuffer = new FaceBuffer();
        var faceIndexOffset = 0;
        for (var boundarySurface : lod2Solid.getBoundaries()) {
            var texCoordIndex = getIndex(boundarySurface);
            for (var polygon : boundarySurface.getPolygons()) {
                var faceStartIndex = faceIndexOffset;
                var faceEndIndex = faceIndexOffset + polygon.getFaceBuffer().getFaceCount() - 1;
                faceIndexOffset = faceEndIndex + 1;
                faceBufferSections.add(new FaceBufferSection(faceStartIndex, faceEndIndex, polygon));

                var polygonFaceBuffer = polygon.getFaceBuffer();
                for (int i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
                    polygonFaceBuffer.setTexCoordIndex(i, texCoordIndex);
                }
                faceBuffer.addFaces(polygonFaceBuffer.getBuffer());
            }
        }
        mesh.getFaces().addAll(faceBuffer.getBufferAsArray());

        var surfaceNormals = new float[faceBuffer.getPointCount() * 3];
        Arrays.fill(surfaceNormals, 0);
        mesh.getNormals().addAll(surfaceNormals);

        var surfaceSmooth = new int[faceBuffer.getFaceCount()];
        Arrays.fill(surfaceSmooth, 1);
        mesh.getFaceSmoothingGroups().addAll(surfaceSmooth);

        setMesh(mesh);

        // 選択されたPolygonを取得
//        var selectedFace = pickResult.getIntersectedFace();
//        PolygonView selectedPolygon = null;
//        for (var entry : lod2Solid.getSurfaceDataPolygonsMap().entrySet()) {
//            // 選択されたMeshViewのMaterialと合致するkey(SurfaceData)を探す
//            var material = entry.getKey() == null
//                    ? World.getActiveInstance().getDefaultMaterial()
//                    : entry.getKey().getMaterial();
//            if (selectedMesh.getMaterial() != material)
//                continue;
//
//            for (var polygon : entry.getValue()) {
//                selectedFace -= polygon.getFaceBuffer().getFaceCount();
//                if (selectedFace < 0) {
//                    selectedPolygon = polygon;
//                    break;
//                }
//            }
//            if (selectedFace < 0)
//                break;
//        }
//
//        if (selectedPolygon != null) {
//            var vertexBuffer = new VertexBuffer();
//            var faceBuffer = new FaceBuffer();
//            PolygonMeshUtils.removeUnusedVertices(
//                    lod2Solid.getVertexBuffer(),
//                    selectedPolygon.getFaceBuffer(),
//                    vertexBuffer, faceBuffer
//            );
//            var mesh = new TriangleMesh();
//            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
//            mesh.getPoints().addAll(vertexBuffer.getBufferAsArray());
//
//            // TODO: 圧縮
//            mesh.getTexCoords().addAll(lod2Solid.getTexCoordBuffer().getBufferAsArray());
//
//            mesh.getFaces().addAll(faceBuffer.getBufferAsArray());
//
//            var normals = new float[faceBuffer.getPointCount() * 3];
//            Arrays.fill(normals, 0);
//            mesh.getNormals().addAll(normals);
//
//            var smooth = new int[faceBuffer.getFaceCount()];
//            Arrays.fill(smooth, 1);
//            mesh.getFaceSmoothingGroups().addAll(smooth);
//
//            outLine.setMesh(mesh);
//        }
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
        int index = 0;
        for (var clazz : colorMap.keySet()) {
            if (boundarySurfaceView.getOriginal().getCityGMLClass() == clazz)
                return index;
            index++;
        }
        return 0;
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

    private static Map<CityGMLClass, Color> buildingSurfaceColors() {
        var map = new HashMap<CityGMLClass, Color>();
        map.put(CityGMLClass.BUILDING_WALL_SURFACE, Color.web("#dcdcdc"));
        map.put(CityGMLClass.BUILDING_ROOF_SURFACE, Color.web("#00008b"));
        map.put(CityGMLClass.BUILDING_GROUND_SURFACE, Color.web("#000000"));
        map.put(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE, Color.web("#f0eb8c"));
        map.put(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE, Color.web("#66cdaa"));
        return map;
    }
}
