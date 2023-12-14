package org.plateau.citygmleditor.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;



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
  public static NodeList getAllTagFromXmlFile(File file, String tagName) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(file);
      doc.getDocumentElement().normalize();
      return doc.getElementsByTagName(tagName);

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
        }
        recursiveFindNodeByTagName(childNode, resultList, tagName);
      }
    }
  }

  public static Node findNearestParentByTagAndAttribute(Node node, String tagName, String attribute) {
    Node parentNode = node.getParentNode();
    if (parentNode == null) {
      return null;
    } else {
      if (parentNode.getNodeName().equals(tagName) && parentNode.getAttributes().getNamedItem(attribute) != null) {
        return parentNode;
      } else {
        return findNearestParentByTagAndAttribute(parentNode, tagName, attribute);
      }
    }
  }

  public static Node findNearestParentByAttribute(Node node, String attribute) {
    Node parentNode = node.getParentNode();
    if (parentNode == null) {
      return null;
    } else {
      if (parentNode.getAttributes().getNamedItem(attribute) != null) {
        return parentNode;
      } else {
        return findNearestParentByAttribute(parentNode, attribute);
      }
    }
  }

  public static List<Node> getTagsByRegex(String regex, Node tagInput) {
    List<Node> result = new ArrayList<>();
    Element element = (Element) tagInput;
    NodeList allTags = element.getElementsByTagName("*");

    for (int i = 0; i < allTags.getLength(); i++) {
      Node tag = allTags.item(i);
      String tagName = tag.getNodeName();
      // Check if the name tag matches the regular expression
      if (tagName.matches(regex)) {
        result.add(tag);
      }
    }
    return result;
  }
}
