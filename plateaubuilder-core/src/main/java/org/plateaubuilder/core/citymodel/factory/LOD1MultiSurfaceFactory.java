package org.plateaubuilder.core.citymodel.factory;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD1MultiSurfaceFactory extends GeometryFactory {

    public LOD1MultiSurfaceFactory(CityModelView target) {
        super(target);
    }

    public LOD1MultiSurfaceView createLOD1MultiSurface(Road gmlObject) {
        var multiSurface = gmlObject.getLod1MultiSurface();
        return multiSurface != null ? createLOD1MultiSurface(multiSurface.getObject()) : null;
    }

    public LOD1MultiSurfaceView createLOD1MultiSurface(LandUse gmlObject) {
        var multiSurface = gmlObject.getLod1MultiSurface();
        return multiSurface != null ? createLOD1MultiSurface(multiSurface.getObject()) : null;
    }

    public LOD1MultiSurfaceView createLOD1MultiSurface(WaterBody gmlObject) {
        var multiSurface = gmlObject.getLod1MultiSurface();
        return multiSurface != null ? createLOD1MultiSurface(multiSurface.getObject()) : null;
    }

    public LOD1MultiSurfaceView createLOD1MultiSurface(PlantCover gmlObject) {
        var multiSurface = gmlObject.getLod1MultiSurface();
        return multiSurface != null ? createLOD1MultiSurface(multiSurface.getObject()) : null;
    }

    public LOD1MultiSurfaceView createLOD1MultiSurface(ADEGenericComponent gmlObject) {
        var multiSurface = gmlObject.getLod1MultiSurface();
        return multiSurface != null ? createLOD1MultiSurface(multiSurface.getObject()) : null;
    }

    private LOD1MultiSurfaceView createLOD1MultiSurface(MultiSurface multiSurface) {
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

        var mesh = createTriangleMesh(polygons);
        var meshView = new MeshView();
        meshView.setMesh(mesh);
        meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());

        var multiSurfaceView = new LOD1MultiSurfaceView(multiSurface, vertexBuffer, texCoordBuffer);
        multiSurfaceView.setPolygons(polygons);
        multiSurfaceView.addMeshView(meshView);

        return multiSurfaceView;
    }
}
