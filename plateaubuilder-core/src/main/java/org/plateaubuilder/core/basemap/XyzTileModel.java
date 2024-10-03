package org.plateaubuilder.core.basemap;

import org.plateaubuilder.core.utils3d.geom.Vec3f;

import java.util.List;

public class XyzTileModel {
    private double worldX;
    private double worldY;
    private double width;
    private double height;
    private int x;
    private int y;
    private int z;
    private double canvasX;
    private double canvasY;
    private boolean needLoading;
    private boolean ignore;
    private List<XyzTileModel> childNodes;
    private XyzTileModel parentNode;
    private List<Vec3f> points;
    public void setPositionSizeXyzTileModel(
            double canvasX,
            double canvasY) {
        this.canvasX = canvasX;
        this.canvasY = canvasY;
    }

    public XyzTileModel(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public List<Vec3f> getPoints() {
        return points;
    }

    public void setPoints(List<Vec3f> points) {
        this.points = points;
    }

    public List<XyzTileModel> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<XyzTileModel> childNodes) {
        this.childNodes = childNodes;
    }

    public XyzTileModel getParentNode() {
        return parentNode;
    }

    public void setParentNode(XyzTileModel parentNode) {
        this.parentNode = parentNode;
    }

    public boolean isNeedLoading() {
        return needLoading;
    }

    public void setNeedLoading(boolean needLoading) {
        this.needLoading = needLoading;
    }

    public double getCanvasX() {
        return canvasX;
    }

    public void setCanvasX(double canvasX) {
        this.canvasX = canvasX;
    }

    public double getCanvasY() {
        return canvasY;
    }

    public void setCanvasY(double canvasY) {
        this.canvasY = canvasY;
    }

    public double getWorldX() {
        return worldX;
    }

    public void setWorldX(double worldX) {
        this.worldX = worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public void setWorldY(double worldY) {
        this.worldY = worldY;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
