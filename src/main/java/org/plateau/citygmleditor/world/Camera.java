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
package org.plateau.citygmleditor.world;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.*;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.event.EventHandler;
import org.locationtech.jts.math.Vector2D;

/**
 * Cameraクラスは、シーンにおけるカメラの動きとインタラクションを処理します。
 */
public class Camera {
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Translate cameraPosition = new Translate(0, 0, 0);

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
    }

    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
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
        } else if (event.isSecondaryButtonDown()) {
            // 回転
            yaw.setAngle(yaw.getAngle() + deltaMousePosition.getX() * modifier * 0.6);
            pitch.setAngle(pitch.getAngle() + deltaMousePosition.getY() * modifier * 0.6);
        }
    };

    private final EventHandler<ScrollEvent> scrollEventHandler = event -> {
        // ズームイン・アウト
        zoomFactor += event.getDeltaY() * 0.01;
        zoom.setX(calculateZoomOffset());
    };

    private double applyZoomFactor(double value) {
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
}
