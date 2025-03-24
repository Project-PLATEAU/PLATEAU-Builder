package org.plateaubuilder.core.citymodel.attribute;

public class Occurs {
    private String min;
    private String max;

    public Occurs(String min, String max) {
        this.min = min;
        this.max = max;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }
}
