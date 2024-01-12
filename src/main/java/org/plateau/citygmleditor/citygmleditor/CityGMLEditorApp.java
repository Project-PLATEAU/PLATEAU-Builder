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
package org.plateau.citygmleditor.citygmleditor;

import java.io.File;
import java.util.List;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Group;

import javafx.stage.Window;
import org.plateau.citygmleditor.world.*;
import org.plateau.citygmleditor.FeatureSelection;

/**
 * JavaFX 3D Viewer Application
 */
public class CityGMLEditorApp extends Application {
    public static final String GML_FILE_URL_PROPERTY = "fileUrl";
    private static Scene scene;
    private static Camera camera;
    private static AxisGizmo axisGizmo;
    private static Light light;
    private static SceneContent sceneContent;
    private static AntiAliasing antiAliasing;
    private static AutoScalingGroup autoScalingGroups;

    private SessionManager sessionManager;

    private static FeatureSelection selection;

    public static Camera getCamera() {
        return camera;
    }

    public static AxisGizmo getAxisGizmo() {
        return axisGizmo;
    }

    public static Light getLight() {
        return light;
    }

    public static SceneContent getSceneContent() {
        return sceneContent;
    }

    public static Scene getScene() {
        return scene;
    }

    public static Window getWindow() {
        return scene.getWindow();
    }

    public static AntiAliasing getAntiAliasing() {
        return antiAliasing;
    }

    public static AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroups;
    }

    public static FeatureSelection getFeatureSellection() {
        return selection;
    }

    @Override
    public void start(Stage stage) throws Exception {
        sessionManager = SessionManager.createSessionManager("CityGMLEditor");
        sessionManager.loadSession();

        World.setActiveInstance(new World(), new Group());
        autoScalingGroups = new AutoScalingGroup(2);
        light = new Light();
        antiAliasing = new AntiAliasing();
        camera = new Camera();
        sceneContent = new SceneContent();

        antiAliasing.setSceneContent();
        camera.setSceneContent();

        axisGizmo = new AxisGizmo();
        selection = new FeatureSelection();

        World.getRoot3D().getChildren().add(camera.getCameraXform());
        World.getRoot3D().getChildren().add(autoScalingGroups);

        sceneContent.rebuildSubScene();

        scene = new Scene(
                FXMLLoader.<Parent>load(Objects.requireNonNull(CityGMLEditorApp.class.getResource("fxml/main.fxml"))),
                1024, 600, true);

        stage.setScene(scene);
        stage.show();

        selection.registerClickEvent(scene);

        stage.setOnCloseRequest(event -> sessionManager.saveSession());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
