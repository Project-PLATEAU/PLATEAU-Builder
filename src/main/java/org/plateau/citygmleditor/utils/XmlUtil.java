package org.plateau.citygmleditor.utils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import static org.plateau.citygmleditor.validation.AppConst.DATE_TIME_FORMAT;
import static org.plateau.citygmleditor.validation.AppConst.PATH_FOLDER;


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
   * Get all node from xml file
   *
   * @param inputStream    inputStream of xml file
   * @return list of node
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   */
  public static Document getXmlDocumentFrom(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(inputStream);
      doc.getDocumentElement().normalize();
      return doc;

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

  public static void recursiveGetNodeByTagNameAndAttr(Node node, List<Node> resultList, String tagName, String attrName, String attrValue) {
    if (node.hasChildNodes()) {
      var childNodes = node.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        var childNode = childNodes.item(i);
        if (childNode.getNodeName().equals(tagName)) {
          if (!StringUtils.isEmpty(XmlUtil.getAttribute(childNode, attrName))) {
            if (XmlUtil.getAttribute(childNode, attrName).equals(attrValue)) {
              resultList.add(childNode);
              return;
            }
          }
        } else {
          recursiveGetNodeByTagNameAndAttr(childNode, resultList, tagName, attrName, attrValue);
        }
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

  public static Node findNearestParentByName(Node node, String name) {
    Node parentNode = node.getParentNode();
    if (parentNode == null) {
      return null;
    } else {
      if (parentNode.getNodeName() != null && parentNode.getNodeName().equals(name)) {
        return parentNode;
      } else {
        return findNearestParentByName(parentNode, name);
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

  public static void recursiveFindNodeByAttribute(Node node, List<Node> resultList, String attribute) {
    if (node.hasChildNodes()) {
      NodeList childNodes = node.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        var childNode = childNodes.item(i);
        if (childNode.getAttributes() != null && childNode.getAttributes().getNamedItem(attribute) != null) {
          resultList.add(childNode);
        }
        recursiveFindNodeByAttribute(childNode, resultList, attribute);
      }
    }
  }

  public static void recursiveFindAttributeContent(Node node, Set<String> resultList, String attribute) {
    if (!node.hasChildNodes()) return;
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      var childNode = childNodes.item(i);
      if (childNode.getAttributes() != null && childNode.getAttributes().getNamedItem(attribute) != null) {
        String xHref = childNode.getAttributes().getNamedItem(attribute).getTextContent().trim();
        if (!xHref.isBlank()) {
          String xHrefSub = xHref.substring(0, 1);
          // if x-href begin with # remove
          resultList.add(Objects.equals(xHrefSub, "#") ? xHref.substring(1) : xHref);
        }
      }
      recursiveFindAttributeContent(childNode, resultList, attribute);
    }
  }

  public static void writeErrorMessageInFile(List<String> messages) throws IOException {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    LocalDateTime now = LocalDateTime.now();

    if (!Files.exists(Path.of(PATH_FOLDER))) {
      Files.createDirectory(Path.of(PATH_FOLDER));
    }

    String filePath = String.format("%serror%s.txt", PATH_FOLDER, dtf.format(now));

    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
    for (String message : messages) {
      bw.write(message + "\n");
    }
    bw.close();
  }

  public static List<Node> findAllNodeByTag(Node node, String tagName) {
    var listNode = new ArrayList<Node>();
    recursiveFindNodeByTagName(node, listNode, tagName);
    return listNode;
  }

  public static String getGmlId(Node node) {
    if (node == null) return "";
    var attribute = node.getAttributes().getNamedItem("gml:id");
    return attribute != null ? attribute.getTextContent() : "";
  }

  public static <T> T getUserDataAttribute(Node node, String attributeName, Class<T> clazz) {
    var attribute = node.getUserData(attributeName);
    return attribute != null ? clazz.cast(attribute) : null;
  }

  public static String getAttribute(Node node, String attributeName) {
    var attribute = node.getAttributes().getNamedItem(attributeName);
    return attribute != null ? attribute.getTextContent() : "";
  }
}
