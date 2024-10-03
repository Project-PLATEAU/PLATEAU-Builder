package org.plateaubuilder.core.basemap;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.util.Pair;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.geospatial.GeoCoordinate;
import org.plateaubuilder.core.utils3d.geom.Vec3f;
import org.plateaubuilder.core.world.World;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.math3.util.FastMath.asinh;

public class XyzTile {
    
    private boolean isBaseMapModeInternal = false;
    
    private String tileServerUrl;
    private double baseMapPositionZ;
    private int maxZoomLevelAvailable;
    
    private List<Pair<Canvas, List<XyzTileModel>>> pairCanvasXyzTileModels;
    
    private Map<Integer, XyzTileModel> mapSizeBase = new HashMap<>();
    private List<Task> tasksForCameraMove = new ArrayList<>();
    private List<Task> tasksForLoadAll = new ArrayList<>();
    private static final int MAX_ZOOM_LEVEL_DEFAULT = 18;
    private static final int ZOOM_LEVEL_UNVAILABLE = -1;
    private static final int MIN_ZOOM_LEVEL = 1;
    private static final double THRESHOLD = 200000;
    private static final int IMAGE_COUNT_DEFAULT = 100;
    
    public XyzTile() {
        tileServerUrl = "https://cyberjapandata.gsi.go.jp/xyz/seamlessphoto/{z}/{x}/{y}.jpg";
        pairCanvasXyzTileModels = new ArrayList<>();
        baseMapPositionZ = 0;
        maxZoomLevelAvailable = MAX_ZOOM_LEVEL_DEFAULT;
    }
    
