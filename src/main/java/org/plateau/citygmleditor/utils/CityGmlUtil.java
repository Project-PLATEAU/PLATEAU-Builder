package org.plateau.citygmleditor.utils;

import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.exporters.GmlExporter;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;


public class CityGmlUtil {

    public static Logger logger = Logger.getLogger(CityGmlUtil.class.getName());

    public static String HOME_PATH = System.getProperty("user.home");

    /**
     * Get all node from xml file
     *
     * @param tagName tag name
     * @return list of node
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static NodeList getAllTagFromCityModel(CityModelView cityModelView, String tagName) throws ParserConfigurationException, IOException, SAXException {
        File gmlFile = createFileFromCityModel(cityModelView);
        try {
            return XmlUtil.getAllTagFromXmlFile(gmlFile, tagName);
        } finally {
            deleteFile(gmlFile);
        }
    }

    public static File createFileFromCityModel(CityModelView cityModel) {
        try {
            String path = HOME_PATH + "/Temp/" + UUID.randomUUID() + ".gml";
            GmlExporter.export(path, cityModel.getGmlObject(), cityModel.getSchemaHandler());
            return new File(path);
        } catch (ADEException | CityGMLWriteException | CityGMLBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        } else {
            logger.severe("File not exist");
        }
    }
}
