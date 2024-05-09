package org.plateaubuilder.core.editor.commands;

import java.util.ArrayList;
import java.util.List;

import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.editor.Editor;

import javafx.scene.Node;

public class UndoManager {
    private final int maxHistoryCount;

    private final List<UndoableCommand> undoStack = new ArrayList<>();
    private final List<UndoableCommand> redoStack = new ArrayList<>();

    public UndoManager(int maxHistoryCount) {
        this.maxHistoryCount = maxHistoryCount;
    }

    public void addCommand(UndoableCommand command) {
        command.redo();
        undoStack.add(0, command);
        if (undoStack.size() > maxHistoryCount) {
            undoStack.remove(undoStack.size() - 1);
        }
        redoStack.clear();

        focus(command.getRedoFocusTarget());
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }


    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void undo() {
        if (undoStack.isEmpty())
            return;

        var command = undoStack.get(0);
        command.undo();
        undoStack.remove(0);
        redoStack.add(0, command);

        focus(command.getUndoFocusTarget());
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void redo() {
        if (redoStack.isEmpty())
            return;

        var command = redoStack.get(0);
        command.redo();
        redoStack.remove(0);
        undoStack.add(0, command);

        focus(command.getRedoFocusTarget());
    }

    private static void focus(Node target) {
        if (target == null)
            return;

        if (target instanceof IFeatureView) {
            Editor.getFeatureSellection().select((IFeatureView) target);
        }
    }
}