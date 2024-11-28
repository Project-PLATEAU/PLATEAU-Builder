package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.LocalityName;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * CountryName属性の追加・削除などの操作処理の実体を持つクラス
 */
public class CountryNameWrapper extends AbstractAttributeWrapper {
    public CountryNameWrapper(ModelType modelType) {
        initialize(modelType, "CountryName", "address");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        AddressDetails addressDetails = building.getAddress().get(0).getAddress().getXalAddress()
                .getAddressDetails();

        return addressDetails.getCountry().getCountryName().get(0).getContent();
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        AddressDetails addressDetails = building.getAddress().get(0).getAddress().getXalAddress()
                .getAddressDetails();
        ArrayList<CountryName> newCountryName = new ArrayList<CountryName>();

        newCountryName.add(new CountryName(value));
        addressDetails.getCountry().setCountryName(newCountryName);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        AddressDetails addressDetails = building.getAddress().get(0).getAddress().getXalAddress()
                .getAddressDetails();

        addressDetails.getCountry().unsetCountryName();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        Country country;

        // CountryNameの設定
        ArrayList<CountryName> countryNames = new ArrayList<>();
        countryNames.add(new CountryName(value));

        if (building.getAddress().get(0).getAddress() == null) {
            // Countryの設定
            country = new Country();
            country.setCountryName(countryNames);

            // AddressDetailsの設定
            AddressDetails addressDetails = new AddressDetails();
            addressDetails.setCountry(country);

            // XalAddressPropertyの設定
            XalAddressProperty xalAddressProperty = new XalAddressProperty();
            xalAddressProperty.setAddressDetails(addressDetails);

            // Addressの設定
            Address address = new Address();
            address.setXalAddress(xalAddressProperty);

            // AddressPropertyの設定
            AddressProperty addressProperty = new AddressProperty();
            addressProperty.setAddress(address);

            // AddressPropertyリストの設定
            ArrayList<AddressProperty> addressProperties = new ArrayList<>();
            addressProperties.add(addressProperty);
            building.setAddress(addressProperties);
        } else {
            AddressDetails addressDetails = building.getAddress().get(0).getAddress().getXalAddress()
                    .getAddressDetails();
            country = addressDetails.isSetCountry() ? addressDetails.getCountry() : new Country();
            country.setCountryName(countryNames);
            addressDetails.setCountry(country);
        }
    }
}