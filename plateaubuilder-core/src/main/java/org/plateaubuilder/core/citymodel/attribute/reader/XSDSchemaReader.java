package org.plateaubuilder.core.citymodel.attribute.reader;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XSDSchemaReader {
    Document xsdDocument;
    Document sourceDocument;

    /**
     * スキーマ情報をxmlから読み取り、Documentとして格納します
     * 
     * @param path スキーマファイルのパス
     * @param type スキーマのタイプ
     */
    public Document readXSDSchemas(String path, String type) {
        NodeList elementNodeList;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            // スキーマファイルからドキュメントを取得
            DOMParser parser = new DOMParser();
            InputStream stream;
            stream = new URL(path).openStream();
            var inputSource = new InputSource(stream);
            parser.parse(inputSource);
            sourceDocument = parser.getDocument();

            // Uro情報格納用の新しいドキュメントを作成
            builder = factory.newDocumentBuilder();
            xsdDocument = builder.newDocument();
            Element documentRootElement = sourceDocument.getDocumentElement();
            String xmlnsName = documentRootElement.getAttribute("xmlns:" + type);
            Element rootElement = xsdDocument.createElement(type);
            rootElement.setAttribute("xmlns:" + type, xmlnsName);
            xsdDocument.appendChild(rootElement);

            // xs:elementタグを取得
            elementNodeList = sourceDocument.getElementsByTagName("xs:element");
            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                Node parentNode = node.getParentNode();
                Element element = (Element) node;

                // 最上位の属性をドキュメントに格納
                if (((Element) parentNode).getTagName() == "xs:schema" && element.getAttribute("abstract") != "true") {
                    Node importedNode = xsdDocument.importNode(element, false);
                    Element importedElement = (Element) importedNode;
                    String annotation = getAnnotation(importedNode);
                    importedElement.setAttribute("annotation", annotation);
                    rootElement.appendChild(importedNode);
                    NodeList complexTypeNodeList = sourceDocument.getElementsByTagName("xs:complexType");
                    traverseComplexType(complexTypeNodeList, element.getAttribute("type").substring(4),
                            (Element) importedNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // printNode(xsdDocument.getDocumentElement(), 0);
        return xsdDocument;
    }

    // <xs:complexType>に対する処理
    private void traverseComplexType(NodeList complexTypeNodeList, String type, Element parentElement) {
        for (int i = 0; i < complexTypeNodeList.getLength(); i++) {
            Node node = complexTypeNodeList.item(i);
            Element element = (Element) node;

            // 親要素のType属性と一致するComplexTypeに対する処理
            if (element.getAttribute("name").equals(type)) {
                NodeList extensionNodeList = element.getElementsByTagName("xs:extension");
                // <xs:extension>がある場合
                if (extensionNodeList.getLength() != 0) {
                    traverseExtension(extensionNodeList, parentElement);
                }
                NodeList elementNodeList = element.getElementsByTagName("xs:element");
                // <xs:element>がある場合
                if (elementNodeList.getLength() != 0) {
                    traverseElement(elementNodeList, null, parentElement);
                }
            }
        }
    }

    // <xs:extension>に対する処理
    private void traverseExtension(NodeList extensionNodeList, Element parentElement) {
        for (int j = 0; j < extensionNodeList.getLength(); j++) {
            Node extensionNode = extensionNodeList.item(j);
            Element extensionElement = (Element) extensionNode;
            String type = extensionElement.getAttribute("base");
            if (type.length() != 0) {
                type = type.substring(4);
                NodeList complexTypeNodeList = sourceDocument.getElementsByTagName("xs:complexType");
                traverseComplexType(complexTypeNodeList, type, parentElement);
            }
        }
    }

    // <xs:element>に対する処理
    private void traverseElement(NodeList elementNodeList, String type, Element parentElement) {
        String elementType;
        for (int j = 0; j < elementNodeList.getLength(); j++) {
            Node elementNode = elementNodeList.item(j);
            Element element = (Element) elementNode;

            if (type != null) {
                if (element.getAttribute("name").equals(type)) {
                    elementType = element.getAttribute("type").substring(4);
                    NodeList complexTypeNodeList = sourceDocument.getElementsByTagName("xs:complexType");
                    if (element.getAttribute("name") != "") {
                        Node importedNode = xsdDocument.importNode(elementNode, false);
                        String annotation = getAnnotation(elementNode);
                        Element importedElement = (Element) importedNode;
                        parentElement.setAttribute("annotation", annotation);
                        parentElement.appendChild(importedElement);
                        traverseComplexType(complexTypeNodeList, elementType, (Element) importedNode);
                    }
                }
            } else {
                String ref = element.getAttribute("ref");
                String substitutionGroup = element.getAttribute("substitutionGroup");
                elementType = element.getAttribute("type");
                if (substitutionGroup.length() != 0) {
                    elementType = elementType.substring(4);
                    NodeList complexTypeNodeList = sourceDocument.getElementsByTagName("xs:complexType");
                    traverseComplexType(complexTypeNodeList, elementType, parentElement);
                }
                if (ref.length() != 0) {
                    ref = element.getAttribute("ref").substring(4);
                    NodeList newElementNodeList = sourceDocument.getElementsByTagName("xs:element");
                    traverseElement(newElementNodeList, ref, parentElement);
                }
                if (element.getAttribute("name") != "") {
                    Node importedNode = xsdDocument.importNode(elementNode, false);
                    Element importedElement = (Element) importedNode;
                    String annotation = getAnnotation(elementNode);
                    importedElement.setAttribute("annotation", annotation);
                    parentElement.appendChild(importedElement);
                }
            }
        }

    }

    private String getAnnotation(Node node) {
        // 子ノードを取得
        NodeList childNodes = node.getChildNodes();
        Node child = childNodes.item(1);
        // 子ノードがElementの場合、そのタグ名をチェック
        if (child instanceof Element) {
            Element childElement = (Element) child;
            if (childElement.getTagName().equals("xs:annotation")) {
                child = child.getChildNodes().item(1).getChildNodes().item(0);
                String value = child.getNodeValue();
                return value;
            }
        }
        return "null";
    }
}
