package org.plateau.citygmleditor.validation;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class L06CompletenessValidator implements IValidator{
    private final String postListXml = "gml:posList";
    private final String parentNote = "bldg:Building";

    @Override
    public List<ValidationResultMessage> validate(CityModel cityModel) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();

        if (Objects.isNull(cityModel)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    "The model Null"));
            return messages;
        }

        File input = new File(World.getActiveInstance().getCityModel().getGmlPath());
        NodeList notes = XmlUtil.getAllTagFromXmlFile(input, postListXml);

        DirectPosition lowerCorner = cityModel.getBoundedBy().getEnvelope().getLowerCorner();
        DirectPosition upperCorner = cityModel.getBoundedBy().getEnvelope().getUpperCorner();
        if (Objects.isNull(lowerCorner) || Objects.isNull(upperCorner)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    "The upperCorner or lowerCorner is Null"));
            return messages;
        }

        for (int i = 0; i < notes.getLength(); i++) {
            Node node = notes.item(i);
            List<String> values = new ArrayList<>(Arrays.asList(node.getFirstChild().getNodeValue().split(" ")));
            if (values.size()%3 != 0) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                        "The coordinates in postList error!"));
            }

            Node nodeParentBuilding = getParentNoteBuilding(node);
            String id = nodeParentBuilding.getAttributes().getNamedItem("gml:id").getTextContent();
            String error = String.format("gml:id=\"%s\"のbuildingにエラー座標値：", id);
            boolean newBuildingObject = true;
            if (messages.size() != 0 && messages.get(messages.size()-1).getMessage().contains(error))
            {
                error = messages.get(messages.size()-1).getMessage();
                newBuildingObject = false;
            }

            for (int j = 0; j < values.size(); j+=3) {
                if (lowerCorner.getValue().get(0) > Double.parseDouble(values.get(j)) || Double.parseDouble(values.get(j)) > upperCorner.getValue().get(0)
                        || lowerCorner.getValue().get(1) > Double.parseDouble(values.get(j+1)) || Double.parseDouble(values.get(j+1)) > upperCorner.getValue().get(1)
                        || lowerCorner.getValue().get(2) > Double.parseDouble(values.get(j+2)) || Double.parseDouble(values.get(j+2)) > upperCorner.getValue().get(2))
                {
                    error = String.format("%s\n%s %s %s", error, values.get(j), values.get(j+1), values.get(j+2));
                }
            }
            if (newBuildingObject && error.contains("\n")) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                        error));
            }
        }

        return messages;
    }

    private Node getParentNoteBuilding(Node node) {
        if (node.getParentNode().getNodeName() == null) {
            return null;
        }
        else if (node.getParentNode().getNodeName().equals(parentNote)) {
            return node.getParentNode();
        }
        return getParentNoteBuilding(node.getParentNode());
    }
}
