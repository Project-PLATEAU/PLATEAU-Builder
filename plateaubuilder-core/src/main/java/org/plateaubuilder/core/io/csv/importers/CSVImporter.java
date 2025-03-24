package org.plateaubuilder.core.io.csv.importers;

import com.opencsv.CSVReader;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeDataCollection;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.BuildingSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.CityFurnitureSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.LandUseSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.RoadSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.SolitaryVegetationObjectSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.UrbanPlanningAreaSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.WaterBodySchemaManager;
import org.plateaubuilder.core.citymodel.attribute.reader.CodeListReader;
import org.plateaubuilder.core.citymodel.attribute.reader.XSDSchemaDocument;
import org.plateaubuilder.core.citymodel.attribute.wrapper.SimpleAttributeHandler;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.attribute.AttributeImporter;
import org.plateaubuilder.core.world.World;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class CSVImporter {

    public static final String SHIFT_JIS = "Shift_JIS";
    public static final String GML_ID = "gml:id";
    public static final String SEPARATOR = "_";
    public static final String ROOT_NAME = "root";
    public static final String URO = "uro";
    public static final String UOM = "uom";
    public static final String CODE_SPACE = "codeSpace";

    public static final String ERR_001 = "Editor上にGMLがインポートされていません！";
    public static final String ERR_002 = "インポートされたCSVファイルにデータがありません！";
    public static final String ERR_003 = "Gml:id %s 列 %s が正しくありません。";
    public static final String ERR_004 = "列 %s が %d 回重複しています。";
    public static final String ERR_005 = "Gml:id %s 列 %s の数量が許容範囲を超えています。";
    public static final String ERR_006 = "Gml:id %s 列 %s は整数型で、0以上でなければなりません。";
    public static final String ERR_007 = "Gml:id %s 列 %s はDouble型でなければなりません。";
    public static final String ERR_008 = "Gml:id %s 列 %s はyyyy形式でなければなりません。";
    public static final String ERR_009 = "Gml:id %s 列 %s はyyyy-MM-dd形式でなければなりません。";
    public static final String ERR_010 = "Gml:id %s 列 %s は整数型でなければなりません。";
    public static final String ERR_011 = "Gml:id %s 列 %s は次の値のいずれかでなければなりません: true, false, 0, 1。";
    public static final String ERR_012 = "Gml:id %s は存在しません。";
    public static final String ERR_013 = "Gml:id %s 列 %s の値 uom が正しくありません。";
    public static final String ERR_014 = "Gml:id %s の列 %s のファイル %s はフォルダ codelists に存在しません。";
    public static final String ERR_015 = "Gml_id %s の列 %s の値は、ファイル %s の codeList %s のリスト内に存在する必要があります。";

    private List<String[]> allData;
    private Map<String, List<String>> cacheCodeSpace = new HashMap<>();

    private XSDSchemaDocument xSchemaDocument = Editor.getUroSchemaDocument();

    /**
     * CSVファイルをインポートする
     *
     */
    public void importCsv() {

        // Remove the row header and use it as the key of the map.
        List<Map<String, String>> csv = convertToMap(allData);
        AttributeDataCollection attributeDataCollection = new AttributeDataCollection();
        // Iterate through each row to create the AttributeDataCollection.
        for (Map<String, String> row : csv) {
            TreeItem<AttributeItem> data = buildTreeAttribute(row);
            attributeDataCollection.add(row.get(GML_ID), data);
        }
        // TODO : All the necessary data is currently stored in the
        // attributeDataCollection variable.
        AttributeImporter attributeImporter = new AttributeImporter();
        attributeImporter.importAttributeDataCollection(attributeDataCollection);
    }

    /**
     * Validate the contents of the CSV file
     *
     * @param fileUrl            CSV file path.
     * @param errorCallback      Callback to display a message
     * @param completionCallback Callback to notify when completed.
     */
    public void validateCsv(String fileUrl, Consumer<String> errorCallback, Runnable completionCallback) {
        new Thread(() -> {
            validateCsv(fileUrl, errorCallback);
            completionCallback.run();
        }).start();
    }

    /**
     * Validate the contents of the CSV file
     *
     * @param fileUrl       CSV file path.
     * @param errorCallback Callback to display a message
     */
    private void validateCsv(String fileUrl, Consumer<String> errorCallback) {
        // Check if the GML file has been imported.
        if (World.getActiveInstance().getCityModelGroup() == null) {
            errorCallback.accept(ERR_001);
            return;
        }
        Charset CHARSET_SHIFT_JIS = Charset.forName(SHIFT_JIS);
        try (
                FileInputStream fos = new FileInputStream(fileUrl);
                Reader reader = new InputStreamReader(fos, CHARSET_SHIFT_JIS);
                CSVReader csvReader = new CSVReader(reader);) {
            allData = csvReader.readAll();
            if (allData.isEmpty()) {
                errorCallback.accept(ERR_002);
                return;
            }
            // Check duplicate header
            checkDuplicateHeader(allData.get(0), errorCallback);
            // Remove the row header and use it as the key of the map.
            List<Map<String, String>> csv = convertToMap(allData);
            List<Node> cityModelViews = World.getActiveInstance().getCityModelGroup().getChildren();
            List<Pair<String, List<IFeatureView>>> listPairGmlPath = new LinkedList<>();
            for (Node cityModelView : cityModelViews) {
                String gmlPath = ((CityModelView) cityModelView).getGmlPath();
                CityModelView city = (CityModelView) cityModelView;
                listPairGmlPath.add(new Pair<>(gmlPath, new LinkedList<>(city.getFeatureViews())));
            }
            CodeListReader codeListReader = new CodeListReader();
            // validate gml_id
            for (Map<String, String> row : csv) {
                IFeatureView featureView = null;
                String datasetPath = "";
                for (Pair<String, List<IFeatureView>> pairGmlPath : listPairGmlPath) {
                    List<IFeatureView> featuresImport = pairGmlPath.getSecond();
                    datasetPath = convertDatasetPath(pairGmlPath.getFirst());
                    featureView = featuresImport.stream().filter(e -> Objects.equals(e.getId(), row.get(GML_ID)))
                            .findFirst().orElse(null);
                    if (featureView != null)
                        break;
                }
                if (featureView == null) {
                    String messageError = String.format(ERR_012, row.get(GML_ID));
                    errorCallback.accept(messageError);
                } else {
                    AttributeSchemaManager schemaManager = attributeSchemaManager(featureView);
                    // validate codeType and min max.
                    Map<String, Integer> countHeader = new LinkedHashMap<>();
                    for (Map.Entry<String, String> cell : row.entrySet()) {
                        String columnName = cell.getKey();
                        String value = cell.getValue();
                        // validate existed the file codeSpace
                        if (columnName.endsWith(SEPARATOR + CODE_SPACE)) {
                            if (!StringUtils.isBlank(value)) {
                                List<String> codeListValue = getCodeListValue(value, datasetPath, codeListReader);
                                if (codeListValue.isEmpty()) {
                                    String messageError = String.format(ERR_014,
                                            row.get(GML_ID), columnName, value);
                                    errorCallback.accept(messageError);
                                }
                            }
                            continue;
                        }
                        // Check whether the value of codeSpace exists in the codelist XML file or not.
                        if (row.containsKey(columnName + SEPARATOR + CODE_SPACE) && !StringUtils.isEmpty(value)) {
                            String codeSpace = row.get(columnName + SEPARATOR + CODE_SPACE);
                            List<String> codeListValue = getCodeListValue(codeSpace, datasetPath, codeListReader);
                            if (!codeListValue.isEmpty() && !codeListValue.contains(value)) {
                                String codeList = String.join(",", codeListValue);
                                String messageError = String.format(ERR_015,
                                        row.get(GML_ID), columnName, codeSpace, codeList);
                                errorCallback.accept(messageError);
                            }
                        }

                        // ignore attribute uro
                        if (columnName.contains(URO) && !columnName.endsWith(SEPARATOR + UOM)) {
                            List<String> names = splitHeaderIgnoreNumber(columnName);
                            if (!xSchemaDocument.validateAttributeHierarchy(names)) {
                                String messageError = String.format(ERR_003,
                                        row.get(GML_ID), columnName);
                                errorCallback.accept(messageError);
                            }
                            continue;
                        }
                        // ignore attribute uro
                        if (columnName.contains(URO) || Objects.equals(GML_ID, columnName) ||
                                (StringUtils.isEmpty(value) && !columnName.endsWith(SEPARATOR + UOM))) {
                            continue;
                        }
                        if (columnName.endsWith(SEPARATOR + UOM)) {
                            String name = columnName.replaceFirst("_uom$", "");
                            if (!StringUtils.isEmpty(row.get(name))) {
                                validateUOM(columnName, value, row.get(GML_ID), schemaManager, errorCallback);
                            }
                            continue;
                        }

                        String min;
                        String max;
                        String type;
                        String colNameForValidation = columnName;
                        try {
                            if (columnName.contains(SEPARATOR)) {
                                List<String> names = splitHeaderIgnoreNumber(columnName);
                                if (names.size() == 1) { // Duplicate column names should have a _number suffix added
                                    colNameForValidation = convertNameToKey(names.get(0));
                                    min = schemaManager.getAttributeMin(colNameForValidation);
                                    max = schemaManager.getAttributeMax(colNameForValidation);
                                    type = schemaManager.getAttributeType(colNameForValidation);
                                    validateMinMax(min, max, countHeader, row.get(GML_ID), names.get(0),
                                            columnName, errorCallback);
                                } else if (names.size() == 2) { // Column names with two levels
                                    colNameForValidation = names.get(0) + SEPARATOR + names.get(1);
                                    String parentName = convertNameToKey(names.get(0));
                                    String childName = convertNameToKey(names.get(1));
                                    min = schemaManager.getChildAttributeMin(parentName, childName);
                                    max = schemaManager.getChildAttributeMax(parentName, childName);
                                    type = schemaManager.getChildAttributeType(parentName, childName);
                                    validateMinMax(min, max, countHeader, row.get(GML_ID), colNameForValidation,
                                            columnName, errorCallback);
                                } else {
                                    String messageError = String.format(ERR_003,
                                            row.get(GML_ID), columnName);
                                    errorCallback.accept(messageError);
                                    continue;
                                }
                            } else {
                                min = schemaManager.getAttributeMin(convertNameToKey(colNameForValidation));
                                max = schemaManager.getAttributeMax(convertNameToKey(colNameForValidation));
                                type = schemaManager.getAttributeType(convertNameToKey(colNameForValidation));
                                validateMinMax(min, max, countHeader, row.get(GML_ID),
                                        colNameForValidation, columnName, errorCallback);
                            }
                        } catch (RuntimeException e) {
                            String messageError = String.format(ERR_003,
                                    row.get(GML_ID), columnName);
                            errorCallback.accept(messageError);
                            continue;
                        }
                        validateType(type, value, row.get(GML_ID), columnName, errorCallback);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * retrieve the codelist value from the .xml file.
     * It will be cached so that it doesn't need to be read multiple times.
     *
     * @param codeSpace      file name xml
     * @param datasetPath    path
     * @param codeListReader codeListReader
     * @return List
     */
    private List<String> getCodeListValue(String codeSpace, String datasetPath, CodeListReader codeListReader) {

        if (!cacheCodeSpace.containsKey(datasetPath + SEPARATOR + codeSpace)) {
            List<String> codeListValue;
            try {
                codeListValue = codeListReader.getCodeListValue(codeSpace, datasetPath);
            } catch (NullPointerException exception) {
                codeListValue = new ArrayList<>();
            }
            cacheCodeSpace.put(datasetPath + SEPARATOR + codeSpace, codeListValue);
        }
        return cacheCodeSpace.get(datasetPath + SEPARATOR + codeSpace);
    }

    /**
     * convert the gml file path to the base folder path.
     *
     * @param gmlPath the gml file path
     * @return
     */
    private String convertDatasetPath(String gmlPath) {
        var datasetPath = Paths.get(gmlPath);
        while (datasetPath != null && !datasetPath.getFileName().toString().equals("udx")) {
            datasetPath = datasetPath.getParent();
        }
        if (datasetPath != null) {
            return datasetPath.getParent().toString();
        }
        return null;
    }

    /**
     * _uom で終わる名前の列を検証します。
     *
     * @param columnNameUom columnNameUom
     * @param value         value
     * @param gmlID         gmlID
     * @param schemaManager schemaManager
     * @param errorCallback errorCallback
     */
    private void validateUOM(String columnNameUom, String value, String gmlID,
            AttributeSchemaManager schemaManager, Consumer<String> errorCallback) {
        // remove _uom.
        String name = columnNameUom.replaceFirst("_uom$", "");

        String attributeKey = convertNameToKey(name);
        if (name.contains(SEPARATOR)) {
            List<String> names = splitHeaderIgnoreNumber(name);
            if (names.size() == 1) { // Duplicate column names should have a _number suffix added
                attributeKey = convertNameToKey(names.get(0));
            }
        }
        String uom = schemaManager.getAttributeUom(attributeKey);
        // if the uom value in the CSV differs from the uom in the JSON, report an
        // error.
        if (uom == null) {
            String messageError = String.format(ERR_003, gmlID, columnNameUom);
            errorCallback.accept(messageError);
        } else if (!Objects.equals(uom, value)) {
            String messageError = String.format(ERR_013, gmlID, columnNameUom);
            errorCallback.accept(messageError);
        }
    }

    /**
     * Convert the attribute name into the attribute key
     * Ex: bldg:name -> name
     *
     * @param colNameForValidation
     * @return
     */
    private String convertNameToKey(String colNameForValidation) {
        if (colNameForValidation.contains(":")) {
            String[] header = colNameForValidation.split(":");
            if (header.length == 2) {
                return header[1];
            } else {
                return colNameForValidation;
            }
        } else {
            return colNameForValidation;
        }
    }

    /**
     * Check for duplicate headers in the csv file
     *
     * @param headers       headers
     * @param errorCallback errorCallback
     */
    private void checkDuplicateHeader(String[] headers, Consumer<String> errorCallback) {
        HashMap<String, Integer> headerCount = new HashMap<>();
        for (String header : headers) {
            headerCount.put(header, headerCount.getOrDefault(header, 0) + 1);
        }
        for (String header : headerCount.keySet()) {
            if (headerCount.get(header) > 1) {
                errorCallback.accept(String.format(ERR_004, header, headerCount.get(header)));
            }
        }
    }

    /**
     * Validate the occurrence of the attribute based on the min-max range
     *
     * @param min               min
     * @param max               max
     * @param countHeader       countHeader
     * @param gmlId             gmlId
     * @param nameForValidation nameForValidation
     * @param name              name
     * @param errorCallback     errorCallback
     */
    private void validateMinMax(String min, String max, Map<String, Integer> countHeader, String gmlId,
            String nameForValidation,
            String name, Consumer<String> errorCallback) {
        Integer count = countHeader.get(nameForValidation);
        count = count == null ? 0 : count;
        countHeader.put(nameForValidation, count + 1);

        int minInt = Integer.parseInt(min);
        int maxInt = Objects.equals(max, "unbounded") ? Integer.MAX_VALUE : Integer.parseInt(max);
        if (count + 1 < minInt || count + 1 > maxInt) {
            String messageError = String.format(ERR_005, gmlId, name);
            errorCallback.accept(messageError);
        }
    }

    /**
     * Validate the value of the CSV cell according to the type
     *
     * @param type          type
     * @param value         value cell
     * @param gmlId         gmlId
     * @param name          column name
     * @param errorCallback show message error
     */
    private void validateType(String type, String value, String gmlId, String name, Consumer<String> errorCallback) {
        String message = "";
        switch (type) {
            case "xs:nonNegativeInteger":
                if (!checkNonNegativeInteger(value)) {
                    message += String.format(ERR_006, gmlId, name);
                }
                break;
            case "gml:LengthType":
            case "gen:measureAttribute":
            case "gml:MeasureType":
            case "gen:doubleAttribute":
                if (!checkDouble(value)) {
                    message += String.format(ERR_007, gmlId, name);
                }
                break;
            case "xs:gYear":
                if (!checkGYear(value)) {
                    message += String.format(ERR_008, gmlId, name);
                }
                break;
            case "xs:date":
            case "gen:dateAttribute":
                if (!checkDate(value)) {
                    message += String.format(ERR_009, gmlId, name);
                }
                break;
            case "gen:intAttribute":
            case "xs:integer":
                if (!checkInteger(value)) {
                    message += String.format(ERR_010, gmlId, name);
                }
                break;
            case "xs:boolean":
                if (!checkBoolean(value)) {
                    message += String.format(ERR_011, gmlId, name);
                }
                break;
        }
        if (!StringUtils.isEmpty(message)) {
            errorCallback.accept(message);
        }
    }

    /**
     * Check if it is of boolean type
     *
     * @param value value for check
     * @return result
     */
    private boolean checkBoolean(String value) {
        return value.equals("true") || value.equals("false") ||
                value.equals("0") || value.equals("1");
    }

    /**
     * Check if it is not a negative integer
     *
     * @param value value for check
     * @return result
     */
    private Boolean checkNonNegativeInteger(String value) {
        try {
            return Integer.parseInt(value) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if it is a double
     *
     * @param value value for check
     * @return result
     */
    private Boolean checkDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if it is in the format "yyyy"
     *
     * @param value value for check
     * @return result
     */
    private Boolean checkGYear(String value) {
        return value.matches("\\d{4}");
    }

    /**
     * Check if it is a date in the format yyyy-MM-dd.
     *
     * @param value value for check
     * @return result
     */
    private static Boolean checkDate(String value) {
        if (StringUtils.isEmpty(value) || value.length() != 10) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        try {
            format.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Check if it is a integer
     *
     * @param value value for check
     * @return result
     */
    private static Boolean checkInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Convert a CSV row to the TreeItem type
     *
     * @param row
     * @return
     */
    private TreeItem<AttributeItem> buildTreeAttribute(Map<String, String> row) {
        var root = new TreeItem<>(
                new AttributeItem(new SimpleAttributeHandler(ROOT_NAME, null, null, null)));
        Map<String, TreeItem<AttributeItem>> treeItemMap = new HashMap<>();
        // Iterate through each row of the CSV.
        for (Map.Entry<String, String> entry : row.entrySet()) {
            String columnName = entry.getKey();
            String value = entry.getValue();
            String uom = row.get(entry.getKey() + SEPARATOR + UOM);
            String codeSpace = row.get(entry.getKey() + SEPARATOR + CODE_SPACE);
            addTreeItem(root, columnName, value, uom, codeSpace, treeItemMap);
        }
        return root;
    }

    /**
     * add the cells of the CSV row to the root (TreeItem).
     *
     * @param root        root
     * @param columnName  columnName
     * @param value       value of cell
     * @param treeItemMap A map containing each child TreeItem
     */
    private void addTreeItem(TreeItem<AttributeItem> root, String columnName, String value, String uom,
            String codeSpace, Map<String, TreeItem<AttributeItem>> treeItemMap) {
        // If the column is gml_id or the value is null, a TreeItem will not be created.
        if (columnName.equals(GML_ID) || StringUtils.isEmpty(value) || columnName.endsWith(SEPARATOR + UOM)) {
            return;
        }
        // Split the header into individual elements based on the underscore (_)
        // character
        List<String> columnParts = splitHeader(columnName);
        TreeItem<AttributeItem> currentParent = root;
        StringBuilder fullColumnName = new StringBuilder();
        // Iterate through each element in the header that was just split.
        for (String columnPart : columnParts) {
            if (fullColumnName.length() > 0) {
                fullColumnName.append(SEPARATOR);
            }
            fullColumnName.append(columnPart);
            String currentColName = fullColumnName.toString();
            // Check if the TreeItem has been created with the current header element.
            if (!treeItemMap.containsKey(currentColName)) {
                TreeItem<AttributeItem> newItem;
                // In the header, there will be _number values to differentiate duplicate
                // attributes.
                // However, when creating the TreeItem, this _number value needs to be removed
                String name = columnPart.replaceAll("_\\d+$", "");
                // If it is the last part of the header, TreeItem will be set to a value.
                if (currentColName.equals(columnName)) {
                    newItem = new TreeItem<>(
                            new AttributeItem(new SimpleAttributeHandler(name, value, uom, codeSpace)));
                } else { // If it is not the last part of the header, a parent TreeItem will be created
                    newItem = new TreeItem<>(
                            new AttributeItem(new SimpleAttributeHandler(name, null, null, null)));
                }
                // All created TreeItem objects are stored with their corresponding headers
                treeItemMap.put(currentColName, newItem);
                // Set the child TreeItem objects.
                currentParent.getChildren().add(newItem);
            }
            currentParent = treeItemMap.get(currentColName);
        }
    }

    /**
     * Split the header into smaller parts based on underscores (_) and numbers.
     * Ex: A_B_1_C_D_12 -> [A, B_1, C, D_12]
     *
     * @param input
     * @return
     */
    private List<String> splitHeader(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        String[] parts = input.split(SEPARATOR);
        for (int i = 0; i < parts.length; i++) {
            currentPart.append(parts[i]);
            if (i == parts.length - 1 || !parts[i + 1].matches("\\d+")) {
                result.add(currentPart.toString());
                currentPart.setLength(0);
            } else {
                currentPart.append(SEPARATOR);
            }
        }
        return result;
    }

    /**
     * Split the header into multiple elements separated by underscores (_), but
     * exclude the elements that are numbers
     * Ex: A_1_B -> [A,B] , A_1 -> [A]
     *
     * @param header header
     * @return result
     */
    private List<String> splitHeaderIgnoreNumber(String header) {
        String[] parts = header.split(SEPARATOR);
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.matches("\\d+")) {
                result.add(part);
            }
        }
        return result;
    }

    /**
     * Use the header as the key for each row
     *
     * @param allData
     * @return
     */
    private List<Map<String, String>> convertToMap(List<String[]> allData) {
        List<Map<String, String>> csv = new LinkedList<>();
        // get header
        String[] headers = allData.get(0);
        // Use the header as the key for each row.
        for (int i = 1; i < allData.size(); i++) {
            Map<String, String> row = setHeaderForRow(headers, allData.get(i));
            csv.add(row);
        }
        return csv;
    }

    /**
     * Use the header as the key for a row.
     *
     * @param headers
     * @param strings
     * @return
     */
    private Map<String, String> setHeaderForRow(String[] headers, String[] strings) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String value = strings[i];
            row.put(headers[i], value);
        }
        return row;
    }

    /**
     * Get the type of AttributeSchemaManager based on IFeatureView.
     *
     * @param selectedFeature
     * @return
     */
    public AttributeSchemaManager attributeSchemaManager(IFeatureView selectedFeature) {
        AbstractCityObject feature = selectedFeature.getGML();
        if (feature == null) {
            return null;
        }
        if (feature instanceof AbstractBuilding) {
            return new BuildingSchemaManager();
        } else if (feature instanceof Road) {
            return new RoadSchemaManager();
        } else if (feature instanceof LandUse) {
            return new LandUseSchemaManager();
        } else if (feature instanceof WaterBody) {
            return new WaterBodySchemaManager();
        } else if (feature instanceof SolitaryVegetationObject) {
            return new SolitaryVegetationObjectSchemaManager();
        } else if (feature instanceof CityFurniture) {
            return new CityFurnitureSchemaManager();
        } else if (feature instanceof ADEGenericComponent) {
            return new UrbanPlanningAreaSchemaManager();
        }
        return null;
    }
}
