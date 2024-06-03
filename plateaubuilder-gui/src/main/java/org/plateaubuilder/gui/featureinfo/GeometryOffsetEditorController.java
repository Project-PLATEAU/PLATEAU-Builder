package org.plateaubuilder.gui.featureinfo;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.control.TextField;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.plateaubuilder.core.citymodel.geometry.ILODSolidView;
import org.plateaubuilder.core.editor.commands.UndoableCommand;
import org.plateaubuilder.core.editor.transform.TransformManipulator;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.world.World;

import java.net.URL;
import java.util.ResourceBundle;

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

    // リスナーハンドル
    private ChangeListener<Point3D> listenerPosition;
    private ChangeListener<Point3D> listenerRotation;
    private ChangeListener<Point3D> listenerScale;

    private enum Transforms {
        LOCATE,
        ROTATE,
        SCALE
    }

    private enum Axis {
        X,
        Y,
        Z
    }

    private final String[][] oldValues = new String[Transforms.values().length][Axis.values().length];

    /**
     * FXMLファイルがロードされた際に呼び出される初期化メソッドです。
     * 
     * @param location FXMLファイルのURL
     * @param resources ロケール固有のリソースバンドル
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Editor.getFeatureSellection().getSelectElementProperty().addListener((ov, oldSelectElement, newSelectElement) -> {
            // 以前のギズモの操作対象に設定されたリスナーハンドルを削除
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

                    // 各TextFieldに現在の値を設定
                    PositionX.textProperty().set(toString(manipulator.getLocation().getX()));
                    PositionY.textProperty().set(toString(manipulator.getLocation().getY()));
                    PositionZ.textProperty().set(toString(manipulator.getLocation().getZ()));

                    RotationX.textProperty().set(toString(manipulator.getRotation().getX()));
                    RotationY.textProperty().set(toString(manipulator.getRotation().getY()));
                    RotationZ.textProperty().set(toString(manipulator.getRotation().getZ()));

                    ScaleX.textProperty().set(toString(manipulator.getScale().getX()));
                    ScaleY.textProperty().set(toString(manipulator.getScale().getY()));
                    ScaleZ.textProperty().set(toString(manipulator.getScale().getZ()));

                    // ギズモ操作による値を各TextFieldに反映するためのリスナーを設定
                    listenerPosition = (observable, oldValue, newValue) -> {
                        PositionX.textProperty().set(toString(newValue.getX()));
                        PositionY.textProperty().set(toString(newValue.getY()));
                        PositionZ.textProperty().set(toString(newValue.getZ()));
                        Editor.getFeatureSellection().refreshOutLine();
                    };

                    manipulator.getLocationProperty().addListener(listenerPosition);

                    listenerRotation = (observable, oldValue, newValue) -> {
                        RotationX.textProperty().set(toString(newValue.getX()));
                        RotationY.textProperty().set(toString(newValue.getY()));
                        RotationZ.textProperty().set(toString(newValue.getZ()));
                        Editor.getFeatureSellection().refreshOutLine();
                    };

                    manipulator.getRotationProperty().addListener(listenerRotation);

                    listenerScale = (observable, oldValue, newValue) -> {
                        ScaleX.textProperty().set(toString(newValue.getX()));
                        ScaleY.textProperty().set(toString(newValue.getY()));
                        ScaleZ.textProperty().set(toString(newValue.getZ()));
                        Editor.getFeatureSellection().refreshOutLine();
                    };

                    manipulator.getScaleProperty().addListener(listenerScale);

                    oldValues[Transforms.LOCATE.ordinal()][Axis.X.ordinal()] = PositionX.getText();
                    oldValues[Transforms.LOCATE.ordinal()][Axis.Y.ordinal()] = PositionY.getText();
                    oldValues[Transforms.LOCATE.ordinal()][Axis.Z.ordinal()] = PositionZ.getText();
                    oldValues[Transforms.ROTATE.ordinal()][Axis.X.ordinal()] = RotationX.getText();
                    oldValues[Transforms.ROTATE.ordinal()][Axis.Y.ordinal()] = RotationY.getText();
                    oldValues[Transforms.ROTATE.ordinal()][Axis.Z.ordinal()] = RotationZ.getText();
                    oldValues[Transforms.SCALE.ordinal()][Axis.X.ordinal()] = ScaleX.getText();
                    oldValues[Transforms.SCALE.ordinal()][Axis.Y.ordinal()] = ScaleY.getText();
                    oldValues[Transforms.SCALE.ordinal()][Axis.Z.ordinal()] = ScaleZ.getText();
                }
            }
        });

        // Enterキーが押されたときのアクションを設定
        PositionX.setOnAction(event -> {
            submitValue(Transforms.LOCATE, Axis.X, PositionX.getText());
        });
        PositionY.setOnAction(event -> {
            submitValue(Transforms.LOCATE, Axis.Y, PositionY.getText());
        });
        PositionZ.setOnAction(event -> {
            submitValue(Transforms.LOCATE, Axis.Z, PositionZ.getText());
        });
        // フォーカスが外れたときのアクションを設定
        PositionX.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.LOCATE, Axis.X, PositionX.getText());
            }
        });
        PositionY.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.LOCATE, Axis.Y, PositionY.getText());
            }
        });
        PositionZ.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.LOCATE, Axis.Z, PositionZ.getText());
            }
        });

        // Enterキーが押されたときのアクションを設定
        RotationX.setOnAction(event -> {
            submitValue(Transforms.ROTATE, Axis.X, RotationX.getText());
        });
        RotationY.setOnAction(event -> {
            submitValue(Transforms.ROTATE, Axis.Y, RotationY.getText());
        });
        RotationZ.setOnAction(event -> {
            submitValue(Transforms.ROTATE, Axis.Z, RotationZ.getText());
        });
        // フォーカスが外れたときのアクションを設定
        RotationX.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.ROTATE, Axis.X, RotationX.getText());
            }
        });
        RotationY.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.ROTATE, Axis.Y, RotationY.getText());
            }
        });
        RotationZ.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.ROTATE, Axis.Z, RotationZ.getText());
            }
        });

        // Enterキーが押されたときのアクションを設定
        ScaleX.setOnAction(event -> {
            submitValue(Transforms.SCALE, Axis.X, ScaleX.getText());
        });
        ScaleY.setOnAction(event -> {
            submitValue(Transforms.SCALE, Axis.Y, ScaleY.getText());
        });
        ScaleZ.setOnAction(event -> {
            submitValue(Transforms.SCALE, Axis.Z, ScaleZ.getText());
        });
        // フォーカスが外れたときのアクションを設定
        ScaleX.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.SCALE, Axis.X, ScaleX.getText());
            }
        });
        ScaleY.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.SCALE, Axis.Y, ScaleY.getText());
            }
        });
        ScaleZ.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                submitValue(Transforms.SCALE, Axis.Z, ScaleZ.getText());
            }
        });
    }

    private void submitValue(Transforms transforms, Axis axis, String newVal) {
        if(newVal.equals(oldValues[transforms.ordinal()][axis.ordinal()])) return;
        // Undo登録
        addUndoManager(transforms, axis, oldValues[transforms.ordinal()][axis.ordinal()], newVal);
        // パラメータ反映
        switch (transforms) {
            case LOCATE:
                updateLocateFromTextFields(axis.ordinal(), toDouble(newVal));
                break;
            case ROTATE:
                updateRotateFromTextFields(axis.ordinal(), toDouble(newVal));
                break;
            case SCALE:
                updateScaleFromTextFields(axis.ordinal(), toDouble(newVal));
                break;
        }
        // ギズモ更新
        updateGizmo();

        oldValues[transforms.ordinal()][axis.ordinal()] = newVal;
    }

    /**
     * 指定された double 値を文字列に変換します。
     * 
     * @param value 変換する double 値
     * @return 変換された文字列
     */
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

    /**
     * 指定された文字列を double 値に変換します。
     * 
     * @param value 変換する文字列
     * @return 変換された double 値。変換できない場合は 0.0 を返します。
     */
    private double toDouble(String value){
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * テキストフィールドからの入力を使用して、特定の軸方向の位置を更新します。
     * 
     * @param axis 軸の番号 (0: X軸, 1: Y軸, 2: Z軸)
     * @param value 新しい位置の値
     */
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
    
    /**
     * テキストフィールドからの入力を使用して、特定の軸周りの回転を更新します。
     * 
     * @param axis 回転軸の番号 (0: X軸, 1: Y軸, 2: Z軸)
     * @param value 新しい回転角度の値
     */
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

    /**
     * テキストフィールドからの入力を使用して、特定の軸に沿ったスケールを更新します。
     * 
     * @param axis スケールの軸の番号 (0: X軸, 1: Y軸, 2: Z軸)
     * @param value 新しいスケール値
     */
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
    
    /**
     * ギズモを更新します。
     * このメソッドは、ギズモに操作対象を再設定することでギズモの表示や位置、姿勢などを更新します。
     */
    private void updateGizmo() {
        if (manipulator == null)
            return;

        World.getActiveInstance().getGizmo().attachManipulator(manipulator);
    }

    private void addUndoManager(Transforms transforms, Axis axis, String oldValue, String newValue) {
        Transforms undoTransforms = transforms;
        Axis undoAxis = axis;
        String undoOldValue = oldValue;
        String undoNewValue = newValue;

        Editor.getUndoManager().addCommand(new UndoableCommand() {
            private final org.plateaubuilder.core.citymodel.IFeatureView focusTarget = Editor.getFeatureSellection().getActive();
            private final TextField textField = undoTransforms == Transforms.LOCATE ? (undoAxis == Axis.X ? PositionX : undoAxis == Axis.Y ? PositionY : PositionZ) : undoTransforms == Transforms.ROTATE ? (undoAxis == Axis.X ? RotationX : undoAxis == Axis.Y ? RotationY : RotationZ) : (undoAxis == Axis.X ? ScaleX : undoAxis == Axis.Y ? ScaleY : ScaleZ);
            private final Transforms transforms = undoTransforms;
            private final Axis axis = undoAxis;
            private final String oldValue = undoOldValue;
            private final String newValue = undoNewValue;
            private final TransformManipulator transformManipulator = manipulator;

            @Override
            public void redo() {
                applyTransform(newValue);
            }

            @Override
            public void undo() {
                applyTransform(oldValue);
            }

            @Override
            public javafx.scene.Node getRedoFocusTarget() {
                return focusTarget.getNode();
            }

            @Override
            public javafx.scene.Node getUndoFocusTarget() {
                return focusTarget.getNode();
            }

            private void applyTransform(String value) {
                manipulator = transformManipulator;
                textField.textProperty().set(value);
                switch(transforms) {
                    case LOCATE:
                        updateLocateFromTextFields(axis.ordinal(), toDouble(value));
                        break;
                    case ROTATE:
                        updateRotateFromTextFields(axis.ordinal(), toDouble(value));
                        break;
                    case SCALE:
                        updateScaleFromTextFields(axis.ordinal(), toDouble(value));
                        break;
                }
                updateGizmo();
            }
        });
    }
}
