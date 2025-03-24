package org.plateaubuilder.core.citymodel;

import java.util.EventListener;

public interface CityModelHierarchyChangeListener extends EventListener {
    void onChange();
}
