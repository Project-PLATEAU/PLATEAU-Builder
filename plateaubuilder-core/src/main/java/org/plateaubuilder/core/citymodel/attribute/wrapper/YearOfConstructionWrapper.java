package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * YearOfConstruction属性の追加・削除などの操作処理の実体を持つクラス
 */
public class YearOfConstructionWrapper extends AbstractAttributeWrapper {
    public YearOfConstructionWrapper(ModelType modelType) {
        initialize(modelType, "yearOfConstruction");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        LocalDate yearOfConstruction = building.getYearOfConstruction();
        return yearOfConstruction != null ? String.valueOf(yearOfConstruction.getYear()) : "";
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        LocalDate localDate = LocalDate.parse(value, formatter);

        building.setYearOfConstruction(localDate);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetYearOfConstruction();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setYearOfConstruction(Integer.parseInt(value));
    }
}