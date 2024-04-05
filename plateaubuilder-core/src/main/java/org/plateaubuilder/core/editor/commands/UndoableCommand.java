package org.plateaubuilder.core.editor.commands;

import javafx.scene.Node;

public interface UndoableCommand {
    void redo();
    void undo();
    Node getUndoFocusTarget();
    Node getRedoFocusTarget();
}