    /**
     * ベースマップを取得する
     *
     * @return ベースマップのリスト
     */
    public List<Canvas> getBaseMaps() {
        return pairCanvasXyzTileModels.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * すべてのベースマップをクリアする
     */
    public void clearBaseMap() {
        cancelAllTask();
        mapSizeBase = new HashMap<>();
        List<Canvas> baseMaps = pairCanvasXyzTileModels.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
        World.getRoot3D().getChildren().removeAll(baseMaps);
        this.pairCanvasXyzTileModels = new ArrayList<>();
    }
    
    /**
     * ベースマップの表示/非表示
     *
     * @param value 表示/非表示フラグ
     */
    public void toggleBaseMapMode(boolean value) {
        isBaseMapModeInternal = value;
        updateBasemapVisibility();
        
    }
    
    public void updateBaseMapPositionZ(int newZPosition) {
        baseMapPositionZ = newZPosition;
        List<Canvas> baseMaps = getBaseMaps();
        if (areBaseMapsInWorld(baseMaps)) {
            World.getRoot3D().getChildren().removeAll(baseMaps);
            adjustBaseMapZPosition(baseMaps, newZPosition);
            World.getRoot3D().getChildren().addAll(baseMaps);
        }
    }
    
    /**
     *タイルのURLを更新してください。
     * @param newUrl
     * @param newType
     */
    public void updateTileServerUrl(String newUrl) {
        tileServerUrl = newUrl;
        ObservableList<Node> cityModelViews =  World.getActiveInstance().getCityModelGroup().getChildren();
        clearBaseMap();
        for(Node cityModelView : cityModelViews){
            Editor.getXyzTile().loadAllBasemapImages(((CityModelView)cityModelView).getGML().getBoundedBy().getEnvelope());
        }
        updateBasemapVisibility();
    }

    public String getTileServerUrl() {
        return this.tileServerUrl;
    }
    
    /**
     * ベースマップを更新する
     */
    public void updateBasemapVisibility() {
        List<Canvas> baseMaps = pairCanvasXyzTileModels.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
        if (isBaseMapModeInternal) {
            World.getRoot3D().getChildren().addAll(baseMaps);
        } else {
            World.getRoot3D().getChildren().removeAll(baseMaps);
        }
    }
    
    /**
     * Load all images onto the screen based on the corner coordinates of the GML file.
     *
     * @param envelope contains the coordinates of the two corners
     */
    public void loadAllBasemapImages(Envelope envelope) {
        mapSizeBase = new HashMap<>();
        // get the coordinates of the two corners of the map.
        GeoCoordinate upper = new GeoCoordinate(envelope.getUpperCorner().getValue());
        GeoCoordinate lower = new GeoCoordinate(envelope.getLowerCorner().getValue());
        // determine the position and size of the Canvas
        Canvas canvasBasemap = buildCanvasBasemap(upper, lower);
        // Calculate a reasonable zoom level to display the entire map without exceeding IMAGE_COUNT_DEFAULT (100 images)
        int zoomLevel = calZoomLevel(upper, lower, canvasBasemap);
        // Terminate the process if no available zoom level exists
        if(zoomLevel == ZOOM_LEVEL_UNVAILABLE){
            return;
        }
        // Create all XyzTileModels with the properties of size, position, child XyzTileModel, etc.
        // The created XyzTileModels will have a zoom level equal to the zoom level calculated above.
        List<XyzTileModel> xyzTileModels = createXyzTileModelList(canvasBasemap, upper, lower, zoomLevel);
        List<XyzTileModel> loadedXyzTileModels = new ArrayList<>();
        // If the zoom level is smaller than the maximum zoom level,
        // the zoom will be calculated based on the distance to the camera.
        if (zoomLevel < maxZoomLevelAvailable) {
            // Get the current position of the camera.
            PerspectiveCamera camera = Editor.getCamera().getCamera();
            var cameraPosition = camera.getLocalToSceneTransform();
            // From the camera's position and the list of images,
            // it will be possible to determine which images need to be loaded and which do not.
            distributeZoomLevels(xyzTileModels, cameraPosition, canvasBasemap);
            // Collect the images that need to be loaded on the screen.
            loadedXyzTileModels = flattenLoadedXyzTiles(xyzTileModels);
            // Sort in descending order by zoom level to load the images from the highest zoom level to the lowest.
            loadedXyzTileModels.sort(Comparator.comparing(XyzTileModel::getZ).reversed());
        } else {
            // If it is the highest zoom level, all images will be displayed regardless of the camera's position.
            loadedXyzTileModels = xyzTileModels;
        }
        Pair<Canvas, List<XyzTileModel>> pairCanvasXyzTileModel = new Pair<>(canvasBasemap, xyzTileModels);
        GraphicsContext gc = canvasBasemap.getGraphicsContext2D();
        // From the number of images, calculate the number of tasks for running multithreading.
        int taskCount = determineTaskCount(loadedXyzTileModels.size());
        Map<Integer, LinkedList<XyzTileModel>> mapTask = new HashMap<>();
        // Evenly distribute the number of images to be loaded across tasks.
        // Ensure that images with a higher zoom level are loaded first.
        for (int i = 0; i < taskCount; i++) {
            LinkedList<XyzTileModel> listTask = new LinkedList<>();
            for (int j = i; j < loadedXyzTileModels.size(); j += taskCount) {
                listTask.add(loadedXyzTileModels.get(j));
            }
            mapTask.put(i, listTask);
        }
        // run the tasks
        for (Map.Entry<Integer, LinkedList<XyzTileModel>> entry : mapTask.entrySet()) {
            Task<Void> imageLoadingTask = buildTask(entry, gc, tileServerUrl);
            Thread thread = new Thread(imageLoadingTask);
            tasksForLoadAll.add(imageLoadingTask);
            thread.start();
        }
        
        pairCanvasXyzTileModels.add(pairCanvasXyzTileModel);
    }
    
    /**
     * Reload the map images when there is a change in the camera's position.
     */
    public void loadImagesAfterCameraMove() {
        cancelAllTaskCameraMove();
        // Get the current position of the camera.
        PerspectiveCamera camera = Editor.getCamera().getCamera();
        var cameraPosition = camera.getLocalToSceneTransform();
        // Traverse the list of images (XyzTileModel) from each imported GML file.
        for (Pair<Canvas, List<XyzTileModel>> pairCanvasXyzTileModel : pairCanvasXyzTileModels) {
            List<XyzTileModel> xyzTileModels = pairCanvasXyzTileModel.getValue();
            Canvas canvasBasemap = pairCanvasXyzTileModel.getKey();
            // From the camera's position and the list of images,
            // it will be possible to determine which images need to be loaded and which do not.
            distributeZoomLevels(xyzTileModels, cameraPosition, canvasBasemap);
            // Collect the images that need to be loaded on the screen.
            List<XyzTileModel> xyzTileModelsToLoad = flattenLoadedXyzTiles(xyzTileModels);
            // Sort in descending order by zoom level to load the images from the highest zoom level to the lowest.
            xyzTileModelsToLoad.sort(Comparator.comparing(XyzTileModel::getZ).reversed());
            if (!xyzTileModelsToLoad.isEmpty()) {
                // From the number of images, calculate the number of tasks for running multithreading.
                int taskCount = determineTaskCount(xyzTileModelsToLoad.size());
                Map<Integer, LinkedList<XyzTileModel>> mapTask = new HashMap<>();
                // Evenly distribute the number of images to be loaded across tasks.
                // Ensure that images with a higher zoom level are loaded first.
                for (int i = 0; i < taskCount; i++) {
                    LinkedList<XyzTileModel> listTask = new LinkedList<>();
                    for (int j = i; j < xyzTileModelsToLoad.size(); j += taskCount) {
                        listTask.add(xyzTileModelsToLoad.get(j));
                    }
                    mapTask.put(i, listTask);
                }
                // run the tasks
                for (Map.Entry<Integer, LinkedList<XyzTileModel>> entry : mapTask.entrySet()) {
                    Task<Void> imageLoadingTask = buildTask(entry, canvasBasemap.getGraphicsContext2D(), tileServerUrl);
                    Thread thread = new Thread(imageLoadingTask);
                    tasksForCameraMove.add(imageLoadingTask);
                    thread.start();
                }
            }
        }
    }
    
    /**
     * Cancel all running tasks
     */
    private void cancelAllTask() {
        for (Task task : tasksForLoadAll) {
            task.cancel();
        }
        tasksForLoadAll.clear();
        cancelAllTaskCameraMove();
    }
    
    /**
     * Cancel the tasks responsible for loading images when moving the camera.
     */
    private void cancelAllTaskCameraMove() {
        for (Task task : tasksForCameraMove) {
            task.cancel();
        }
        tasksForCameraMove.clear();
    }
    
    /**
     * ベースマップがワールド内にあるか確認してください。
     * @param baseMaps
     * @return
     */
    private boolean areBaseMapsInWorld(List<Canvas> baseMaps) {
        Set<Node> worldChildren = new HashSet<>(World.getRoot3D().getChildren());
        return worldChildren.containsAll(baseMaps);
    }
    
    /**
     *ベースマップのZ座標を調整してください。
     * @param baseMaps
     * @param newZPosition
     */
    private void adjustBaseMapZPosition(List<Canvas> baseMaps, int newZPosition) {
        for (Canvas canvas : baseMaps) {
            List<Translate> zTranslations = getZTranslations(canvas);
            if (zTranslations.isEmpty()) {
                addNewZTranslation(canvas, newZPosition);
            } else {
                updateExistingZTranslations(canvas, zTranslations, newZPosition);
            }
        }
    }
    
    /**
     *ベースマップのTranslateを取得してください。
     * @param canvas
     * @return
     */
    private List<Translate> getZTranslations(Canvas canvas) {
        return canvas.getTransforms().stream()
                .filter(transform -> transform instanceof Translate)
                .map(transform -> (Translate) transform)
                .collect(Collectors.toList());
    }
    
    /**
     * ベースマップに新しいTranslateを追加してください。
     * @param canvas
     * @param zPosition
     */
    private void addNewZTranslation(Canvas canvas, int zPosition) {
        Translate newTranslate = new Translate();
        newTranslate.setZ(zPosition);
        canvas.getTransforms().add(newTranslate);
    }
    
    /**
     * ベースマップのTranslateを更新してください。
     * @param canvas
     * @param zTranslations
     * @param newZPosition
     */
    private void updateExistingZTranslations(Canvas canvas, List<Translate> zTranslations, int newZPosition) {
        canvas.getTransforms().removeAll(zTranslations);
        zTranslations.forEach(translate -> translate.setZ(newZPosition));
        canvas.getTransforms().addAll(zTranslations);
    }
    
    /**
     * Calculate the zoom level of the image(XyzTileModel) based on the distance from the camera.
     *
     * @param tileModels
     * @param cameraPosition
     * @param canvas
     */
    private void distributeZoomLevels(List<XyzTileModel> tileModels, Transform cameraPosition, Canvas canvas) {
        if (tileModels != null && !tileModels.isEmpty()) {
            // Iterate through each XyzTileModel with the smallest zoom level.
            for (XyzTileModel model : tileModels) {
                // Use recursion to iterate through the child XyzTileModel with increasing zoom levels
                // Ex: 1 XyzTileModel with a zoom level of 14 will have childNodes consisting of 4 XyzTileModels with a zoom level of 15.
                loadXyzTileModelsRecursively(model.getChildNodes(), model, cameraPosition, model.getZ());
            }
        }
    }
    
    /**
     * Determine which images will be displayed on the screen based on their distance from the camera.
     * Calculate the distance of the child node with the highest zoom level first,
     * then move to the parent node with decreasing zoom levels.
     *
     * @param childNodes
     * @param model
     * @param cameraPosition
     * @param minZoom
     */
    private void loadXyzTileModelsRecursively(List<XyzTileModel> childNodes, XyzTileModel model, Transform cameraPosition, int minZoom) {
        // If an XyzTileModel has no childNodes, it means that it is the image with the highest zoom level.
        if (childNodes == null || childNodes.isEmpty()) {
            // Calculate the distance from the image to the camera
            double distance = calculateDistanceMinCamera(cameraPosition, model);
            // Based on the distance to the camera, the appropriate zoom level for the image can be determined
            int zoomLevel = calculateZoomLevelByDistance(distance);
            // If the calculated zoom level matches the current zoom level of the image, load this image.
            if (zoomLevel == model.getZ()) {
                // Mark this image as needLoading.
                model.setNeedLoading(true);
            } else {
                // Mark the image as not needLoading.
                model.setNeedLoading(false);
            }
            return;
        }
        // Iterate through each child XyzTileModel and continue the recursion.
        for (XyzTileModel child : childNodes) {
            loadXyzTileModelsRecursively(child.getChildNodes(), child, cameraPosition, minZoom);
        }
        // After iterating through all 4 child images and finding that none need to be loaded on the screen,
        // the distance from the parent image to the camera will be considered to determine the appropriate zoom level.
        if ((childNodes.stream().allMatch(e -> !e.isNeedLoading()) &&
                childNodes.stream().allMatch(e -> !e.isIgnore())
        )) {
            // Calculate the distance from the image to the camera
            double distance = calculateDistanceMinCamera(cameraPosition, model);
            // Based on the distance to the camera, the appropriate zoom level for the image can be determined
            int zoomLevel = calculateZoomLevelByDistance(distance);
            // If the calculated zoom level matches the current zoom level of the image, load this image.
            // or if the image has the smallest zoom level, it will also need to be loaded on the screen.
            if (zoomLevel == model.getZ() || model.getZ() == minZoom) {
                model.setNeedLoading(true);
            } else {
                model.setNeedLoading(false);
            }
            return;
        } else {
            // If any one of the 4 child images needs to be loaded, we must load all 4 child images
            for (XyzTileModel child : childNodes) {
                // If an image has ignore = true, It will not be loaded on the screen, as its child images have already been needLoading.
                // If an image has ignore = false, The image will need will be loaded on the screen
                if (!child.isIgnore()) {
                    child.setNeedLoading(true);
                }
            }
        }
        // One of the child images of this image has been displayed on the screen,
        // so this image will not be loaded anymore and its values will be set as below.
        model.setNeedLoading(false);
        model.setIgnore(true);
    }
    
    /**
     * Create multiple child XyzTileModel with a higher zoom level.
     * @param parentModel
     * @param canvas
     */
    private void buildChild(XyzTileModel parentModel, Canvas canvas) {
        // The XyzTileModel with the highest zoom level will not have child XyzTileModel.
        if (parentModel.getZ() == maxZoomLevelAvailable) {
            return;
        }
        List<XyzTileModel> childNodes = new ArrayList<>();
        double childHeight = parentModel.getHeight() / 2;
        double childWidth = parentModel.getWidth() / 2;
        //Create the upper left child node.
        XyzTileModel childNode1 = new XyzTileModel(parentModel.getX() * 2, parentModel.getY() * 2, parentModel.getZ() + 1);
        childNode1.setWorldX(parentModel.getWorldX());
        childNode1.setWorldY(parentModel.getWorldY());
        calculateXyzTilePosition(childNode1, canvas, parentModel);
        // continue to generate child nodes
        buildChild(childNode1, canvas);
        childNodes.add(childNode1);
        
        //Create the upper right child node.
        XyzTileModel childNode2 = new XyzTileModel(parentModel.getX() * 2 + 1, parentModel.getY() * 2, parentModel.getZ() + 1);
        childNode2.setWorldX(parentModel.getWorldX() + childWidth);
        childNode2.setWorldY(parentModel.getWorldY());
        calculateXyzTilePosition(childNode2, canvas, parentModel);
        // continue to generate child nodes
        buildChild(childNode2, canvas);
        childNodes.add(childNode2);
        
        //Create the lower left child node.
        XyzTileModel childNode3 = new XyzTileModel(parentModel.getX() * 2, parentModel.getY() * 2 + 1, parentModel.getZ() + 1);
        childNode3.setWorldX(parentModel.getWorldX());
        childNode3.setWorldY(parentModel.getWorldY() - childHeight);
        calculateXyzTilePosition(childNode3, canvas, parentModel);
        // continue to generate child nodes
        buildChild(childNode3, canvas);
        childNodes.add(childNode3);
        
        // Create the lower right child node.
        XyzTileModel childNode4 = new XyzTileModel(parentModel.getX() * 2 + 1, parentModel.getY() * 2 + 1, parentModel.getZ() + 1);
        childNode4.setWorldX(parentModel.getWorldX() + childWidth);
        childNode4.setWorldY(parentModel.getWorldY() - childHeight);
        calculateXyzTilePosition(childNode4, canvas, parentModel);
        // continue to generate child nodes
        buildChild(childNode4, canvas);
        childNodes.add(childNode4);
        // add child nodes to parent node
        parentModel.setChildNodes(childNodes);
    }
    
    /**
     * Set the size of the child xyzTileModel based on the parent xyzTileModel.
     * @param xyzTileModel
     * @param canvas
     * @param parent
     */
    private void calculatePositionWithParent(XyzTileModel xyzTileModel, Canvas canvas, XyzTileModel parent) {
        // The height and width of the child will be half of the parent's
        xyzTileModel.setHeight(parent.getHeight() / 2.0);
        xyzTileModel.setWidth(parent.getWidth() / 2.0);
        
        // Create a property "points" to hold a list of 4 corner points and 1 center point and 4 midpoint.
        Vec3f corner1Point = new Vec3f((float) xyzTileModel.getWorldX(), (float) (xyzTileModel.getWorldY() - xyzTileModel.getHeight()), 0);
        Vec3f corner2Point = new Vec3f((float) (xyzTileModel.getWorldX() + xyzTileModel.getWidth()), (float) xyzTileModel.getWorldY(), 0);
        Vec3f corner3Point = new Vec3f((float) xyzTileModel.getWorldX(), (float) xyzTileModel.getWorldY(), 0);
        Vec3f corner4Point = new Vec3f((float) (xyzTileModel.getWorldX() + xyzTileModel.getWidth()),
                (float) (xyzTileModel.getWorldY() - xyzTileModel.getHeight()), 0);
        Vec3f centorPoint = new Vec3f((corner1Point.x + corner2Point.x) / 2, (corner1Point.y + corner2Point.y) / 2, 0);
        Vec3f midpoint1 = new Vec3f((corner1Point.x + corner3Point.x) / 2, (corner1Point.y + corner3Point.y) / 2, 0);
        Vec3f midpoint2 = new Vec3f((corner1Point.x + corner4Point.x) / 2, (corner1Point.y + corner4Point.y) / 2, 0);
        Vec3f midpoint3 = new Vec3f((corner2Point.x + corner3Point.x) / 2, (corner2Point.y + corner3Point.y) / 2, 0);
        Vec3f midpoint4 = new Vec3f((corner2Point.x + corner4Point.x) / 2, (corner2Point.y + corner4Point.y) / 2, 0);
        List<Vec3f> points = List.of(corner1Point, corner2Point, corner3Point, corner4Point,
                centorPoint, midpoint1, midpoint2, midpoint3, midpoint4);
        xyzTileModel.setPoints(points);
    }
    
    /**
     * Calculate the position and size of a XyzTileModel based on mapSizeBase
     * @param xyzTileModel
     * @param canvas
     */
    private void calculatePositionWithBase(XyzTileModel xyzTileModel, Canvas canvas) {
        Vec3f lod1Point;
        Vec3f lod2Point;
        Vec3f lod3Point;
        Vec3f lod4Point;
        // If this zoom level already has the mapSizeBase
        if (mapSizeBase.containsKey(xyzTileModel.getZ())) {
            // Use the base XyzTileModel to calculate the positions for other XyzTileModels.
            calculateXyzTile(xyzTileModel);
            lod1Point = new Vec3f((float) xyzTileModel.getWorldX(), (float) (xyzTileModel.getWorldY() - xyzTileModel.getHeight()), 0);
            lod2Point = new Vec3f((float) (xyzTileModel.getWorldX() + xyzTileModel.getWidth()), (float) xyzTileModel.getWorldY(), 0);
            lod3Point = new Vec3f((float) xyzTileModel.getWorldX(), (float) xyzTileModel.getWorldY(), 0);
            lod4Point = new Vec3f((float) (xyzTileModel.getWorldX() + xyzTileModel.getWidth()),
                    (float) (xyzTileModel.getWorldY() - xyzTileModel.getHeight()), 0);
        } else {
            // If this zoom level is not yet in the mapSizeBase, the coordinates will be calculated according to the formula.
            GeoCoordinate corner1 = calculateGeoCoordinate(xyzTileModel.getX(), xyzTileModel.getY() + 1, xyzTileModel.getZ());
            GeoCoordinate corner2 = calculateGeoCoordinate(xyzTileModel.getX() + 1, xyzTileModel.getY(), xyzTileModel.getZ());
            GeoCoordinate corner3 = calculateGeoCoordinate(xyzTileModel.getX(), xyzTileModel.getY(), xyzTileModel.getZ());
            GeoCoordinate corner4 = calculateGeoCoordinate(xyzTileModel.getX() + 1, xyzTileModel.getY() + 1, xyzTileModel.getZ());
            lod1Point = projectCoordinate(corner1);
            lod2Point = projectCoordinate(corner2);
            lod3Point = projectCoordinate(corner3);
            lod4Point = projectCoordinate(corner4);
            xyzTileModel.setWorldX(Math.min(lod1Point.x, lod2Point.x));
            xyzTileModel.setWorldY(Math.max(lod1Point.y, lod2Point.y));
            xyzTileModel.setWidth(Math.abs(lod1Point.x - lod2Point.x));
            xyzTileModel.setHeight(Math.abs(lod1Point.y - lod2Point.y));
            // Add to mapSizeBase to serve as the base for other images at the same zoom level.
            mapSizeBase.put(xyzTileModel.getZ(), xyzTileModel);
        }
        // Create a property "points" to hold a list of 4 corner points and 1 center point and 4 midpoint.
        Vec3f centorPoint = new Vec3f((lod1Point.x + lod2Point.x) / 2, (lod1Point.y + lod2Point.y) / 2, 0);
        Vec3f midpoint1 = new Vec3f((lod1Point.x + lod3Point.x) / 2, (lod1Point.y + lod3Point.y) / 2, 0);
        Vec3f midpoint2 = new Vec3f((lod1Point.x + lod4Point.x) / 2, (lod1Point.y + lod4Point.y) / 2, 0);
        Vec3f midpoint3 = new Vec3f((lod2Point.x + lod3Point.x) / 2, (lod2Point.y + lod3Point.y) / 2, 0);
        Vec3f midpoint4 = new Vec3f((lod2Point.x + lod4Point.x) / 2, (lod2Point.y + lod4Point.y) / 2, 0);
        List<Vec3f> points = List.of(lod1Point, lod2Point, lod3Point, lod4Point,
                centorPoint, midpoint1, midpoint2, midpoint3, midpoint4);
        xyzTileModel.setPoints(points);
    }
    
    /**
     * Calculate the position of a XyzTileModel based on mapSizeBase
     * @param xyzTileModel
     */
    private void calculateXyzTile(XyzTileModel xyzTileModel) {
        XyzTileModel base = mapSizeBase.get(xyzTileModel.getZ());
        int xTimes = xyzTileModel.getX() - base.getX();
        if (xTimes != 0) {
            // Calculate the X position of the image based on the base.
            xyzTileModel.setWorldX(base.getWorldX() + xTimes * base.getWidth());
        } else {
            xyzTileModel.setWorldX(base.getWorldX());
        }
        int yTimes = xyzTileModel.getY() - base.getY();
        if (yTimes != 0) {
            // Calculate the Y position of the image based on the base.
            xyzTileModel.setWorldY(base.getWorldY() - yTimes * base.getHeight());
        } else {
            xyzTileModel.setWorldY(base.getWorldY());
        }
        xyzTileModel.setHeight(base.getHeight());
        xyzTileModel.setWidth(base.getWidth());
    }
    
    /**
     * Calculate the shortest distance from the 4 corners and the center of the image to the camera
     * @param cameraPosition
     * @param tile
     * @return
     */
    private double calculateDistanceMinCamera(Transform cameraPosition, XyzTileModel tile) {
        Double min = null;
        // Calculate the shortest distance from the 4 corners and the center of the image to the camera
        for (Vec3f point : tile.getPoints()) {
            double distance = calculateDistanceCamera(cameraPosition, point);
            if (min == null || distance < min) {
                min = distance;
            }
        }
        return min;
    }
    
    /**
     * カメラまでの距離を計算する
     * @param cameraPosition
     * @param point
     * @return
     */
    private double calculateDistanceCamera(Transform cameraPosition, Vec3f point) {
        double dx = cameraPosition.getTx() - point.x;
        double dy = cameraPosition.getTy() - point.y;
        double dz = cameraPosition.getTz() - baseMapPositionZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Calculate the zoom level as zoom level = sqrt(THRESHOLD / distance).
     * @param distance
     * @return
     */
    private int calculateZoomLevelByDistance(double distance) {
        int zoomLevel = (int) Math.ceil(Math.sqrt(THRESHOLD / distance));
        return (zoomLevel > maxZoomLevelAvailable) ? maxZoomLevelAvailable : (zoomLevel < MIN_ZOOM_LEVEL) ? MIN_ZOOM_LEVEL : zoomLevel;
    }
    
    /**
     * Retrieve the list of images that need to be reloaded on the screen
     * @param xyzTileModels
     * @return
     */
    private List<XyzTileModel> flattenLoadedXyzTiles(List<XyzTileModel> xyzTileModels) {
        List<XyzTileModel> loadedXyzTiles = new ArrayList<>();
        // Iterate from the parent images with the smallest zoom level
        for (XyzTileModel model : xyzTileModels) {
            // automatically set ignore = false.
            model.setIgnore(false);
            // If the image needs to be loaded on the screen, it will be added to the return result
            if (model.isNeedLoading()) {
                model.setNeedLoading(false);
                loadedXyzTiles.add(model);
                continue;
            }
            if (model.getChildNodes() == null || model.getChildNodes().isEmpty()) {
                continue;
            }
            // Use recursion to iterate through the child images to find the images that need to be loaded.
            flattenXyzTilesRecursively(model, loadedXyzTiles);
        }
        return loadedXyzTiles;
    }
    
    /**
     * Retrieve the list of images that need to be reloaded on the screen
     * @param xyzTileModel
     * @param loadedXyzTiles
     */
    private void flattenXyzTilesRecursively(XyzTileModel xyzTileModel, List<XyzTileModel> loadedXyzTiles) {
        xyzTileModel.setIgnore(false);
        // If the image needs to be loaded on the screen, it will be added to the return result
        if (xyzTileModel.isNeedLoading()) {
            loadedXyzTiles.add(xyzTileModel);
            xyzTileModel.setNeedLoading(false);
            return;
        }
        if (xyzTileModel.getChildNodes() == null || xyzTileModel.getChildNodes().isEmpty()) {
            return;
        }
        for (XyzTileModel child : xyzTileModel.getChildNodes()) {
            // Use recursion to iterate through the child images to find the images that need to be loaded.
            flattenXyzTilesRecursively(child, loadedXyzTiles);
        }
    }
    
    /**
     * Calculate a reasonable zoom level to display the entire map without exceeding IMAGE_COUNT_DEFAULT
     * @param upper
     * @param lower
     * @param canvasBasemap
     * @return
     */
    private int calZoomLevel(GeoCoordinate upper, GeoCoordinate lower, Canvas canvasBasemap) {
        int zoomLevel = MAX_ZOOM_LEVEL_DEFAULT;
        boolean isZoomLevelAvailable = false;
        try {
            // Gradually decrease the zoom level to find a reasonable value.
            for (; zoomLevel >= MIN_ZOOM_LEVEL; zoomLevel--) {
                XyzTileModel tileUpper = getTiles(upper.lat, upper.lon, zoomLevel);
                XyzTileModel tileLower = getTiles(lower.lat, lower.lon, zoomLevel);
                // Do not recheck zoomAvailable if it has already been found previously
                if (!isZoomLevelAvailable) {
                    isZoomLevelAvailable = isZoomLevelAvailable(tileUpper);
                    if (isZoomLevelAvailable) {
                        // Find the highest available zoom level.
                        maxZoomLevelAvailable = zoomLevel;
                    }
                }
                // Calculate the number of images needed to load the entire map at the current zoom level.
                int imageCount = (Math.abs(tileUpper.getX() - tileLower.getX()) + 1) * (Math.abs(tileUpper.getY() - tileLower.getY()) + 1);
                // If the number of images is less than the default number of images, it will return the current zoom level.
                if (imageCount <= IMAGE_COUNT_DEFAULT && isZoomLevelAvailable) {
                    return zoomLevel;
                }
            }
        } catch (Exception e) {
            // If an exception occurs, it will return ZOOM_LEVEL_UNAVAILABLE.
            return ZOOM_LEVEL_UNVAILABLE;
        }
        // If a reasonable zoom level cannot be found, it will return MIN_ZOOM_LEVEL
        return MIN_ZOOM_LEVEL;
    }
    
    /**
     * Check the available zoom level for the current basemap URL
     * @param tileUpper
     * @return
     * @throws Exception
     */
    private boolean isZoomLevelAvailable(XyzTileModel tileUpper) throws Exception {
        try {
            String urlSTR = tileServerUrl;
            urlSTR = urlSTR.replace("{z}", tileUpper.getZ() + "")
                    .replace("{x}", tileUpper.getX() + "")
                    .replace("{y}", tileUpper.getY() + "");
            Image image = new Image(urlSTR);
            if (image.isError()) {
                throw image.getException();
            }
            return !image.isError();
        } catch (Exception e) {
            // If the image file does not exist, it will return false.
            if (e instanceof FileNotFoundException) {
                return false;
            } else {
                // Besides the error of the image not existing, all other errors will throw an exception.
                throw e;
            }
        }
    }
    
    /**
     * Initialize a Task for drawing an image
     * @param entry
     * @param gc
     * @param tileServerUrl
     * @return
     */
    private Task<Void> buildTask(
            Map.Entry<Integer, LinkedList<XyzTileModel>> entry,
            GraphicsContext gc,
            String tileServerUrl) {
        LinkedList<XyzTileModel> xyzTileModels = entry.getValue();
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (XyzTileModel xyzTileModel : xyzTileModels) {
                    String url = tileServerUrl;
                    // create a URL from a base url
                    url = url.replace("{z}", xyzTileModel.getZ() + "")
                            .replace("{x}", xyzTileModel.getX() + "")
                            .replace("{y}", xyzTileModel.getY() + "");
                    Image image = initImage(url);
                    if (isCancelled()) {
                        // Exit gracefully if the task is cancelled
                        break;
                    }
                    // draw the images on the canvas
                    Platform.runLater(() -> {
                        gc.drawImage(image,
                                xyzTileModel.getCanvasX(),
                                xyzTileModel.getCanvasY(),
                                xyzTileModel.getWidth(),
                                xyzTileModel.getHeight());
                    });
                }
                return null;
            }
        };
    }
    
