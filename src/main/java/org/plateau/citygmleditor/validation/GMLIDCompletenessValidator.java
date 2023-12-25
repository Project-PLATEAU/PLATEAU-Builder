package org.plateau.citygmleditor.validation;

import org.apache.commons.lang3.ObjectUtils;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GMLIDCompletenessValidator implements IValidator {
    public List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException {
        Set<String> gmlIDs = new HashSet<>();
        List<ValidationResultMessage> messages = new ArrayList<>();

        messages.add(new ValidationResultMessage(
                ValidationResultMessageType.Info,
                "gml:idの完全性を検証中..."
        ));

        File file = new File(World.getActiveInstance().getCityModel().getGmlPath());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        List<Node> nodeList = new ArrayList<>();
        XmlUtil.recursiveFindNodeByAttribute(doc, nodeList, TagName.GML_ID);

        boolean hasErrorNull = false;
        for (Node node : nodeList) {
            String id = node.getAttributes().getNamedItem(TagName.GML_ID).getNodeValue();
            if (ObjectUtils.isEmpty(id)) {
                if (!hasErrorNull) {
                    hasErrorNull = true;
                    messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                            String.format("Exists GmlId null\n")));
                }
                continue;
            }

            if (gmlIDs.contains(id)) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    String.format("%sは重複して使用されています。\n", id)));
            }
            gmlIDs.add(id);
        }

        return messages;
    }
}
