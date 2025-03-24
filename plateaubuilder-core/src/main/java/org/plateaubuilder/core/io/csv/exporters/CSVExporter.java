package org.plateaubuilder.core.io.csv.exporters;

import com.opencsv.CSVWriter;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeDataCollection;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.wrapper.RootAttributeHandler;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.attribute.AttributeTreeBuilder;
import org.plateaubuilder.core.world.World;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CSVExporter {
    
    public static final String CHARSET_SHIFT_JIS = "Shift_JIS";
    public static final String GML_ID = "gml:id";
    public static final String SEPARATOR = "_";
    public static final String ROOT_NAME = "root";
    public static final String UOM = "uom";
    public static final String CODE_SPACE = "codeSpace";
    
    private boolean exportAll;
    
    public CSVExporter(boolean exportAll) {
        this.exportAll = exportAll;
    }
    
    /**
     * CSVファイルをエクスポートする
     *
     * @param fileUrl CSVファイルのURL
     */
    public void export(String fileUrl) {
        // 全モデルの属性をエクスポートする場合
        List<Node> cityModelViews = World.getActiveInstance().getCityModelGroup().getChildren();
        List<IFeatureView> featuresExport;
        if (exportAll) {
            featuresExport = new ArrayList<>();
            for (Node cityModelView : cityModelViews) {
                CityModelView city = (CityModelView) cityModelView;
                featuresExport.addAll(city.getFeatureViews());
            }
        } else {
            // 選択中のモデルの属性をエクスポートする場合
            featuresExport = new ArrayList<>(Editor.getFeatureSellection().getSelectedFeatures());
        }
        
        // 中間データ
        AttributeDataCollection attributeDataCollection = new AttributeDataCollection();
        if (!featuresExport.isEmpty()) {
            for (IFeatureView feature : featuresExport) {
                // Tree構造を作成
                var root = new TreeItem<>(
                        new AttributeItem(new RootAttributeHandler(feature)));
                AttributeTreeBuilder.attributeToTree(feature, root);
                // 対象地物のTree構造を中間データに追加
                attributeDataCollection.add(feature.getId(), root);
            }
            
            Pair<Set<String>, List<Map<String, String>>> csv = buildCsv(attributeDataCollection.getAllData());
            Set<String> headers = csv.getFirst();
            List<Map<String, String>> bodyCsv = csv.getSecond();
            List<List<String>> rows = new LinkedList<>();
            for (Map<String, String> mapRow : bodyCsv) {
                List<String> row = new LinkedList<>();
                for (String header : headers) {
                    String value = mapRow.get(header) == null ? StringUtils.EMPTY : mapRow.get(header);
                    row.add(value);
                }
                rows.add(row);
            }
            try (
                    FileOutputStream fos = new FileOutputStream(fileUrl);
                    Writer writer = new OutputStreamWriter(fos, CHARSET_SHIFT_JIS);
                    CSVWriter csvWriter = new CSVWriter(writer)
            ) {
                csvWriter.writeNext(headers.toArray(new String[0]));
                for (List<String> row : rows) {
                    csvWriter.writeNext(row.toArray(new String[0]));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Convert the attributeDataCollection object into the header and body of a CSV file.
     *
     * @param attributeDataCollection
     * @return Return the header and body of the CSV
     */
    private Pair<Set<String>, List<Map<String, String>>> buildCsv(Map<String, TreeItem<AttributeItem>> attributeDataCollection) {
        Set<String> headers = new LinkedHashSet<>();
        headers.add(GML_ID);
        
        Map<String, Integer> duplicateAttributes = new HashMap<>();
        // Identify the names of the duplicated attributes.
        for (Map.Entry<String, TreeItem<AttributeItem>> building : attributeDataCollection.entrySet()) {
            buildMapDuplicateAttribute(building.getValue(), duplicateAttributes, null);
            duplicateAttributes.entrySet().removeIf(e -> e.getValue() == 0);
        }
        
        List<Map<String, String>> bodyCsv = new LinkedList<>();
        // Iterate through each building
        for (Map.Entry<String, TreeItem<AttributeItem>> building : attributeDataCollection.entrySet()) {
            Map<String, String> rowCSV = new LinkedHashMap<>();
            rowCSV.put(GML_ID, building.getKey());
            buildCsvForBuilding(building.getValue(), headers, rowCSV, duplicateAttributes, null);
            bodyCsv.add(rowCSV);
            duplicateAttributes.entrySet().removeIf(e -> e.getValue() == 0);
        }
        Set<String> headerAfter = new LinkedHashSet<>();
        for (String header : headers) {
            if (headers.stream().noneMatch(e -> e.startsWith(header + SEPARATOR))
                    || headers.stream().anyMatch(e -> e.equals(header + SEPARATOR + UOM))
                    || headers.stream().anyMatch(e -> e.equals(header + SEPARATOR + CODE_SPACE)
            )) {
                headerAfter.add(header);
            }
        }
        return new Pair<>(headerAfter, bodyCsv);
    }
    
    /**
     * Go through all the attributeItems to check for duplicate attributes.
     *
     * @param attributeItem       : building or child attributes.
     * @param duplicateAttributes : map duplicate attribute
     * @param attributeName       : attribute name
     */
    private void buildMapDuplicateAttribute(TreeItem<AttributeItem> attributeItem,
                                            Map<String, Integer> duplicateAttributes,
                                            String attributeName) {
        ObservableList<TreeItem<AttributeItem>> childrenItems = attributeItem.getChildren();
        // Create a map containing the names of duplicate attributes.
        for (TreeItem<AttributeItem> item : childrenItems) {
            if (!Objects.equals(item.getValue().getName(), ROOT_NAME)) {
                String nameCheck = attributeName != null ? attributeName + SEPARATOR + item.getValue().getName() : item.getValue().getName();
                // If an attribute is duplicated, set the value to 1.
                if (duplicateAttributes.containsKey(nameCheck)) {
                    duplicateAttributes.put(nameCheck, 1);
                } else {
                    duplicateAttributes.put(nameCheck, 0);
                }
            }
        }
        for (TreeItem<AttributeItem> item : childrenItems) {
            String nameCount = null;
            if (!Objects.equals(item.getValue().getName(), ROOT_NAME)) {
                nameCount = attributeName != null ? attributeName + SEPARATOR + item.getValue().getName() : item.getValue().getName();
                int count = duplicateAttributes.get(nameCount);
                if (count > 0) {
                    duplicateAttributes.put(nameCount, count + 1);
                    nameCount = nameCount + SEPARATOR + count;
                }
            }
            // Retrieve the information of the child item
            buildMapDuplicateAttribute(item, new HashMap<>(), nameCount);
        }
    }
    
    /**
     * Retrieve all child attributes of a building to create the header and body for the CSV.
     *
     * @param attributeItem       : building or child attributes.
     * @param headers             : header of csv
     * @param rowCSV              : data of 1 one row csv
     * @param duplicateAttributes : map duplicate attribute
     * @param attributeName       : attribute name
     */
    private void buildCsvForBuilding(TreeItem<AttributeItem> attributeItem, Set<String> headers,
                                     Map<String, String> rowCSV, Map<String, Integer> duplicateAttributes,
                                     String attributeName) {
        if (attributeItem == null) {
            return;
        }
        // Attributes with the name 'root' will be ignored.
        if (!Objects.equals(attributeItem.getValue().getName(), ROOT_NAME)) {
            String value;
            String uom;
            String codeSpace;
            try {
                value = attributeItem.getValue().getValue();
                uom = attributeItem.getValue().getUom();
                codeSpace = attributeItem.getValue().getCodeSpace();
            } catch (NullPointerException e) {
                value = null;
                uom = null;
                codeSpace = null;
            }
            // add name to headers
            // put value to row csv
            headers.add(attributeName);
            rowCSV.put(attributeName, value);
            if (!StringUtils.isBlank(uom) && !StringUtils.isBlank(value)) {
                headers.add(attributeName + SEPARATOR + UOM );
                rowCSV.put(attributeName + SEPARATOR + UOM, uom);
            }
            if (!StringUtils.isBlank(codeSpace) && !StringUtils.isBlank(value)) {
                Path path = Paths.get(codeSpace);
                String fileNameXml = path.getFileName().toString();
                headers.add(attributeName + SEPARATOR + CODE_SPACE);
                rowCSV.put(attributeName + SEPARATOR + CODE_SPACE, fileNameXml);
            }
            
        }
        ObservableList<TreeItem<AttributeItem>> childrenItems = attributeItem.getChildren();
        // Create a map containing the names of duplicate attributes.
        for (TreeItem<AttributeItem> item : childrenItems) {
            if (!Objects.equals(item.getValue().getName(), ROOT_NAME)) {
                String nameCheck = attributeName != null ? attributeName + SEPARATOR + item.getValue().getName() : item.getValue().getName();
                // If an attribute is duplicated, set the value to 1.
                if (duplicateAttributes.containsKey(nameCheck)) {
                    duplicateAttributes.put(nameCheck, 1);
                } else {
                    duplicateAttributes.put(nameCheck, 0);
                }
            }
        }
        // If there is a hierarchical structure, concatenate the root attribute name and leaf attribute name with an underscore ('_').
        // If the same attribute exists, append ‘_[serial number]’ to the subsequent duplicate items in the attribute name.
        // Note that the serial number starts from 1.
        for (TreeItem<AttributeItem> item : childrenItems) {
            String nameCount = null;
            if (!Objects.equals(item.getValue().getName(), ROOT_NAME)) {
                nameCount = attributeName != null ? attributeName + SEPARATOR + item.getValue().getName() : item.getValue().getName();
                int count = duplicateAttributes.get(nameCount);
                if (count > 0) {
                    duplicateAttributes.put(nameCount, count + 1);
                    nameCount = nameCount + SEPARATOR + count;
                }
            }
            // Retrieve the information of the child item
            buildCsvForBuilding(item, headers, rowCSV, new HashMap<>(), nameCount);
        }
    }
}
