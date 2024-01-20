package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lbldg03LogicalAccuaracyValidator implements IValidator {

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();

        File input = new File(World.getActiveInstance().getCityModel().getGmlPath());
        NodeList windowNodeList = XmlUtil.getAllTagFromXmlFile(input, TagName.BLDG_WINDOW);

        for (int i = 0; i < windowNodeList.getLength(); i++) {
            if (Objects.isNull(XmlUtil.findNearestParentByName(windowNodeList.item(i), TagName.BLDG_BOUNDARY_SURFACE))) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageError.ERR_LBLDG_03_001_2));
            }
        }

        NodeList doorNodeList = XmlUtil.getAllTagFromXmlFile(input, TagName.BLDG_DOOR);
        for (int i = 0; i < doorNodeList.getLength(); i++) {
            if (Objects.isNull(XmlUtil.findNearestParentByName(doorNodeList.item(i), TagName.BLDG_BOUNDARY_SURFACE))) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageError.ERR_LBLDG_03_001_3));
            }
        }

        return messages;
    }
}
