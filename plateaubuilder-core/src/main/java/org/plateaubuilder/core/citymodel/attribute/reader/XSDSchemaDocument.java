package org.plateaubuilder.core.citymodel.attribute.reader;

import static java.util.Map.entry;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.Measure;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.attribute.Attribute;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.opengis.gml.StringOrRefType;

public class XSDSchemaDocument {
    private static final Map<String, Class> typeMap = Map.ofEntries(entry("xs:string", String.class), entry("xs:integer", Integer.class),
            entry("xs:double", Double.class), entry("xs:gYear", LocalDate.class), entry("xs:date", ZonedDateTime.class), entry("xs:boolean", Boolean.class),
            entry("xs:anyURI", String.class), entry("gml:CodeType", Code.class), entry("gml:LengthType", Length.class), entry("gml:MeasureType", Measure.class),
            entry("gml:StringOrRefType", StringOrRefType.class));

    private static final Set<String> ignoreTypes = Set.of("urf:TargetPropertyType", "gml:MultiSurfacePropertyType", "gml:MultiCurvePropertyType",
            "gml:MultiPointPropertyType", "urf:BoundaryPropertyType");

    Document xsdDocument;

    public void initialize(String path, String type) {
        XSDSchemaReader xsdSchemaReader = new XSDSchemaReader();
        xsdDocument = xsdSchemaReader.readXSDSchemas(path, type);
    }

