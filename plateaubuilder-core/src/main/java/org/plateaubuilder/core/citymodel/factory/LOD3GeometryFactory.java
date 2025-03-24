package org.plateaubuilder.core.citymodel.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.citymodel.geometry.AbstractLODGeometryMeshView;
import org.plateaubuilder.core.citymodel.geometry.LOD3GeometryView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD3GeometryFactory extends GeometryFactory {
    public LOD3GeometryFactory(CityModelView target) {
        super(target);
    }

    public LOD3GeometryView createLOD3Geometry(SolitaryVegetationObject gmlObject) {
        var geometry = gmlObject.getLod3Geometry();
        return geometry != null ? createLOD3Geometry(geometry.getObject()) : null;
    }

    public LOD3GeometryView createLOD3Geometry(CityFurniture gmlObject) {
        var geometry = gmlObject.getLod3Geometry();
        return geometry != null ? createLOD3Geometry(geometry.getObject()) : null;
    }

    public LOD3GeometryView createLOD3Geometry(BuildingInstallation gmlObject) {
        var geometry = gmlObject.getLod3Geometry();
        return geometry != null ? createLOD3Geometry(geometry.getObject()) : null;
    }

    public LOD3GeometryView createLOD3Geometry(AbstractGeometry geometry) {
        var geometryView = new LOD3GeometryView(geometry, vertexBuffer, texCoordBuffer);
        if (geometry instanceof Solid) {
            geometryView.setPolygons(createPolygonViews((Solid) geometry));
            geometryView.setGmlType("Solid");
        } else if (geometry instanceof MultiSurface) {
            geometryView.setPolygons(createPolygonViews((MultiSurface) geometry));
            geometryView.setGmlType("MultiSurface");
        } else if (geometry instanceof CompositeSurface) {
            geometryView.setPolygons(createPolygonViews((CompositeSurface) geometry));
            geometryView.setGmlType("CompositeSurface");
        }

        setMaterial(geometryView);

        return geometryView;
    }

    private List<PolygonView> createPolygonViews(Solid solid) {
        var exterior = solid.getExterior();
        var compositeSurface = (CompositeSurface) exterior.getObject();

        List<SurfaceProperty> surfaceMember = compositeSurface.getSurfaceMember();

        var polygons = new ArrayList<PolygonView>();

        for (SurfaceProperty surfaceMemberElement : surfaceMember) {
            var polygon = (Polygon) surfaceMemberElement.getSurface();
            if (polygon == null) {
                continue;
            }

            var polygonObject = createPolygon(polygon);
            polygons.add(polygonObject);
        }

        return polygons;
    }

    private List<PolygonView> createPolygonViews(MultiSurface multiSurface) {
        List<SurfaceProperty> surfaceMember = multiSurface.getSurfaceMember();

        var polygons = new ArrayList<PolygonView>();
        for (SurfaceProperty surfaceMemberElement : surfaceMember) {
            var surface = surfaceMemberElement.getSurface();
            if (surface instanceof Polygon) {
                var polygon = (Polygon) surface;
                var polygonObject = createPolygon(polygon, polygon.getId());
                polygons.add(polygonObject);
            } else if (surface instanceof CompositeSurface) {
                var compositeSurface = (CompositeSurface) surface;
                for (var member : compositeSurface.getSurfaceMember()) {
                    var polygon = (Polygon) member.getSurface();
                    var polygonObject = createPolygon(polygon, compositeSurface.getId());
                    polygons.add(polygonObject);
                }
            }
        }

        return polygons;
    }

    private List<PolygonView> createPolygonViews(CompositeSurface multiSurface) {
        List<SurfaceProperty> surfaceMember = multiSurface.getSurfaceMember();

        var polygons = new ArrayList<PolygonView>();
        for (SurfaceProperty surfaceMemberElement : surfaceMember) {
            var surface = surfaceMemberElement.getSurface();
            if (surface instanceof Polygon) {
                var polygon = (Polygon) surface;
                var polygonObject = createPolygon(polygon, polygon.getId());
                polygons.add(polygonObject);
            } else if (surface instanceof CompositeSurface) {
                var compositeSurface = (CompositeSurface) surface;
                for (var member : compositeSurface.getSurfaceMember()) {
                    var polygon = (Polygon) member.getSurface();
                    var polygonObject = createPolygon(polygon, compositeSurface.getId());
                    polygons.add(polygonObject);
                }
            }
        }

        return polygons;
    }

    private void setMaterial(AbstractLODGeometryMeshView geometryView) {
        var polygonsMap = geometryView.getSurfaceDataPolygonsMap();
        // 1メッシュにつき1マテリアルしか登録できないため、マテリアルごとに別メッシュとして生成
        for (Map.Entry<SurfaceDataView, List<PolygonView>> entry : polygonsMap.entrySet()) {
            var meshView = new MeshView();
            meshView.setMesh(createTriangleMesh(entry.getValue()));
            if (entry.getKey() == null) {
                meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());
            } else {
                meshView.setMaterial(entry.getKey().getMaterial());
            }
            geometryView.addMeshView(meshView);
        }
    }
}
