package org.plateau.citygmleditor.constant;

public enum PolygonRelationship {
    NOT_INTERSECT("1"), TOUCH("2"), INTERSECT_3D("3"), FLAT_INTERSECT("4"), OTHER("5");

    String value;

    PolygonRelationship(String text) {
        this.value = text;
    }

    public static PolygonRelationship getRelationshipByText(String text) {
        for (PolygonRelationship relationship : PolygonRelationship.values()) {
            if (relationship.value.equals(text)) return relationship;
        }
        throw new RuntimeException("Invalid relationship text");
    }
}