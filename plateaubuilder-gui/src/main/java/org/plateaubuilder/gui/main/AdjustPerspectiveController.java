package org.plateaubuilder.gui.main;

import java.net.URL;
import java.util.ResourceBundle;

import org.locationtech.jts.math.Vector2D;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.world.Camera;
import org.plateaubuilder.core.world.World;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class AdjustPerspectiveController implements Initializable {
    public Canvas arrowCanvas;
    public ImageView iconZoom;
    public ImageView iconPan;
    public ImageView iconRotate;

    private Camera camera;

    public enum SelectIcons {
        NONE, ZOOM, PAN, ROTATE,
    }

    private SelectIcons selectIcon = SelectIcons.NONE;
    private SelectIcons reserveIcon = SelectIcons.NONE;

    private Vector2D lastMousePosition;

    private boolean isDragging = false;

    private SimpleDoubleProperty rotateProperty = new SimpleDoubleProperty();
    private Image arrowIcon = new Image(getClass().getResource("/org/plateaubuilder/gui/images/icon_arrow.png").toExternalForm());
    private IconControl IconControlZoom;
    private IconControl IconControlPan;
    private IconControl IconControlRotate;

    private class IconControl {
        private final ImageView imageView;
        private final Image originImage;
        private final Image hoverImage;
        private final SelectIcons changeIcon;

        public IconControl(ImageView imageView, Image originImage, Image hoverImage, SelectIcons changeIcon) {
            this.imageView = imageView;
            this.originImage = originImage;
            this.hoverImage = hoverImage;
            this.changeIcon = changeIcon;

            registEventHandlers();
        }

        private void registEventHandlers() {
            // マウスオーバー時のイベントハンドラを設定
            imageView.setOnMouseEntered(event -> {
                reserveIcon = changeIcon;
                selectIcon = changeIcon;
                imageView.setImage(hoverImage);
            });
            // マウスが離れた時のイベントハンドラを設定
            imageView.setOnMouseExited(event -> {
                reserveIcon = SelectIcons.NONE;
                if (!isDragging) {
                    selectIcon = SelectIcons.NONE;
                    imageView.setImage(originImage);
                }
            });
        }

        private void resetIcon() {
            imageView.setImage(originImage);
        }
    }

    /**
     * FXMLファイルがロードされた際に呼び出される初期化メソッドです。
     * 
     * @param location FXMLファイルのURL
     * @param resources ロケール固有のリソースバンドル
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iconZoom.addEventHandler(MouseEvent.ANY, mouseEventHandler);
        iconPan.addEventHandler(MouseEvent.ANY, mouseEventHandler);
        iconRotate.addEventHandler(MouseEvent.ANY, mouseEventHandler);

        camera = World.getActiveInstance().getCamera();
        var angleProperty = camera.getYaw().angleProperty();
        rotateProperty.bind(angleProperty);
        DrawArraw(angleProperty.doubleValue());
        rotateProperty.addListener(observable -> {
            var doubleProperty = (DoubleProperty) observable;
            DrawArraw(doubleProperty.doubleValue());
        });

        var resourceDirectory = "/org/plateaubuilder/gui/";
        var zoomIconPath = getClass().getResource(resourceDirectory + "images/icon_view_zoom_hover.png").toExternalForm();
        var panIconPath = getClass().getResource(resourceDirectory + "images/icon_view_pan_hover.png").toExternalForm();
        var rotateIconPath = getClass().getResource(resourceDirectory + "images/icon_view_rotate_hover.png").toExternalForm();

        IconControlZoom = new IconControl(iconZoom, iconZoom.getImage(), new Image(zoomIconPath), SelectIcons.ZOOM);
        IconControlPan = new IconControl(iconPan, iconPan.getImage(), new Image(panIconPath), SelectIcons.PAN);
        IconControlRotate = new IconControl(iconRotate, iconRotate.getImage(), new Image(rotateIconPath), SelectIcons.ROTATE);
    }

    private void DrawArraw(double angle) {
        GraphicsContext gc = arrowCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, arrowCanvas.getWidth(), arrowCanvas.getHeight());
        gc.save();
        var transform = gc.getTransform();
        transform.appendRotation(90 - angle, arrowCanvas.getWidth() / 2, arrowCanvas.getHeight() / 2);
        gc.setTransform(transform);
        gc.drawImage(arrowIcon, 0, 0, arrowCanvas.getWidth(), arrowCanvas.getHeight());
        gc.restore();
    }

    /**
     * マウスイベントを処理するハンドラ
     */
    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
        var scene = Editor.getScene();
        isDragging = false;
        if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            if (reserveIcon == SelectIcons.NONE){
                IconControlZoom.resetIcon();
                IconControlPan.resetIcon();
                IconControlRotate.resetIcon();
                Editor.getXyzTile().loadImagesAfterCameraMove();
            }
            scene.setCursor(Cursor.DEFAULT);
        }

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.isPrimaryButtonDown()) {
            lastMousePosition = new Vector2D(event.getSceneX(), event.getSceneY());
            scene.setCursor(Cursor.HAND);
        }

        if (event.getEventType() != MouseEvent.MOUSE_DRAGGED && event.getEventType() != MouseEvent.DRAG_DETECTED && event.getEventType() != MouseEvent.MOUSE_PRESSED)
            return;

        isDragging = true;

        var currentMousePosition = new Vector2D(event.getSceneX(), event.getSceneY());
        var deltaMousePosition = currentMousePosition.subtract(lastMousePosition);
        lastMousePosition = currentMousePosition;

        if (event.isPrimaryButtonDown()) {
            scene.setCursor(Cursor.CLOSED_HAND);
            switch(selectIcon){
                case ZOOM:
                    camera.dragZoom(deltaMousePosition);
                    break;
                case PAN:
                    camera.dragPan(deltaMousePosition);
                    break;
                case ROTATE:
                    camera.dragRotate(deltaMousePosition);
                    break;
            }
        }
    };
}