    /**
     * Create the image object and check for errors. If there is an error, reinitialize it, with a maximum of 3 attempts.
     * @param url URL of the image
     * @return Image
     */
    private Image initImage(String url) {
        Image image = new Image(url);
        for (int i = 0; i < 3; i++) {
            if (image.isError()) {
                image = new Image(url);
                continue;
            }
        }
        return image;
    }
    
    /**
     * Construct the frame of the base map based on the coordinates of two corners.
     * @param upper
     * @param lower
     * @return
     */
    private Canvas buildCanvasBasemap(GeoCoordinate upper, GeoCoordinate lower) {
        Vec3f lod1Point = projectCoordinate(upper);
        Vec3f lod2Point = projectCoordinate(lower);
        
        // Compute the bounding box for the ImageView
        double minX = Math.min(lod1Point.x, lod2Point.x);
        double minY = Math.min(lod1Point.y, lod2Point.y);
        double width = Math.abs(lod1Point.x - lod2Point.x);
        double height = Math.abs(lod1Point.y - lod2Point.y);
        
        Canvas canvas = new Canvas(width, height);
        canvas.setLayoutX(minX);
        canvas.setLayoutY(minY);
        canvas.setScaleY(-1);
        Translate zTranslation = new Translate();
        zTranslation.setZ(baseMapPositionZ);
        canvas.getTransforms().addAll(zTranslation);
        return canvas;
    }
    
