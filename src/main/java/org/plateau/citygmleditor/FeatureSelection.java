package org.plateau.citygmleditor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.citygml4j.model.citygml.CityGMLClass;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citymodel.factory.GeometryFactory;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.control.BuildingSurfaceTypeEditor;
import org.plateau.citygmleditor.utils3d.geom.Vec2f;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.PolygonMeshUtils;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;
import org.plateau.citygmleditor.world.World;

import org.plateau.citygmleditor.citymodel.BuildingView;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

import java.nio.ByteBuffer;
import java.util.*;

public class FeatureSelection {
    private final ObjectProperty<BuildingView> active = new SimpleObjectProperty<>();
    private final MeshView outLine = new MeshView();

    private final BuildingSurfaceTypeEditor surfaceTypeEditor = new BuildingSurfaceTypeEditor();

    public FeatureSelection() {
        var material = new PhongMaterial();
        material.setDiffuseColor(new Color(0, 0, 0, 1));
        WritableImage image = new WritableImage(1, 1);
        PixelWriter writer = image.getPixelWriter();
        writer.setColor(0, 0, Color.ORANGE);

        material.setSelfIlluminationMap(image);
        outLine.setMaterial(material);
        outLine.setDrawMode(DrawMode.LINE);
        outLine.setDepthTest(DepthTest.DISABLE);

        var node = (Group) World.getRoot3D();
        node.getChildren().add(outLine);
    }

    public BuildingView getActive() {
        return active.get();
    }

    public ObjectProperty<BuildingView> getActiveFeatureProperty() {
        return active;
    }

    public void registerClickEvent(Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            PickResult pickResult = event.getPickResult();
            var newSelectedMesh = pickResult.getIntersectedNode();
            var feature = getBuilding(newSelectedMesh);

            if (feature == null)
                return;

            if (feature.getLOD2Solid() != null && feature.getLOD2Solid().isVisible())
                surfaceTypeEditor.setTarget(feature.getLOD2Solid());
            else {
                outLine.setMesh(feature.getLOD1Solid().getMesh());
            }

            this.active.set(feature);
        });
    }

    public MeshView getOutLine() {
        return outLine;
    }

    private BuildingView getBuilding(Node node) {
        while (node != null && !(node instanceof BuildingView)) {
            node = node.getParent();
        }
        return (BuildingView)node;
    }
}
