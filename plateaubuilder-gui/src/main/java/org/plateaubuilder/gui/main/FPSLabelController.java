package org.plateaubuilder.gui.main;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FPSLabelController {
    @FXML
    private Label fpsLabel;

    private long lastTime = 0;
    private int frameCount = 0;

    public void initialize() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // 計算されたFPSを更新
                frameCount++;
                long elapsedTime = now - lastTime;
                if (elapsedTime >= 1e9) { // 1秒以上経過したらFPSを更新
                    double fps = frameCount / (elapsedTime / 1e9);
                    fpsLabel.setText("FPS: " + String.format("%.2f", fps));
                    frameCount = 0;
                    lastTime = now;
                }
            }
        };
        timer.start();
    }
}
