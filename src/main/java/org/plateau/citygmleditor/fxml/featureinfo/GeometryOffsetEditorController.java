package org.plateau.citygmleditor.fxml.featureinfo;

import java.net.URL;
import java.util.ResourceBundle;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citygmleditor.GizmoModel;
import org.plateau.citygmleditor.citygmleditor.TransformManipulator;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.world.World;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.control.TextField;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class GeometryOffsetEditorController implements Initializable {
    @FXML
    private TextField PositionX;
    @FXML
    private TextField PositionY;
    @FXML
    private TextField PositionZ;

    @FXML
    private TextField RotationX;
    @FXML
    private TextField RotationY;
    @FXML
    private TextField RotationZ;

    @FXML
    private TextField ScaleX;
    @FXML
    private TextField ScaleY;
    @FXML
    private TextField ScaleZ;

    private TransformManipulator manipulator;

    private ChangeListener<Point3D> listenerPosition;
    private ChangeListener<Point3D> listenerRotation;
    private ChangeListener<Point3D> listenerScale;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CityGMLEditorApp.getFeatureSellection().getSelectElementProperty().addListener((ov, oldSelectElement, newSelectElement) -> {
            if (oldSelectElement != null) {
                if (oldSelectElement instanceof ILODSolidView) {
                    var oldManipulator = ((ILODSolidView) oldSelectElement).getTransformManipulator();

                    if (listenerPosition != null)
                        oldManipulator.getLocationProperty().removeListener(listenerPosition);

                    if (listenerRotation != null)
                        oldManipulator.getRotationProperty().removeListener(listenerRotation);

                    if (listenerScale != null)
                        oldManipulator.getScaleProperty().removeListener(listenerScale);
                }
            }

            if (newSelectElement != null) {
                if (newSelectElement instanceof ILODSolidView) {
                    manipulator = ((ILODSolidView) newSelectElement).getTransformManipulator();

                    PositionX.textProperty().set(toString(manipulator.getLocation().getX()));
                    PositionY.textProperty().set(toString(manipulator.getLocation().getY()));
                    PositionZ.textProperty().set(toString(manipulator.getLocation().getZ()));

                    RotationX.textProperty().set(toString(manipulator.getRotation().getX()));
                    RotationY.textProperty().set(toString(manipulator.getRotation().getY()));
                    RotationZ.textProperty().set(toString(manipulator.getRotation().getZ()));

                    ScaleX.textProperty().set(toString(manipulator.getScale().getX()));
                    ScaleY.textProperty().set(toString(manipulator.getScale().getY()));
                    ScaleZ.textProperty().set(toString(manipulator.getScale().getZ()));

                    listenerPosition = (observable, oldValue, newValue) -> {
                        PositionX.textProperty().set(toString(newValue.getX()));
                        PositionY.textProperty().set(toString(newValue.getY()));
                        PositionZ.textProperty().set(toString(newValue.getZ()));
                    };

                    manipulator.getLocationProperty().addListener(listenerPosition);

                    listenerRotation = (observable, oldValue, newValue) -> {
                        RotationX.textProperty().set(toString(newValue.getX()));
                        RotationY.textProperty().set(toString(newValue.getY()));
                        RotationZ.textProperty().set(toString(newValue.getZ()));
                    };

                    manipulator.getRotationProperty().addListener(listenerRotation);

                    listenerScale = (observable, oldValue, newValue) -> {
                        ScaleX.textProperty().set(toString(newValue.getX()));
                        ScaleY.textProperty().set(toString(newValue.getY()));
                        ScaleZ.textProperty().set(toString(newValue.getZ()));
                    };

                    manipulator.getScaleProperty().addListener(listenerScale);
                }
            }
        });
        
        // 各TextFieldにイベントハンドラを設定
        PositionX.textProperty().addListener((obs, oldVal, newVal) -> {
            updateLocateFromTextFields(0, toDouble(newVal));
            updateGizmo();
        });
        PositionY.textProperty().addListener((obs, oldVal, newVal) -> {
            updateLocateFromTextFields(1, toDouble(newVal));
            updateGizmo();
        });
        PositionZ.textProperty().addListener((obs, oldVal, newVal) -> {
            updateLocateFromTextFields(2, toDouble(newVal));
            updateGizmo();
        });

        RotationX.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRotateFromTextFields(0, toDouble(newVal));
            updateGizmo();
        });
        RotationY.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRotateFromTextFields(1, toDouble(newVal));
            updateGizmo();
        });
        RotationZ.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRotateFromTextFields(2, toDouble(newVal));
            updateGizmo();
        });
        
        ScaleX.textProperty().addListener((obs, oldVal, newVal) -> {
            updateScaleFromTextFields(0, toDouble(newVal));
            updateGizmo();
        });
        ScaleY.textProperty().addListener((obs, oldVal, newVal) -> {
            updateScaleFromTextFields(1, toDouble(newVal));
            updateGizmo();
        });
        ScaleZ.textProperty().addListener((obs, oldVal, newVal) -> {
            updateScaleFromTextFields(2, toDouble(newVal));
            updateGizmo();
        });
    }

    private String toString(double value) {
        try {
            int integer = (int) value;
            if(integer == value)
                return String.valueOf(integer);
            return String.valueOf(value);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private double toDouble(String value){
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void updateLocateFromTextFields(int axis, double value) {
        if (manipulator == null) 
            return;

        var pivot = manipulator.getOrigin();
        var pivotCurrent = manipulator.getTransformCache().transform(pivot);
        var pivotDelta = pivotCurrent.subtract(pivot);

        var locationX = axis == 0 ? value : manipulator.getLocation().getX();
        var locationY = axis == 1 ? value : manipulator.getLocation().getY();
        var locationZ = axis == 2 ? value : manipulator.getLocation().getZ();

        // ローカル座標系へ適用
        Transform worldToLocalTransform = manipulator.getTransformCache();

        // 逆変換行列を取得
        try {
            worldToLocalTransform = worldToLocalTransform.createInverse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        var localPivot = worldToLocalTransform.transform(pivotDelta);
        var localValue = worldToLocalTransform.transform(new Point3D(locationX, locationY, locationZ));
        var offset = localValue.subtract(localPivot);

        manipulator.addTransformCache(new Translate(offset.getX(), offset.getY(), offset.getZ()));

        manipulator.setLocation(new Point3D(locationX, locationY, locationZ));

        // 建物の座標変換を初期化
        manipulator.getSolidView().getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        manipulator.getSolidView().getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        manipulator.getSolidView().getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));
    }
    
    private void updateRotateFromTextFields(int axis, double value) {
        if (manipulator == null) 
            return;

        var pivot = manipulator.getOrigin();

        var rotationOrgX = manipulator.getRotation().getX();
        var rotationOrgY = manipulator.getRotation().getY();
        var rotationOrgZ = manipulator.getRotation().getZ();

        var rotationX = axis == 0 ? value : manipulator.getRotation().getX();
        var rotationY = axis == 1 ? value : manipulator.getRotation().getY();
        var rotationZ = axis == 2 ? value : manipulator.getRotation().getZ();

        manipulator.setRotation(new Point3D(rotationX, rotationY, rotationZ));
        Transform rotate = new Rotate();
        rotate = rotate.createConcatenation(new Rotate(rotationX - rotationOrgX, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS));
        rotate = rotate.createConcatenation(new Rotate(rotationY - rotationOrgY, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS));
        rotate = rotate.createConcatenation(new Rotate(rotationZ - rotationOrgZ, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS));
        manipulator.addTransformCache(rotate);

        // 建物の座標変換を初期化
        manipulator.getSolidView().getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        manipulator.getSolidView().getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        manipulator.getSolidView().getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));
    }

    private void updateScaleFromTextFields(int axis, double value) {
        if (manipulator == null) 
            return;

        var pivot = manipulator.getOrigin();

        var scaleX = axis == 0 ? value : manipulator.getScale().getX();
        var scaleY = axis == 1 ? value : manipulator.getScale().getY();
        var scaleZ = axis == 2 ? value : manipulator.getScale().getZ();

        manipulator.setScale(new Point3D(scaleX, scaleY, scaleZ));

        // 建物の座標変換を初期化
        manipulator.getSolidView().getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        manipulator.getSolidView().getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        manipulator.getSolidView().getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));
    }
    
    private void updateGizmo() {
        if (manipulator == null)
            return;
            
        for (var node : World.getRoot3D().getChildrenUnmodifiable()) {
            if (node instanceof GizmoModel) {
                ((GizmoModel) node).attachManipulator(manipulator);
            }
        }
    }
}
