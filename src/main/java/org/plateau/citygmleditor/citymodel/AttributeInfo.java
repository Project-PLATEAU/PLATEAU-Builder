package org.plateau.citygmleditor.citymodel;

import org.apache.xerces.parsers.DOMParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.swing.text.html.StyleSheet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class AttributeInfo {
    NodeList nodeListElement;
    private Document uroDocument;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;

    public void readUroSchemas(String path) {
        try {
            // DOMパーサのインスタンスを作成
            DOMParser parser = new DOMParser();
            // XMLファイルをパース
            parser.parse(path);
            // ドキュメントオブジェクトを取得
            document = parser.getDocument();

            // 新しいXMLドキュメントを作成
            builder = factory.newDocumentBuilder();
            uroDocument = builder.newDocument();
            // 新しいElementを作成
            Element rootElement = uroDocument.createElement("uro");
            // ElementをDocumentのルートとして追加
            uroDocument.appendChild(rootElement);
            // complexTypeタグを取得
            nodeListElement = document.getElementsByTagName("xs:element");

            for (int i = 0; i < nodeListElement.getLength(); i++) {
                Node node = nodeListElement.item(i);
                Node parentNode = node.getParentNode();
                if (((Element) parentNode).getTagName() == "xs:schema") {
                    // 最上位の属性をノードとして格納
                    Element element = (Element) node;
                    Node importedNode = uroDocument.importNode(element, false);
                    rootElement.appendChild(importedNode);
                    NodeList complexTypeNodeList = document.getElementsByTagName("xs:complexType");
                    traverseComplexType(complexTypeNodeList, element.getAttribute("type").substring(4),
                            (Element) importedNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // printNode(uroDocument.getDocumentElement(), 0);
    }

    // <xs:complexType>に対する処理
    private void traverseComplexType(NodeList complexTypeNodeList, String type, Element parentElement) {
        for (int i = 0; i < complexTypeNodeList.getLength(); i++) {
            Node node = complexTypeNodeList.item(i);
            Element element = (Element) node;

            // 親要素のType属性と一致するComplextTypeに対する処理
            if (element.getAttribute("name").equals(type)) {
                NodeList extensionNodeList = element.getElementsByTagName("xs:extension");
                // <xs:extension>
                if (extensionNodeList.getLength() != 0) {
                    traverseExtension(extensionNodeList, parentElement);
                }
                NodeList elementNodeList = element.getElementsByTagName("xs:element");
                // <xs:element>
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
                NodeList complexTypeNodeList = document.getElementsByTagName("xs:complexType");
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
                    NodeList complexTypeNodeList = document.getElementsByTagName("xs:complexType");
                    if (element.getAttribute("name") != "") {
                        Node importedNode = uroDocument.importNode(elementNode, false);
                        parentElement.appendChild(importedNode);
                        traverseComplexType(complexTypeNodeList, elementType, (Element) importedNode);
                    }
                }
            } else {
                String ref = element.getAttribute("ref");
                String substitutionGroup = element.getAttribute("substitutionGroup");
                elementType = element.getAttribute("type");
                if (substitutionGroup.length() != 0) {
                    elementType = elementType.substring(4);
                    NodeList complexTypeNodeList = document.getElementsByTagName("xs:complexType");
                    traverseComplexType(complexTypeNodeList, elementType, parentElement);
                }
                if (ref.length() != 0) {
                    ref = element.getAttribute("ref").substring(4);
                    NodeList newElementNodeList = document.getElementsByTagName("xs:element");
                    traverseElement(newElementNodeList, ref, parentElement);
                }
                if (element.getAttribute("name") != "") {
                    Node importedNode = uroDocument.importNode(elementNode, false);
                    parentElement.appendChild(importedNode);
                }
            }
        }
    }

    // ノードを表示するメソッド
    private static void printNode(Node node, int depth) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // インデント用の空白を生成
            String indent = new String(new char[depth * 2]).replace("\0", " ");
            Element element = (Element) node;
            // ノードの情報を表示
            System.out.println(indent + "Node Name: " + element.getAttribute("name"));

            // 子ノードがあれば、それぞれに対してこのメソッドを再帰的に呼び出す
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                printNode(childNodes.item(i), depth + 1);
            }
        }
    }
}
