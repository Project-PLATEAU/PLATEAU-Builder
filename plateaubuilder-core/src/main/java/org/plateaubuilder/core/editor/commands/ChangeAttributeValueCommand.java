package org.plateaubuilder.core.editor.commands;

import javafx.scene.Node;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;

/**
 * 属性の値が変更された際の Undo/Redo コマンドクラスです。
 * このクラスは変更前/変更後の値を保持し、undo() と redo() で値を切り替えます。
 */
public class ChangeAttributeValueCommand extends AbstractCityGMLUndoableCommand {
    private final AttributeItem attributeItem;
    private final String oldValue;
    private final String newValue;
    private final Node focusTarget;

    public ChangeAttributeValueCommand(AttributeItem attributeItem, String oldValue, String newValue,
            Node focusTarget) {
        this.attributeItem = attributeItem;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.focusTarget = focusTarget;
    }

    @Override
    public void redo() {
        attributeItem.setValue(newValue);
    }

    @Override
    public void undo() {
        attributeItem.setValue(oldValue);
    }

    @Override
    public Node getUndoFocusTarget() {
        return focusTarget;
    }

    @Override
    public Node getRedoFocusTarget() {
        return focusTarget;
    }
}