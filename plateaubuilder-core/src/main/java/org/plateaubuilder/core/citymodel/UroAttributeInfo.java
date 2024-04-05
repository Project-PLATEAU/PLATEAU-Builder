package org.plateaubuilder.core.citymodel;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class UroAttributeInfo {
    NodeList nodeListElement;
    Document uroDocument;
    Node uroNode;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;

    public void readUroSchemas(String path) {
        try {
            // DOMパーサのインスタンスを作成
            DOMParser parser = new DOMParser();
            // XMLファイルをパース
            InputStream stream;
            stream = new URL(path).openStream();
            var inputSource = new InputSource(stream);
            parser.parse(inputSource);
            // ドキュメントオブジェクトを取得
            document = parser.getDocument();
            // 新しいXMLドキュメントを作成
            builder = factory.newDocumentBuilder();
            uroDocument = builder.newDocument();
            Element documentRootElement = document.getDocumentElement();
            String xmlnsUro = documentRootElement.getAttribute("xmlns:uro");
            // 新しいElementを作成
            Element rootElement = uroDocument.createElement("uro");
            rootElement.setAttribute("xmlns:uro", xmlnsUro);
            // ElementをDocumentのルートとして追加
            uroDocument.appendChild(rootElement);
            // complexTypeタグを取得
            nodeListElement = document.getElementsByTagName("xs:element");

            for (int i = 0; i < nodeListElement.getLength(); i++) {
                Node node = nodeListElement.item(i);
                Node parentNode = node.getParentNode();
                Element element = (Element) node;

                if (((Element) parentNode).getTagName() == "xs:schema" && element.getAttribute("abstract") != "true") {
                    // 最上位の属性をノードとして格納
                    Node importedNode = uroDocument.importNode(element, false);
                    Element importedElement = (Element) importedNode;
                    String annotation = getAnnotation(importedNode);
                    importedElement.setAttribute("annotation", annotation);
                    rootElement.appendChild(importedNode);
                    NodeList complexTypeNodeList = document.getElementsByTagName("xs:complexType");
                    traverseComplexType(complexTypeNodeList, element.getAttribute("type").substring(4),
                            (Element) importedNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        uroNode = uroDocument.getDocumentElement();
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
                        // System.out.println("element.getAttribute(\"name\"):" +
                        // element.getAttribute("name"));
                        Node importedNode = uroDocument.importNode(elementNode, false);
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
        // <xs:documentation>要素が見つからなかった場合はfalseを返す
        return "null";

    }

    /**
     * removeExtraItems
     * 追加可能な要素リストから不要な要素を削除
     * 
     * @param パースしたuroの要素リスト
     */
    private void removeExtraItems(Node node) {
        NodeList childNodes = node.getChildNodes();
        ArrayList<Node> removeNodeList = new ArrayList<>();

        // 重複チェック用のセット
        HashSet<String> nodeNameSet = new HashSet<>();

        // 重複をチェックし、条件に合致するノードを削除対象リストに追加
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) childNode;
                String nodeName = element.getAttribute("name");
                String nodeNameLowerCase = nodeName.toLowerCase();
                String abstractInfo = element.getAttribute("abstract");

                // 大文字で始まるノードを削除対象に
                if (Character.isUpperCase(nodeName.charAt(0))) {
                    removeNodeList.add(childNode);
                }
                // abstract="true"属性を持つノードを削除対象に
                if (abstractInfo.equals("true")) {
                    removeNodeList.add(childNode);
                }
            }
        }

        // 削除対象のノードを削除
        for (Node n : removeNodeList) {
            // 親ノードがこのノードを持っているか確認
            if (n.getParentNode() == node) {
                node.removeChild(n);
            }
        }
    }

    /**
     * getUroAttributeDocument
     * パースしたuroの要素リストを返す
     * 
     * @return パースしたuroの要素リスト
     */
    public Document getUroAttributeDocument() {
        removeExtraItems(uroDocument.getDocumentElement());
        return uroDocument;
    }

    // ノードを表示するメソッド
    private static void printNode(Node node, int depth) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // インデント用の空白を生成
            String indent = new String(new char[depth * 2]).replace("\0", " ");
            Element element = (Element) node;
            // ノードの情報を表示
            System.out.println(indent + "Tag Name: " + element.getTagName() + "   Node Name: "
                    + element.getAttribute("name") + "___" + element.getAttribute("annotation") + "   Parent TagName: "
                    + element.getParentNode());

            // 子ノードがあれば、それぞれに対してこのメソッドを再帰的に呼び出す
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                printNode(childNodes.item(i), depth + 1);
            }
        }
    }
}