    private void removeExtraItems(Node node) {
        NodeList nodeList = node.getChildNodes();
        ArrayList<Node> removeNodeList = new ArrayList<>();
        // 重複をチェックし、条件に合致するノードを削除対象リストに追加
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) childNode;
                // 大文字で始まるノードを削除対象に追加
                if (Character.isUpperCase(element.getAttribute("name").charAt(0))) {
                    removeNodeList.add(childNode);
                }
                // abstract="true"属性を持つノードを削除対象に追加
                if (element.getAttribute("abstract").equals("true")) {
                    removeNodeList.add(childNode);
                }
            }
        }

        // 削除対象のノードを削除
        for (Node n : removeNodeList) {
            // 親ノードが対象ノードを持つかどうかを確認
            if (n.getParentNode() == node) {
                node.removeChild(n);
            }
        }
    }

    /**
     * パースしたxsdの要素リストを取得します
     * 
     * @return パースしたxsdの要素リスト
     */
    public Document getXSDDocument() {
        removeExtraItems(xsdDocument.getDocumentElement());
        return xsdDocument;
    }

    /**
     * 条件を満たす要素の一覧を返却します
     *
     * @param targetName            検索のキーとなる対象属性の名前
     * @param required              追加が必須となっている要素のみを抽出するかどうかを表すflag
     * @param treeViewChildItemList 追加済みの子属性のリスト
     * @param type                  削除対象の要素の種別(uroなど)
     * @return 条件にマッチする要素のリスト
     */
    public ArrayList<ArrayList<String>> getElementList(String targetName, boolean required,
            ArrayList<String> treeViewChildItemList, String type) {
        ArrayList<ArrayList<String>> attributeList = new ArrayList<ArrayList<String>>();

        // ルート要素を取得
        if (targetName == "root") {
            // Uro要素の取得
            Node rootNode = xsdDocument.getDocumentElement();
            Element targetElement = (Element) rootNode;
            NodeList elementNodeList = rootNode.getChildNodes();
            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    targetElement = (Element) node;
                    ArrayList<String> attributeSet = new ArrayList<String>();
                    attributeSet.add(type + ":" + targetElement.getAttribute("name"));
                    attributeSet.add(targetElement.getAttribute("type"));

                    attributeSet.add(targetElement.getAttribute("annotation"));
                    Node childNode = node.getChildNodes().item(0);
                    Element childElement = (Element) childNode;
                    if (childElement != null) {
                        attributeSet.add(type + ":" + childElement.getAttribute("name"));
                    } else {
                        attributeSet.add(null);
                    }
                    attributeList.add(attributeSet);
                }
            }
        } else {
            // 第二階層以下の要素を取得

            // 追加対象の基準となる親要素を取得
            Node rootNode = xsdDocument.getDocumentElement();
            Element targetElement = (Element) rootNode;
            NodeList elementNodeList = rootNode.getChildNodes();
            targetName = targetName.substring(4);

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (targetName.equals(element.getAttribute("name"))) {
                        targetElement = (Element) node;
                    }
                }
            }
            if (!targetElement.getTagName().equals(type)) {
                // 基準となる要素の子要素を取得
                NodeList targetNodeList = targetElement.getElementsByTagName("xs:element");

                for (int j = 0; j < targetNodeList.getLength(); j++) {
                    Node node = targetNodeList.item(j);
                    Element element = (Element) node;
                    int count = 0;
                    if (treeViewChildItemList != null) {
                        for (String itemName : treeViewChildItemList) {
                            if (itemName.equals(type + ":" + element.getAttribute("name"))) {
                                count++;
                            }
                        }
                    }
                    if (!targetName.toLowerCase().matches(element.getAttribute("name").toLowerCase())) {
                        String maxOccurs = element.getAttribute("maxOccurs");
                        int max;
                        if (maxOccurs.equals("unbounded")) {
                            max = Integer.MAX_VALUE;
                        } else if (maxOccurs == "") {
                            max = 1;
                        } else {
                            max = Integer.parseInt(maxOccurs);
                        }
                        if (count < max) {
                            ArrayList<String> attributeSet = new ArrayList<>();
                            if (required) {
                                if (element.getAttribute("minOccurs") == ""
                                        || Integer.parseInt(element.getAttribute("minOccurs")) > 0) {
                                    attributeSet.add(type + ":" + element.getAttribute("name"));
                                    attributeSet.add(element.getAttribute("type"));
                                    attributeSet.add(element.getAttribute("annotation"));
                                    attributeList.add(attributeSet);
                                }
                            } else {
                                attributeSet.add(type + ":" + element.getAttribute("name"));
                                attributeSet.add(element.getAttribute("type"));
                                attributeSet.add(element.getAttribute("annotation"));
                                attributeList.add(attributeSet);
                            }
                        }
                    }
                }
            }
        }
        return attributeList;
    }

    /**
     * 対象の要素のtype属性の内容を取得します
     *
     * @param attributeKeyName       確認対象の要素
     * @param parentAttributeKeyName 確認対象の親要素の名前
     * @param type                   削除対象の要素の種別(uroなど)
     */
    public String getType(String attributeKeyName,
            String parentAttributeKeyName, String type) {
        Node rootNode = xsdDocument.getDocumentElement();
        NodeList nodeList = rootNode.getChildNodes();
        Element baseElement = (Element) rootNode;
        if (parentAttributeKeyName == null)
            parentAttributeKeyName = type;

        // 確認対象の基準となる親要素を取得
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (parentAttributeKeyName.equals(type + ":" + element.getAttribute("name"))) {
                    baseElement = element;
                }
            }
        }

        if (baseElement != null) {
            // 基準となる要素の子要素を取得
            NodeList targetNodeList = baseElement.getElementsByTagName("xs:element");
            for (int j = 0; j < targetNodeList.getLength(); j++) {
                Node node = targetNodeList.item(j);
                Element element = (Element) node;
                if (attributeKeyName.matches(type + ":" + element.getAttribute("name"))) {
                    return element.getAttribute("type");
                }
            }
        }
        return null;
    }

    /**
     * 対象の要素が削除可能かどうかを判別します
     *
     * @param elementName 削除対象の要素の名前
     * @param parentName  削除対象の親要素の名前
     * @param type        削除対象の要素の種別(uroなど)
     */
    public Boolean isDeletable(String elementName,
            String parentName, String type) {

        Node rootNode = xsdDocument.getDocumentElement();
        NodeList nodeList = rootNode.getChildNodes();
        Element baseElement = null;

        // 削除対象の基準となる親要素を取得
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (parentName.equals(type + ":" + element.getAttribute("name"))) {
                    baseElement = element;
                }
            }
        }
        if (baseElement != null) {
            // 基準となる要素の子要素を取得
            NodeList targetNodeList = baseElement.getElementsByTagName("xs:element");
            for (int j = 0; j < targetNodeList.getLength(); j++) {
                Node node = targetNodeList.item(j);
                Element element = (Element) node;
                if (elementName.matches(type + ":" + element.getAttribute("name"))) {
                    if (element.getAttribute("minOccurs") == "") {
                        return false;
                    } else if (Integer.parseInt(element.getAttribute("minOccurs")) > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 編集可能な属性かを判断します
     * 
     * @param selectedAttributeItem 編集対象のAttributeItem
     * @param type                  編集対象の属性が持つタイプ
     */
    public boolean isEditable(AttributeItem selectedAttributeItem, String type) {
        String selectedItemName = selectedAttributeItem.getName();
        int childElementLength = getElementList(selectedItemName, false, null, type).size();
        if (childElementLength != 0) {
            return false;
        }
        return true;
    }

    public List<Attribute> getGenericApplicationProperties(String namespace, String substitutionGroupName) {
        Node rootNode = xsdDocument.getDocumentElement();
        NodeList nodeList = rootNode.getChildNodes();

        var targetSubstitutionGroup = String.format("%s:%s", namespace, substitutionGroupName);
        var properties = new ArrayList<Attribute>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                var substitutionGroup = element.getAttribute("substitutionGroup");
                if (targetSubstitutionGroup.equals(substitutionGroup)) {
                    var name = element.getAttribute("name");
                    var minOccurs = element.getAttribute("minOccurs");
                    var maxOccurs = element.getAttribute("maxOccurs");
                    var type = element.getAttribute("type");

                    var attribute = new Attribute("uro", name, minOccurs, maxOccurs, typeMap.get(type));
                    properties.add(attribute);
                    getGenericApplicationProperties(element, properties, attribute);
                }
            }
        }

        return properties;
    }

    private void getGenericApplicationProperties(Element element, List<Attribute> properties, Attribute parent) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                var name = childElement.getAttribute("name");
                var minOccurs = childElement.getAttribute("minOccurs");
                var maxOccurs = childElement.getAttribute("maxOccurs");
                var type = childElement.getAttribute("type");

                var attribute = new Attribute("uro", name, minOccurs, maxOccurs, typeMap.get(type));
                properties.add(attribute);
                if (parent != null) {
                    parent.addChild(attribute);
                }
                getGenericApplicationProperties(childElement, properties, attribute);
            }
        }
    }

    public List<Attribute> getUrfAttributes(String featureType) {
        var targetType = String.format("%sType", featureType);
        Node rootNode = xsdDocument.getDocumentElement();
        NodeList nodeList = rootNode.getChildNodes();

        var properties = new ArrayList<Attribute>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                var type = element.getAttribute("type");
                if (type.equals(targetType)) {
                    getUrfAttributes(element, properties, null);
                }
            }
        }

        return properties;
    }

    private void getUrfAttributes(Element element, List<Attribute> properties, Attribute parent) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                var name = childElement.getAttribute("name");
                var minOccurs = childElement.getAttribute("minOccurs");
                var maxOccurs = childElement.getAttribute("maxOccurs");
                var type = childElement.getAttribute("type");

                if (ignoreTypes.contains(type)) {
                    continue;
                }

                var attribute = new Attribute("urf", name, minOccurs, maxOccurs, typeMap.get(type));
                properties.add(attribute);
                if (parent != null) {
                    parent.addChild(attribute);
                }
                getUrfAttributes(childElement, properties, attribute);
            }
        }
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
