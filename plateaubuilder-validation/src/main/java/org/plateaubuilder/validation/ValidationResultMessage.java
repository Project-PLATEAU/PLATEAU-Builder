package org.plateaubuilder.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultMessage {
    private ValidationResultMessageType type;
    private String message = "";

    private List<GmlElementError> elementErrors = new ArrayList<>();

    ValidationResultMessage() {
    }

    public ValidationResultMessage(ValidationResultMessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public ValidationResultMessage(ValidationResultMessageType type, String message,
        List<GmlElementError> elementErrors) {
        this.type = type;
        this.message = message;
        this.elementErrors = elementErrors;
    }

    public ValidationResultMessageType getType() {
        return type;
    }

    public void setType(ValidationResultMessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GmlElementError> getElementErrors() {
        return elementErrors;
    }

    public void setElementErrors(List<GmlElementError> elementErrors) {
        this.elementErrors = elementErrors;
    }
}
