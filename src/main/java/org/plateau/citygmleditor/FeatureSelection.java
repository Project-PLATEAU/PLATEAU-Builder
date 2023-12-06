package org.plateau.citygmleditor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.DepthTest;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citymodel.Building;
import org.plateau.citygmleditor.world.World;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

public class FeatureSelection {
    private ObjectProperty<Building> active = new SimpleObjectProperty<>();
    private MeshView outLine;

    public Building getActive() {
        return active.get();
    }

    public ObjectProperty<Building> getActiveFeatureProperty() {
        return active;
    }

    public void registerClickEvent(Scene scene) {
        outLine = new MeshView();

        var material = new PhongMaterial();
        material.setDiffuseColor(Color.ORANGE);
        outLine.setMaterial(material);
        // outLine.setCullFace(CullFace.BACK);
        outLine.setDrawMode(DrawMode.LINE);
        outLine.setDepthTest(DepthTest.DISABLE);

        var node = (Group) World.getRoot3D();
        node.getChildren().add(outLine);

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            PickResult pickResult = event.getPickResult();
            var newSelectedMesh = pickResult.getIntersectedNode();

            var feature = newSelectedMesh;
            while (feature != null && !(feature instanceof Building)) {
                feature = feature.getParent();
            }

            if (newSelectedMesh instanceof MeshView) {
                var meshView = (MeshView) newSelectedMesh;

                outLine.setMesh(meshView.getMesh());
            }

            // } else if (newSelectedMesh instanceof LOD2Solid) {
            // var lod2Solid = (LOD2Solid) newSelectedMesh;
            //
            // // 選択されたBoundarySurfaceを取得
            // // 3(頂点) * 2(positionとuv)で1面当たり6要素ある
            // var selectedFace = pickResult.getIntersectedFace() * 6;
            // BoundarySurface selectedBoundary = null;
            // for (var boundary : lod2Solid.getBoundaries()) {
            // for (var polygon : boundary.getPolygons()) {
            // selectedFace -= polygon.getFaces().length;
            // }
            // if (selectedFace < 0) {
            // selectedBoundary = boundary;
            // break;
            // }
            // }
            //
            // if (selectedBoundary != null) {
            // outLine.setMesh(GeometryFactory.createTriangleMesh(selectedBoundary.getPolygons()));
            // }
            // }

            if (feature == null)
                return;

            this.active.set((Building) feature);
        });
    }
}
