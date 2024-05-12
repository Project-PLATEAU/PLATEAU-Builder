package org.plateaubuilder.core.citymodel;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;

public class CodeListManager {
    Document codeTypeDocument;
    Document sourceDocument;

    /**
     * コードリストの定義ファイルをxmlから読み取り、Nodeとして取得します
     *
     * @param path コードリストの定義ファイルのパス
     */
    public void readCodeList(String path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        NodeList dictionaryEntryNodeList;
        try {
            // コードリストの定義ファイルからドキュメントを取得
            DOMParser parser = new DOMParser();
            var fileStream = new FileInputStream(path);
            var inputSource = new InputSource(fileStream);
            parser.parse(inputSource);
            sourceDocument = parser.getDocument();

            // CodeListの情報格納用の新しいドキュメントを作成
            builder = factory.newDocumentBuilder();
            codeTypeDocument = builder.newDocument();
            Element documentRootElement = sourceDocument.getDocumentElement();

            Element rootElement = codeTypeDocument.createElement("codeType");
            codeTypeDocument.appendChild(rootElement);
            dictionaryEntryNodeList = sourceDocument.getElementsByTagName("gml:dictionaryEntry");

            // 対象のコードリストから必要な情報を取得し、ノードリストに追加
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
     * パースしたCodeTypeの要素リストを取得します
     *
     * @return パースしたcodeTypeの要素リスト
     */
    public Document getCodeListDocument() {
        return codeTypeDocument;
    }

    /**
     * ノードをターミナル上で可視化します
     * 
     * @param node  表示対象のノード
     * @param depth 表示対象のノードの深さ
     */
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