package org.plateaubuilder.core.citymodel.attribute.reader;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

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
    private Set<String> visited = new HashSet<>(); // 既に訪問した型を追跡

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

            // ノード構造をファイルに保存
            // saveNodeToDesktop(xsdDocument.getDocumentElement());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return xsdDocument;
    }

    // <xs:complexType>に対する処理
    private void traverseComplexType(NodeList complexTypeNodeList, String type, Element parentElement) {
        // 既に訪問済みの型であれば、再帰を防止
        if (visited.contains(type)) {
            return;
        }
        visited.add(type); // 訪問済みリストに追加
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
        visited.remove(type); // 再帰から戻った際に訪問済みリストから削除
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
                    if (element.getAttribute("name") != "" && !element.getAttribute("abstract").matches("true")) {
                        Node importedNode = xsdDocument.importNode(elementNode, false);
                        String annotation = getAnnotation(elementNode);
                        Element importedElement = (Element) importedNode;
                        parentElement.setAttribute("annotation", annotation);
                        parentElement.appendChild(importedElement);
                        traverseSubstitutionGroup(element.getAttribute("name").substring(4),
                                sourceDocument.getElementsByTagName("xs:element"),
                                (Element) importedNode);
                        traverseComplexType(complexTypeNodeList, elementType, (Element) importedNode);
                    }
                }
            } else {
                // <xs:element ref="uro:DataQualityAttribute"/>
                String ref = element.getAttribute("ref");
                elementType = element.getAttribute("type");
                if (ref.length() != 0) {
                    ref = element.getAttribute("ref").substring(4);
                    NodeList newElementNodeList = sourceDocument.getElementsByTagName("xs:element");
                    traverseElement(newElementNodeList, ref, parentElement);
                    traverseSubstitutionGroup(ref,
                            newElementNodeList,
                            parentElement);
                }
                if (element.getAttribute("name") != "" && !element.getAttribute("abstract").matches("true")) {
                    Node importedNode = xsdDocument.importNode(elementNode, false);
                    Element importedElement = (Element) importedNode;
                    String annotation = getAnnotation(elementNode);
                    importedElement.setAttribute("annotation", annotation);
                    parentElement.appendChild(importedElement);
                    if (elementType.length() != 0 && elementType.startsWith("uro:")) {
                        NodeList complexTypeNodeList = sourceDocument.getElementsByTagName("xs:complexType");
                        traverseComplexType(complexTypeNodeList, elementType.substring(4), importedElement);
                    }
                }
            }
        }
    }

    // <substitutionGroup>に対する処理
    private void traverseSubstitutionGroup(String targetName, NodeList elementNodeList, Element parentElement) {
        for (int j = 0; j < elementNodeList.getLength(); j++) {
            Element element = (Element) elementNodeList.item(j);
            if (element.getAttribute("substitutionGroup").length() != 0
                    && targetName.matches(element.getAttribute("substitutionGroup").substring(4))) {
                if (!element.getAttribute("abstract").matches("true")) {
                    Node elementNode = element;
                    Node importedNode = xsdDocument.importNode(elementNode, false);
                    Element importedElement = (Element) importedNode;
                    parentElement.appendChild(importedElement);

                    traverseComplexType(sourceDocument.getElementsByTagName("xs:complexType"),
                            element.getAttribute("type").substring(4), importedElement);
                    traverseSubstitutionGroup(element.getAttribute("name"),
                            sourceDocument.getElementsByTagName("xs:element"), importedElement);
                } else {
                    traverseComplexType(sourceDocument.getElementsByTagName("xs:complexType"),
                            element.getAttribute("type").substring(4), parentElement);
                    traverseSubstitutionGroup(element.getAttribute("name"),
                            sourceDocument.getElementsByTagName("xs:element"), parentElement);
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

    /**
     * ノードの構造をファイルに出力します
     * 
     * @param node   出力対象のノード
     * @param depth  ノードの深さ
     * @param writer ファイル出力用のBufferedWriter
     * @throws IOException ファイル書き込み時のエラー
     */
    private static void printNode(Node node, int depth, BufferedWriter writer) throws IOException {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // インデント用の空白を生成
            String indent = new String(new char[depth * 2]).replace("\0", " ");
            Element element = (Element) node;

            // ノードの情報を文字列として構築
            String nodeInfo = String.format("%sTag Name: %s   Node Name: %s   ___   Parent TagName: %s%n",
                    indent,
                    element.getTagName(),
                    element.getAttribute("name"),
                    element.getParentNode().getNodeName());

            // ファイルに書き込み
            writer.write(nodeInfo);

            // 子ノードがあれば、それぞれに対してこのメソッドを再帰的に呼び出す
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                printNode(childNodes.item(i), depth + 1, writer);
            }
        }
    }

    /**
     * ノード構造をデスクトップに保存します
     * 
     * @param rootNode 出力対象のルートノード
     */
    private static void saveNodeToDesktop(Node rootNode) {
        // デスクトップのパスを取得
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";

        // タイムスタンプを含むファイル名を生成
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("node_structure_%s.txt", timestamp);

        // 出力ファイルのフルパスを構築
        Path filePath = Paths.get(desktopPath, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            // ファイルヘッダー情報を書き込み
            writer.write("XSD Node Structure - Generated at: " + timestamp + "\n");
            writer.write("----------------------------------------\n\n");

            // ノード構造を出力
            printNode(rootNode, 0, writer);

            System.out.println("Node structure has been saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}