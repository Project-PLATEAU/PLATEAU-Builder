package org.plateau.citygmleditor.world;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.plateau.citygmleditor.citygmleditor.AutoScalingGroup;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;

public class CoordinateGrid {
    private Group grid;
    private AutoScalingGroup autoScalingGroup;

    private SimpleBooleanProperty showGrid = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            if (get()) {
                if (grid == null) {
                    createGrid();
                }
                autoScalingGroup.getChildren().add(grid);
            } else if (grid != null) {
                autoScalingGroup.getChildren().remove(grid);
            }
        }
    };

    public boolean getShowGrid() {
        return showGrid.get();
    }

    public SimpleBooleanProperty showGridProperty() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid.set(showGrid);
    }

    private void createGrid() {
        grid = new Group();
        int gridSize = 100;
        double gridSpacing = 100.0;
        for (int i = -gridSize / 2; i <= gridSize / 2; i++) {
            double position = i * gridSpacing;
            Line lineX = new Line(position, -gridSize * gridSpacing / 2, position, gridSize * gridSpacing / 2);
            Line lineY = new Line(-gridSize * gridSpacing / 2, position, gridSize * gridSpacing / 2, position);
            if (position == 0) {
                lineX.setStroke(Color.GREEN);
                lineY.setStroke(Color.RED);
            } else if (position % (100.0 * 10) == 0) {
                lineX.setStroke(Color.WHITESMOKE);
                lineY.setStroke(Color.WHITESMOKE);
            } else {
                lineX.setStroke(Color.GRAY);
                lineY.setStroke(Color.GRAY);
            }
            grid.getChildren().addAll(lineX, lineY);
        }
    }

    public CoordinateGrid() {
        this.autoScalingGroup = CityGMLEditorApp.getAutoScalingGroup();
    }
}