    /**
     * Create all XyzTileModels with the properties of size, position, child XyzTileModel, etc.
     * The XyzTileModel will have the highest zoom level equal to the specified zoom level.
     * The child XyzTileModel will have gradually increasing zoom levels.
     * @param canvas
     * @param upper
     * @param lower
     * @param zoomLevel The zoom level to be set as the highest for the XyzTileModel.
     * @return
     */
    private List<XyzTileModel> createXyzTileModelList(Canvas canvas, GeoCoordinate upper, GeoCoordinate lower, int zoomLevel) {
        // Calculate the XYZ tiles of the corners.
        XyzTileModel tileUpper = getTiles(upper.lat, upper.lon, zoomLevel);
        XyzTileModel tileLower = getTiles(lower.lat, lower.lon, zoomLevel);
        // Calculate the min and max values
        int minX = Math.min(tileUpper.getX(), tileLower.getX());
        int maxX = Math.max(tileUpper.getX(), tileLower.getX());
        int minY = Math.min(tileUpper.getY(), tileLower.getY());
        int maxY = Math.max(tileUpper.getY(), tileLower.getY());
        List<XyzTileModel> xyzTileModels = new ArrayList<>();
        // From the min and max values of X and Y, create all XyzTileModel instances.
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                xyzTileModels.add(new XyzTileModel(x, y, zoomLevel));
            }
        }
        //
        for (XyzTileModel tile : xyzTileModels) {
            // Calculate the position and size of the XyzTileModel.
            calculateXyzTilePosition(tile, canvas, null);
            // Create multiple child XyzTileModels for the XyzTileModel.
            buildChild(tile, canvas);
        }
        return xyzTileModels;
    }
    
    /**
     * Calculate the position and size of the XyzTileModel
     * @param xyzTileModel
     * @param canvas
     */
    private void calculateXyzTilePosition(XyzTileModel xyzTileModel, Canvas canvas, XyzTileModel parent) {
        // If this is the parent XyzTileModel
        if (parent == null) {
            calculatePositionWithBase(xyzTileModel, canvas);
        } else {
            // If this is the child XyzTileModel
            calculatePositionWithParent(xyzTileModel, canvas, parent);
        }
        // Calculate the reference coordinates according to the canvas.
        double canvasX = xyzTileModel.getWorldX() - canvas.getLayoutX();
        double canvasY = canvas.getHeight() - (xyzTileModel.getWorldY() - canvas.getLayoutY());
        
        xyzTileModel.setPositionSizeXyzTileModel(
                canvasX,
                canvasY);
        
        xyzTileModel.setNeedLoading(true);
    }
    
    /**
     * Determine the number of tasks to execute simultaneously based on the number of images that need to be loaded
     * @param size
     * @return
     */
    private int determineTaskCount(int size) {
        int[] thresholds = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 130, 160, 180, 200, 500, 1000, 1500, 2000, 2500};
        for (int i = 0; i < thresholds.length; i++) {
            if (size < thresholds[i]) {
                return i + 1;
            }
        }
        return thresholds.length + 1;
    }
    
    /**
     * Calculate the coordinates based on the xyz tile
     * @param x
     * @param y
     * @param z
     * @return
     */
    private GeoCoordinate calculateGeoCoordinate(int x, int y, int z) {
        double lat = tileY2lat(y, z);
        double lon = tileX2long(x, z);
        return new GeoCoordinate(List.of(lat, lon, 0d));
    }
    
    /**
     * Calculate XyzTileModel based on longitude, latitude, and zoom level.
     * @param latitude
     * @param longitude
     * @param zoomLevel
     * @return
     */
    private XyzTileModel getTiles(double latitude, double longitude, int zoomLevel) {
        return new XyzTileModel(long2tileX(longitude, zoomLevel), lat2tileY(latitude, zoomLevel), zoomLevel);
    }
    
    /**
     * Convert longitude to x.
     * @param longitude
     * @param z
     * @return
     */
    private int long2tileX(double longitude, int z) {
        return (int) Math.floor((longitude + 180.0) / 360.0 * (1 << z));
    }
    
    /**
     * Convert latitude to y.
     * @param latitude
     * @param z
     * @return
     */
    private int lat2tileY(double latitude, int z) {
        double latRad = Math.toRadians(latitude);
        return (int) Math.floor((1.0 - asinh(Math.tan(latRad)) / Math.PI) / 2.0 * (1 << z));
    }
    
    /**
     * Convert y to latitude.
     * @param y
     * @param z
     * @return
     */
    private double tileY2lat(int y, int z) {
        double n = Math.PI - 2.0 * Math.PI * y / Math.pow(2.0, z);
        return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    }
    
    /**
     * Convert x to longitude.
     * @param x
     * @param z
     * @return
     */
    private double tileX2long(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180.0;
    }
    
    /**
     * convert GeoCoordinate to Vec3f based on GeoReference
     * @param coordinate
     * @return
     */
    private Vec3f projectCoordinate(GeoCoordinate coordinate) {
        var geoReference = World.getActiveInstance().getGeoReference();
        return geoReference.project(coordinate);
    }
}
