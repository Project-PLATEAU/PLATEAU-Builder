package org.plateau.citygmleditor.validation;

public class ValidationResultMessage {
    private ValidationResultMessageType type;
    private String message = "";

    ValidationResultMessage() {
    }

    public ValidationResultMessage(ValidationResultMessageType type, String message) {
        this.type = type;
        this.message = message;
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
}
