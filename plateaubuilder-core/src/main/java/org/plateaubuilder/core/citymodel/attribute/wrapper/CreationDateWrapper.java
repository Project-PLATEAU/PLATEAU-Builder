package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;

/**
 * CreationDate属性の追加・削除などの操作処理の実体を持つクラス
 */
public class CreationDateWrapper extends AbstractAttributeWrapper {
    public CreationDateWrapper() {
        initialize("creationDate");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getCreationDate());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(value, formatter);

        building.setCreationDate(localDate);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(value, formatter);

        building.setCreationDate(localDate);
    }
}
