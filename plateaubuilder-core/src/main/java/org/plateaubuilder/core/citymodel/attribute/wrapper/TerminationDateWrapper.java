package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.citygml4j.model.citygml.building.AbstractBuilding;

/**
 * TerminationDate属性の追加・削除などの操作処理の実体を持つクラス
 */
public class TerminationDateWrapper extends AbstractAttributeWrapper {
    public TerminationDateWrapper() {
        initialize("terminationDate");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getTerminationDate());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(value, formatter);

        building.setTerminationDate(localDate);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetTerminationDate();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(value, formatter);

        building.setTerminationDate(localDate);
    }
}