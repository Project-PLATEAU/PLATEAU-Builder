package org.plateaubuilder.core.geospatial;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.gdal.gdal.gdal;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.plateaubuilder.core.utils3d.geom.Vec3d;
import org.plateaubuilder.core.utils3d.geom.Vec3f;

public class GeoReference {
    private CoordinateTransformation projectTransform;
    private CoordinateTransformation unprojectTransform;
    private Vec3d origin;
    private StringProperty epsgCode = new SimpleStringProperty();

    public GeoReference(GeoCoordinate origin, String epsgCode) {
        // GDALの設定
        gdal.SetConfigOption("GDAL_DATA", "./gdal/gdal-data");
        gdal.SetConfigOption("PROJ_LIB", "./gdal/projlib");
        // GDALの初期化
        gdal.AllRegister();

        // CRSの定義
        SpatialReference  geo = new SpatialReference();
        SpatialReference  prj = new SpatialReference();

        geo.ImportFromEPSG(6697);
        prj.ImportFromEPSG(Integer.parseInt(epsgCode.substring(epsgCode.length() - 4)));

        // 座標変換のセットアップ
        projectTransform = new CoordinateTransformation(geo, prj);
        unprojectTransform = new CoordinateTransformation(prj, geo);

        double[] originCoord = {origin.lat, origin.lon, origin.alt};

        projectTransform.TransformPoint(originCoord);
        this.origin = new Vec3d(originCoord[1], originCoord[0], originCoord[2]);
        setEPSGCode(epsgCode);
    }

    /**
     * 緯度経度座標を平面直角座標に投影後{@code origin}からの相対座標に変換します。
     * @param coordinate 緯度経度座標
     */
    public Vec3f project(GeoCoordinate coordinate) {
        // 変換する座標
        double[] coord = {coordinate.lat, coordinate.lon, coordinate.alt};

        // 座標変換の実行
        projectTransform.TransformPoint(coord);

        var position = new Vec3d(coord[1], coord[0], coord[2]);
        position.sub(origin);
        return new Vec3f((float) position.x, (float) position.y, (float) position.z);
    }

    /**
     * ローカル座標に{@code origin}を加算し平面直角座標に変換後緯度経度座標に逆投影します。
     * @param position ローカル座標
     * @return 緯度経度座標
     */
    public GeoCoordinate unproject(Vec3f position) {
        var position3d = new Vec3d(position.x, position.y, position.z);
        position3d.add(origin);

        double[] coord = {position3d.y, position3d.x, position3d.z};

        // 座標変換の実行
        unprojectTransform.TransformPoint(coord);

        return new GeoCoordinate(coord[0], coord[1], coord[2]);
    }

    /**
     * 平面直角座標系での原点を取得します。
     */
    public Vec3d getOrigin() {
        return origin;
    }
    
    public StringProperty getEPSGCodeProperty() {
        return epsgCode;
    }

    /**
     * 空間座標系（EPSGコード）を取得
     */
    public String getEPSGCode() {
        return epsgCode.get();
    }

    /**
     * 空間座標系（EPSGコード）を設定
     */
    public void setEPSGCode(String epsgCode) {
        this.epsgCode.set(epsgCode);
    }
}
