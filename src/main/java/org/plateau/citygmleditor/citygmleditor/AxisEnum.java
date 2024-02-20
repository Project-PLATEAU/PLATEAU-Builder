package org.plateau.citygmleditor.citygmleditor;

public enum AxisEnum {
    X("X"),
    Y("Y"),
    Z("Z"),
    NEGATIVE_X("-X"),
    NEGATIVE_Y("-Y"),
    NEGATIVE_Z("-Z");

    final private String displayName;

    private AxisEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
