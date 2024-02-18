package org.plateau.citygmleditor.citymodel;

import org.apache.xerces.parsers.DOMParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.swing.text.html.StyleSheet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.FileInputStream;
import org.xml.sax.InputSource;
import java.util.ArrayList;
import java.util.HashSet;

public class CodeSpaceAttributeInfo {
    Document codeTypeDocument;
    Document sourceDocument;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    NodeList dictionaryEntryNodeList;

    /**
     * readCodeType
     * CodeTypeのxmlをパースする
     *
     * @param path CodeTypeのパス
     */
    public void readCodeType(String path) {
        try {
            // DOMパーサのインスタンスを作成
            DOMParser parser = new DOMParser();

            // XMLファイルをパース
            var fileStream = new FileInputStream(path);
            var inputSource = new InputSource(fileStream);
            parser.parse(inputSource);

            // ドキュメントオブジェクトを取得
            sourceDocument = parser.getDocument();
            // 新しいXMLドキュメントを作成
            builder = factory.newDocumentBuilder();
            codeTypeDocument = builder.newDocument();
            Element documentRootElement = sourceDocument.getDocumentElement();

            Element rootElement = codeTypeDocument.createElement("codeType");
            codeTypeDocument.appendChild(rootElement);
            dictionaryEntryNodeList = sourceDocument.getElementsByTagName("gml:dictionaryEntry");

            for (int i = 0; i < dictionaryEntryNodeList.getLength(); i++) {
                Node node = dictionaryEntryNodeList.item(i);
                Element element = (Element) node;

                NodeList definitionNodeList = element.getElementsByTagName("gml:Definition");
                Node importedDefinitionNode = codeTypeDocument.importNode(definitionNodeList.item(0), false);

                NodeList descriptionNodeList = element.getElementsByTagName("gml:description");
                Node descriptionNode = descriptionNodeList.item(0);

                NodeList nameNodeList = element.getElementsByTagName("gml:name");
                Node nameNode = nameNodeList.item(0);

                Node importedNameNode = codeTypeDocument.importNode(nameNode, true);
                Node importedDescriptionNode = codeTypeDocument.importNode(descriptionNode, true);

                ((Element) importedDefinitionNode).appendChild(importedNameNode);
                ((Element) importedDefinitionNode).appendChild(importedDescriptionNode);

                rootElement.appendChild(((Element) importedDefinitionNode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // printNode(codeTypeDocument.getDocumentElement(), 0);
    }

    /**
     * getCodeTypeAttributeDocument
     * パースしたCodeTypeの要素リストを返す
     *
     * @return パースしたuroの要素リスト
     */
    public Document getCodeTypeDocument() {
        return codeTypeDocument;
    }

    // ノードを表示するメソッド
    private static void printNode(Node node, int depth) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // インデント用の空白を生成
            String indent = new String(new char[depth * 2]).replace("\0", " ");
            Element element = (Element) node;
            // ノードの情報を表示
            System.out.println(indent + "Tag Name: " + element.getTagName() + "   Node Value: "
                    + node.getTextContent() + "   Parent TagName: " + element.getParentNode());

            // 子ノードがあれば、それぞれに対してこのメソッドを再帰的に呼び出す
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                printNode(childNodes.item(i), depth + 1);
            }
        }
    }
}