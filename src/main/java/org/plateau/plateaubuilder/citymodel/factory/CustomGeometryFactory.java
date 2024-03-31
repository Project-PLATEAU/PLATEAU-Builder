package org.plateau.plateaubuilder.citymodel.factory;

import javafx.scene.shape.TriangleMesh;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.citymodel.geometry.PolygonView;

import java.util.List;

public class CustomGeometryFactory extends GeometryFactory {

  public CustomGeometryFactory(CityModelView target) {
    super(target);
  }

  @Override
  public PolygonView createPolygon(Polygon polygon) {
    return super.createPolygon(polygon);
  }
  @Override
  public TriangleMesh createTriangleMesh(List<PolygonView> polygons) {
    return super.createTriangleMesh(polygons);
  }
}

