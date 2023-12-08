package org.plateau.citygmleditor.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class XmlUtil {

  public static Logger logger = Logger.getLogger(XmlUtil.class.getName());

  /**
   * Get all node from xml file
   *
   * @param file    xml file
   * @param tagName tag name
   * @return list of node
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   */
  public static List<Node> getAllNodeFromXmlFile(File file, String tagName)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(file);
      doc.getDocumentElement().normalize();
      var resultList = new ArrayList<Node>();
      recursiveFindNodeByTagName(doc, resultList, tagName);
      return resultList;
    } catch (ParserConfigurationException | SAXException | IOException e) {
      logger.severe("Error while parsing xml file");
      throw e;
    }
  }

  /**
   * find node by tag name recursively
   * @param node parent node
   * @param resultList the list which stores the result
   * @param tagName tag name
   */
  public static void recursiveFindNodeByTagName(Node node, List<Node> resultList, String tagName) {
    if (node.hasChildNodes()) {
      var childNodes = node.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        var childNode = childNodes.item(i);
        if (childNode.getNodeName().equals(tagName)) {
          resultList.add(childNode);
        } else {
          recursiveFindNodeByTagName(childNode, resultList, tagName);
        }
      }
    }
  }
}
