
package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.util.ArrayList;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * LocalityName属性の追加・削除などの操作処理の実体を持つクラス
 */
public class LocalityNameWrapper extends AbstractAttributeWrapper {
    public LocalityNameWrapper(ModelType modelType) {
        initialize(modelType, "LocalityName", "address");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        AddressProperty addressProperty = building.getAddress().get(0);
        AddressDetails addressDetails = addressProperty.getAddress().getXalAddress()
                .getAddressDetails();

        return addressDetails.getCountry().getLocality().getLocalityName().get(0).getContent();
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        AddressDetails addressDetails = building.getAddress().get(0).getAddress().getXalAddress()
                .getAddressDetails();
        Country country = addressDetails.getCountry();
        Locality locality = country.getLocality();
        ArrayList<LocalityName> newLocalityName = new ArrayList<LocalityName>();

        locality.addLocalityName(new LocalityName(value));
        newLocalityName.add(new LocalityName(value));
        locality.setLocalityName(newLocalityName);
        country.setLocality(locality);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        AddressDetails addressDetails = building.getAddress().get(0).getAddress().getXalAddress()
                .getAddressDetails();

        addressDetails.getCountry().unsetLocality();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        Country country;
        Locality locality = new Locality();
        ArrayList<LocalityName> localityNames = new ArrayList<LocalityName>();

        localityNames.add(new LocalityName(value));
        locality.setLocalityName(localityNames);

        if (building.getAddress().get(0).getAddress() == null) {
            // Countryの設定
            country = new Country();
            country.setLocality(locality);

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
            country.setLocality(locality);
            addressDetails.setCountry(country);
        }
    }
}