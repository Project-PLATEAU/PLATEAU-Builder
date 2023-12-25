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
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.util.Duration;

import org.plateau.citygmleditor.citygmleditor.*;

/**
 * Class responsible for camera operation.
 * Rotate, move, zoom in/out, etc.
 */
public class Camera {
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Rotate cameraXRotate = new Rotate(-20, 0, 0, 0, Rotate.X_AXIS);
    private final Rotate cameraYRotate = new Rotate(-20, 0, 0, 0, Rotate.Y_AXIS);
    private final Rotate cameraLookXRotate = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
    private final Rotate cameraLookZRotate = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
    private final Translate cameraPosition = new Translate(0, 0, 0);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();
    private final double cameraDistance = 200;

    private double dragStartX, dragStartY, dragStartRotateX, dragStartRotateY;
    private Rotate yUpRotate = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private boolean isHookingMousePrimaryButtonEvent;

    private Light light;
    private SceneContent sceneContent;

    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
        double yFlip = 1.0;
        if (getYUp()) {
            yFlip = 1.0;
        } else {
            yFlip = -1.0;
        }
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
            dragStartRotateX = cameraXRotate.getAngle();
            dragStartRotateY = cameraYRotate.getAngle();
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            double xDelta = event.getSceneX() - dragStartX;
            double yDelta = event.getSceneY() - dragStartY;
            double modifier = 1.0;
            double modifierFactor = 0.3;

