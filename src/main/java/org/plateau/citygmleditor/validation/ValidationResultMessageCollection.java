package org.plateau.citygmleditor.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultMessageCollection {
    private final List<ValidationResultMessage> messages = new ArrayList<ValidationResultMessage>();
    private int errorCount;
    private int warningCount;

    public void add(ValidationResultMessage message) {
        switch (message.getType()) {
            case Warning:
                warningCount++;
                break;
            case Error:
                errorCount++;
                break;
        }
        messages.add(message);
    }

    public void add(ValidationResultMessageType type, String message) {
        add(new ValidationResultMessage(type, message));
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public List<ValidationResultMessage> getMessages() {
        return messages;
    }
}
