package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * YearOfDemolition属性の追加・削除などの操作処理の実体を持つクラス
 */
public class YearOfDemolitionWrapper extends AbstractAttributeWrapper {
    public YearOfDemolitionWrapper(ModelType modelType) {
        initialize(modelType, "yearOfDemolition");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        LocalDate yearOfDemolition = building.getYearOfDemolition();
        return yearOfDemolition != null ? String.valueOf(yearOfDemolition.getYear()) : "";
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        LocalDate localDate = LocalDate.parse(value, formatter);

        building.setYearOfDemolition(localDate);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetYearOfDemolition();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setYearOfDemolition(Integer.parseInt(value));
    }
}