            if (event.isControlDown()) {
                modifier = 0.1;
            }
            if (event.isShiftDown()) {
                modifier = 10.0;
            }

            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX); // *DELTA_MULTIPLIER;
            mouseDeltaY = (mousePosY - mouseOldY); // *DELTA_MULTIPLIER;

            double flip = -1.0;
            boolean alt = (true || event.isAltDown()); // For now, don't require ALT to be pressed
            if (alt && (event.isMiddleButtonDown() || (event.isPrimaryButtonDown() && event.isSecondaryButtonDown()))) {
                cameraXform2.t.setX(cameraXform2.t.getX() + flip * mouseDeltaX * modifierFactor * modifier * 0.3); // -
                cameraXform2.t.setY(cameraXform2.t.getY() + yFlip * mouseDeltaY * modifierFactor * modifier * 0.3); // -
            } else if (alt && event.isPrimaryButtonDown()) {
                if (!isHookingMousePrimaryButtonEvent) {
                    // When rotate mode is selected
                    if (getRotateMode() == true) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + flip * mouseDeltaY * modifierFactor * modifier * 2.0); // -
                        cameraXform.rz.setAngle(cameraXform.rz.getAngle() + yFlip * mouseDeltaX * modifierFactor * modifier * 2.0); // -
                        // When move mode is selected
                    } else if (getMoveMode() == true) {
                        cameraXform2.t.setX(cameraXform2.t.getX() + flip * mouseDeltaX * modifierFactor * modifier * 0.3); // -
                        cameraXform2.t.setY(cameraXform2.t.getY() + yFlip * mouseDeltaY * modifierFactor * modifier * 0.3); // -
                    }
                }
            } else if (alt && event.isSecondaryButtonDown()) {
                double z = cameraPosition.getZ();
                double newZ = z - flip * (mouseDeltaX + mouseDeltaY) * modifierFactor * modifier;
                cameraPosition.setZ(newZ);
            }
        }
    };

    private final EventHandler<ScrollEvent> scrollEventHandler = event -> {
        if (event.getTouchCount() > 0) { // touch pad scroll
            cameraXform2.t.setX(cameraXform2.t.getX() - (0.01 * event.getDeltaX())); // -
            cameraXform2.t.setY(cameraXform2.t.getY() + (0.01 * event.getDeltaY())); // -
        } else {
            double z = cameraPosition.getZ() - (event.getDeltaY() * 0.2);
            z = Math.max(z, -1000);
            z = Math.min(z, 0);
            cameraPosition.setZ(z);
        }
    };

    private final EventHandler<ZoomEvent> zoomEventHandler = event -> {
        if (!Double.isNaN(event.getZoomFactor()) && event.getZoomFactor() > 0.8 && event.getZoomFactor() < 1.2) {
            double z = cameraPosition.getZ() / event.getZoomFactor();
            z = Math.max(z, -1000);
            z = Math.min(z, 0);
            cameraPosition.setZ(z);
        }
    };

    private final EventHandler<KeyEvent> keyEventHandler = event -> {
        System.out.println("KeyEvent ...");
        Duration currentTime;
        double CONTROL_MULTIPLIER = 0.1;
        double SHIFT_MULTIPLIER = 0.1;
        double ALT_MULTIPLIER = 0.5;

        switch (event.getCode()) {
            case Z:
                if (event.isShiftDown()) {
                    cameraXform.ry.setAngle(0.0);
                    cameraXform.rx.setAngle(0.0);
                    camera.setTranslateZ(-300.0);
                }
                cameraXform2.t.setX(0.0);
                cameraXform2.t.setY(0.0);
                break;
            case UP:
                if (event.isControlDown() && event.isShiftDown()) {
                    cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                } else if (event.isControlDown()) {
                    cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                } else if (event.isShiftDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + 5.0 * SHIFT_MULTIPLIER;
                    camera.setTranslateZ(newZ);
                }
                break;
            case DOWN:
                if (event.isControlDown() && event.isShiftDown()) {
                    cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                } else if (event.isControlDown()) {
                    cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                } else if (event.isShiftDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z - 5.0 * SHIFT_MULTIPLIER;
                    camera.setTranslateZ(newZ);
                }
                break;
            case RIGHT:
                if (event.isControlDown() && event.isShiftDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                } else if (event.isControlDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                }
                break;
            case LEFT:
                if (event.isControlDown() && event.isShiftDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER); // -
                } else if (event.isControlDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER); // -
                }
                break;
        }
    };

    private SimpleBooleanProperty yUp = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                setYUpRotate(180);
            } else {
                setYUpRotate(0);
            }
        }
    };
    private SimpleBooleanProperty rotateMode = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
        }
    };

    private SimpleBooleanProperty moveMode = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
        }
    };

    public SimpleBooleanProperty yUpProperty() {
        return yUp;
    }

    public SimpleBooleanProperty moveModeProperty() {
        return moveMode;
    }

    public SimpleBooleanProperty rotateModeProperty() {
        return rotateMode;
    }

    public EventHandler<MouseEvent> getMouseEventHandler() {
        return mouseEventHandler;
    }

    public EventHandler<KeyEvent> getKeyEventHandler() {
        return keyEventHandler;
    }

    public EventHandler<ScrollEvent> getScrollEventHandler() {
        return scrollEventHandler;
    }

    public EventHandler<ZoomEvent> getZoomEventHandler() {
        return zoomEventHandler;
    }

    public Xform getCameraXform() {
        return cameraXform;
    }

    public boolean getYUp() {
        return yUp.get();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public Rotate getCameraXRotate() {
        return cameraXRotate;
    }

    public Rotate getCameraYRotate() {
        return cameraYRotate;
    }

    public Translate getCameraPosition() {
        return cameraPosition;
    }

    public Rotate getCameraLookXRotate() {
        return cameraLookXRotate;
    }

    public Rotate getCameraLookZRotate() {
        return cameraLookZRotate;
    }

    public boolean getMoveMode() {
        return moveMode.get();
    }

    public boolean getRotateMode() {
        return rotateMode.get();
    }

    public void setSceneContent() {
        this.sceneContent = CityGMLEditorApp.getSceneContent();
    }

    public void setYUpRotate(int rate) {
        yUpRotate.setAngle(rate);
    }

    public void setYUp(boolean yUp) {
        this.yUp.set(yUp);
    }

    public void setHookingMousePrimaryButtonEvent(boolean isHook) {
        isHookingMousePrimaryButtonEvent = isHook;
    }

    public Camera() {
        this.light = CityGMLEditorApp.getLight();
        // CAMERA
        camera.setNearClip(1.0); // TODO: Workaround as per RT-31255
        camera.setFarClip(10000.0); // TODO: Workaround as per RT-31255

        camera.getTransforms().addAll(
                yUpRotate,
                cameraPosition,
                cameraLookXRotate,
                cameraLookZRotate);

        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraPosition.setZ(-cameraDistance);

        SessionManager sessionManager = SessionManager.getSessionManager();
        sessionManager.bind(cameraLookXRotate.angleProperty(), "cameraLookXRotate");
        sessionManager.bind(cameraLookZRotate.angleProperty(), "cameraLookZRotate");
        sessionManager.bind(cameraPosition.xProperty(), "cameraPosition.x");
        sessionManager.bind(cameraPosition.yProperty(), "cameraPosition.y");
        sessionManager.bind(cameraPosition.zProperty(), "cameraPosition.z");
        sessionManager.bind(cameraXRotate.angleProperty(), "cameraXRotate");
        sessionManager.bind(cameraYRotate.angleProperty(), "cameraYRotate");
        sessionManager.bind(camera.nearClipProperty(), "cameraNearClip");
        sessionManager.bind(camera.farClipProperty(), "cameraFarClip");

    }
}
