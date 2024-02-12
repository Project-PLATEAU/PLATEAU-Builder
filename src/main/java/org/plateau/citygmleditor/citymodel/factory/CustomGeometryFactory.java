package org.plateau.citygmleditor.citymodel.factory;

import java.util.List;

import javafx.scene.shape.TriangleMesh;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;

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

