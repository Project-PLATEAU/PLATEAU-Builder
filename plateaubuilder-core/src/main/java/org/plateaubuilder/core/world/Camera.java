/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.plateaubuilder.core.world;

import org.locationtech.jts.math.Vector2D;
import org.plateaubuilder.core.editor.Editor;

import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * Cameraクラスは、シーンにおけるカメラの動きとインタラクションを処理します。
 */
public class Camera {
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    // 座標変換
    private final Translate pivotTranslate = new Translate();
    private final Rotate pitch = new Rotate();
    { pitch.setAxis(Rotate.Y_AXIS); }
    private final Rotate yaw = new Rotate();
    { yaw.setAxis(Rotate.Z_AXIS); }
    private final Translate zoom = new Translate();

    // ズーム
    private double zoomFactor = 0f;
    private static final double baseZoomDistance = 400f;

    private Vector2D lastMousePosition;
    private PauseTransition pause;
    public Camera() {
        // CAMERA
        camera.setNearClip(1.0); // TODO: Workaround as per RT-31255
        camera.setFarClip(10000.0); // TODO: Workaround as per RT-31255

        camera.getTransforms().addAll(
                pivotTranslate,
                yaw,
                pitch,
                zoom,
                // JavaFXではZ軸正面、-Y軸上向きがデフォルトの視線回転なので回転に補正
                new Rotate(-90, Rotate.Z_AXIS),
                new Rotate(-90, Rotate.X_AXIS)
        );

        zoom.setX(-baseZoomDistance);
        pitch.setAngle(45);
        yaw.setAngle(90);
    }

    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
        if (event.isPrimaryButtonDown() && !event.isSecondaryButtonDown() && !event.isMiddleButtonDown()) {
            return;
        }
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            lastMousePosition = new Vector2D(event.getSceneX(), event.getSceneY());
        }

        if (event.getEventType() != MouseEvent.MOUSE_DRAGGED)
            return;

        double modifier = 1.0;

        if (event.isControlDown()) {
            modifier = 0.1;
        }
        if (event.isShiftDown()) {
            modifier = 10.0;
        }

        var currentMousePosition = new Vector2D(event.getSceneX(), event.getSceneY());
        var deltaMousePosition = currentMousePosition.subtract(lastMousePosition);
        lastMousePosition = currentMousePosition;

        if (event.isMiddleButtonDown()) {
            // 移動
            var deltaPosition = yaw.deltaTransform(
                    applyZoomFactor(deltaMousePosition.getY() * modifier * 0.5),
                    applyZoomFactor(deltaMousePosition.getX() * modifier * 0.5),
                    0
            );
            pivotTranslate.setX(pivotTranslate.getX() + deltaPosition.getX());
            pivotTranslate.setY(pivotTranslate.getY() + deltaPosition.getY());
            pivotTranslate.setZ(pivotTranslate.getZ() + deltaPosition.getZ());
            if (pause == null) {
                pause = new PauseTransition(Duration.millis(200));
                pause.setOnFinished(e -> {
                    Editor.getXyzTile().loadImagesAfterCameraMove();
                });
            }
            pause.playFromStart();
        } else if (event.isSecondaryButtonDown()) {
            // 回転
            yaw.setAngle(yaw.getAngle() - deltaMousePosition.getX() * modifier * 0.6);
            pitch.setAngle(pitch.getAngle() + deltaMousePosition.getY() * modifier * 0.6);
        }
    };

    private final EventHandler<ScrollEvent> scrollEventHandler = event -> {
        // 中心からのマウス位置を取得
        var scene = Editor.getSceneContent().getSubScene();
        var center = new Vector2D(scene.getWidth() / 2, scene.getHeight() / 2);
        var currentMousePosition = new Vector2D(event.getX(), event.getY());
        var deltaMousePosition = center.subtract(currentMousePosition);

        // マウス位置にある座標がズーム後にも同じ位置になるように移動
        var currentPosition = yaw.deltaTransform(applyZoomFactor(deltaMousePosition.getY() * 0.5), applyZoomFactor(deltaMousePosition.getX() * 0.5), 0);
        var nextZoomFactor = zoomFactor + event.getDeltaY() * 0.01;
        var nextZoomPosition = yaw.deltaTransform(applyZoomFactor(deltaMousePosition.getY() * 0.5, nextZoomFactor),
                applyZoomFactor(deltaMousePosition.getX() * 0.5, nextZoomFactor), 0);
        var deltaPosition = currentPosition.subtract(nextZoomPosition);
        pivotTranslate.setX(pivotTranslate.getX() + deltaPosition.getX());
        pivotTranslate.setY(pivotTranslate.getY() + deltaPosition.getY());
        pivotTranslate.setZ(pivotTranslate.getZ() + deltaPosition.getZ());

        if (pause == null) {
            pause = new PauseTransition(Duration.millis(200));
            pause.setOnFinished(e -> {
                Editor.getXyzTile().loadImagesAfterCameraMove();
            });
        }
        pause.playFromStart();
        // ズームイン・アウト
        zoomFactor = nextZoomFactor;
        zoom.setX(calculateZoomOffset());
    };

    private double applyZoomFactor(double value) {
        return applyZoomFactor(value, zoomFactor);
    }

    private double applyZoomFactor(double value, double zoomFactor) {
        return value * Math.pow(2, -zoomFactor);
    }

    private double calculateZoomOffset() {
        return applyZoomFactor(-baseZoomDistance);
    }

    private double calculateZoomFactorByOffsetDistance(double distance) {
        return -Math.log(distance / baseZoomDistance) / Math.log(2);
    }

    public EventHandler<MouseEvent> getMouseEventHandler() {
        return mouseEventHandler;
    }

    public EventHandler<ScrollEvent> getScrollEventHandler() {
        return scrollEventHandler;
    }

    public Node getRoot() {
        return camera;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public void focus(MeshView meshView) {
        var bounds = meshView.getBoundsInParent();
        var pivot = new Point3D(bounds.getCenterX(), bounds.getCenterY(), bounds.getMinZ());
        var extent = new Point3D(
                bounds.getMaxX() - bounds.getMinX(),
                bounds.getMaxY() - bounds.getMinY(),
                bounds.getMaxZ() - bounds.getMinZ()
        );
        pivotTranslate.setX(pivot.getX());
        pivotTranslate.setY(pivot.getY());
        pivotTranslate.setZ(pivot.getZ());
        var desiredZoomOffsetDistance = extent.magnitude() * 4;
        zoomFactor = calculateZoomFactorByOffsetDistance(desiredZoomOffsetDistance);
        zoom.setX(calculateZoomOffset());
        pitch.setAngle(45);
    }

    /**
     * ズームアイコンドラッグ
     * @param deltaMousePosition マウス移動量
     */
    public void dragZoom(Vector2D deltaMousePosition) {
        zoomFactor -= deltaMousePosition.getY() * 0.01;
        zoom.setX(calculateZoomOffset());
    }

    /**
     * パンアイコンドラッグ
     * @param deltaMousePosition マウス移動量
     */
    public void dragPan(Vector2D deltaMousePosition) {
        // 移動
        var deltaPosition = yaw.deltaTransform(
                applyZoomFactor(deltaMousePosition.getY() * 0.5),
                applyZoomFactor(deltaMousePosition.getX() * 0.5),
                0
        );
        pivotTranslate.setX(pivotTranslate.getX() + deltaPosition.getX());
        pivotTranslate.setY(pivotTranslate.getY() + deltaPosition.getY());
        pivotTranslate.setZ(pivotTranslate.getZ() + deltaPosition.getZ());
    }

    /**
     * 回転アイコンドラッグ
     * @param deltaMousePosition マウス移動量
     */
    public void dragRotate(Vector2D deltaMousePosition) {
        // 回転
        yaw.setAngle(yaw.getAngle() - deltaMousePosition.getX() * 0.6);
        pitch.setAngle(pitch.getAngle() + deltaMousePosition.getY() * 0.6);
    }

    /*
     * ビューを北にフォーカスします
     */
    public void focusNorth() {
        resetPivot();

        var citymodelGroup = World.getActiveInstance().getCityModelGroup();
        double distance = 1000;
        if (citymodelGroup != null) {
            var bounds = citymodelGroup.getBoundsInParent();
            var distance1 = (Math.abs(bounds.getMaxX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxY();
            var distance2 = (Math.abs(bounds.getMinX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxY();
            distance = Math.max(distance1, distance2);
        }
        zoomFactor = calculateZoomFactorByOffsetDistance(distance);
        zoom.setX(calculateZoomOffset());

        yaw.setAngle(90);
        pitch.setAngle(0);
    }

    /*
     * ビューを南にフォーカスします
     */
    public void focusSouth() {
        resetPivot();

        var citymodelGroup = World.getActiveInstance().getCityModelGroup();
        double distance = 1000;
        if (citymodelGroup != null) {
            var bounds = citymodelGroup.getBoundsInParent();
            var distance1 = (Math.abs(bounds.getMaxX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) - bounds.getMinY();
            var distance2 = (Math.abs(bounds.getMinX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) - bounds.getMinY();
            distance = Math.max(distance1, distance2);
        }
        zoomFactor = calculateZoomFactorByOffsetDistance(distance);
        zoom.setX(calculateZoomOffset());

        yaw.setAngle(-90);
        pitch.setAngle(0);
    }

    /*
     * ビューを西にフォーカスします
     */
    public void focusWest() {
        resetPivot();

        var citymodelGroup = World.getActiveInstance().getCityModelGroup();
        double distance = 1000;
        if (citymodelGroup != null) {
            var bounds = citymodelGroup.getBoundsInParent();
            var distance1 = (Math.abs(bounds.getMaxY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxX();
            var distance2 = (Math.abs(bounds.getMinY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxX();
            distance = Math.max(distance1, distance2);
        }
        zoomFactor = calculateZoomFactorByOffsetDistance(distance);
        zoom.setX(calculateZoomOffset());

        yaw.setAngle(180);
        pitch.setAngle(0);
    }

    /*
     * ビューを東にフォーカスします
     */
    public void focusEast() {

        resetPivot();

        var citymodelGroup = World.getActiveInstance().getCityModelGroup();
        double distance = 1000;
        if (citymodelGroup != null) {
            var bounds = citymodelGroup.getBoundsInParent();
            var distance1 = (Math.abs(bounds.getMaxY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) - bounds.getMinX();
            var distance2 = (Math.abs(bounds.getMinY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) - bounds.getMinX();
            distance = Math.max(distance1, distance2);
        }
        zoomFactor = calculateZoomFactorByOffsetDistance(distance);
        zoom.setX(calculateZoomOffset());

        yaw.setAngle(0);
        pitch.setAngle(0);
    }

    /*
     * ビューを北 + 上にフォーカスします
     */
    public void focusTop() {
        resetPivot();

        var citymodelGroup = World.getActiveInstance().getCityModelGroup();
        double distance = 1000;
        if (citymodelGroup != null) {
            var bounds = citymodelGroup.getBoundsInParent();
            var distance1 = (Math.abs(bounds.getMaxX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var distance2 = (Math.abs(bounds.getMinX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var distance3 = (Math.abs(bounds.getMaxX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var distance4 = (Math.abs(bounds.getMinX()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var distance5 = (Math.abs(bounds.getMaxY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var distance6 = (Math.abs(bounds.getMinY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var distance7 = (Math.abs(bounds.getMaxY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var distance8 = (Math.abs(bounds.getMinY()) / (Math.tan(Math.toRadians(camera.getFieldOfView() / 2)))) + bounds.getMaxZ();
            var maxDistanceX = Math.max(Math.max(distance1, distance2), Math.max(distance3, distance4));
            var maxDistanceY = Math.max(Math.max(distance5, distance6), Math.max(distance7, distance8));
            distance = Math.max(maxDistanceX, maxDistanceY);
        }
        zoomFactor = calculateZoomFactorByOffsetDistance(distance);
        zoom.setX(calculateZoomOffset());

        yaw.setAngle(90);
        pitch.setAngle(90);
    }

    public Rotate getYaw() {
        return yaw;
    }

    private void resetPivot() {
        pivotTranslate.setX(0);
        pivotTranslate.setY(0);
        pivotTranslate.setZ(0);
    }
}
