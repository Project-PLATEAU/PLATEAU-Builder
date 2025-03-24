package org.plateaubuilder.core.citymodel.factory;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.geometry.LOD1GeometryView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD1SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2GeometryView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD1GeometryFactory extends GeometryFactory {

    public LOD1GeometryFactory(CityModelView target) {
        super(target);
    }

    public LOD1GeometryView createLOD1Geometry(SolitaryVegetationObject gmlObject) {
        var geometry = gmlObject.getLod1Geometry();
        return geometry != null ? createLOD1Geometry(geometry.getObject()) : null;
    }

    public LOD1GeometryView createLOD1Geometry(CityFurniture gmlObject) {
        var geometry = gmlObject.getLod1Geometry();
        return geometry != null ? createLOD1Geometry(geometry.getObject()) : null;
    }

    public LOD1GeometryView createLOD1Geometry(AbstractGeometry geometry) {
        var polygons = new ArrayList<PolygonView>();
        if (geometry instanceof Solid) {
            polygons.addAll(createPolygonViews((Solid) geometry));
        } else if (geometry instanceof MultiSurface) {
            polygons.addAll(createPolygonViews((MultiSurface) geometry));
        }

        var mesh = createTriangleMesh(polygons);
        var meshView = new MeshView();
        meshView.setMesh(mesh);
        meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());

        var solidView = new LOD1GeometryView(geometry, vertexBuffer, texCoordBuffer);
        solidView.setPolygons(polygons);
        solidView.addMeshView(meshView);

        return solidView;
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
            var polygon = (Polygon) surfaceMemberElement.getSurface();
            if (polygon == null) {
                continue;
            }

            var polygonObject = createPolygon(polygon);
            polygons.add(polygonObject);
        }

        return polygons;
    }
}
