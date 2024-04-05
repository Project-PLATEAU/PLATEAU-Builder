package org.plateaubuilder.core.geospatial;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.osgeo.proj4j.*;
import org.plateaubuilder.core.utils3d.geom.Vec3d;
import org.plateaubuilder.core.utils3d.geom.Vec3f;

public class GeoReference {
    private final CoordinateTransform projectTransform;
    private final CoordinateTransform unprojectTransform;
    private final Vec3d origin;
    private StringProperty epsgCode = new SimpleStringProperty();

    public GeoReference(GeoCoordinate origin, String epsgCode) {
        // CRSの定義
        CRSFactory crsFactory = new CRSFactory();
        // GMLでのSRCはEPSG:4326（緯度経度座標、高さは扱わないためEPSG:6697とほぼ同義）
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");
        // TODO: 座標系選択
        // 投影後のSRCはEPSG:2451（平面直角座標9系）
        CoordinateReferenceSystem jpr = crsFactory.createFromName(epsgCode);

        // 座標変換のセットアップ
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        projectTransform = ctFactory.createTransform(wgs84, jpr);
        unprojectTransform = ctFactory.createTransform(jpr, wgs84);

        var originCoord = new ProjCoordinate(origin.lon, origin.lat);
        var originXY = new ProjCoordinate();
        projectTransform.transform(originCoord, originXY);
        this.origin = new Vec3d(originXY.x, originXY.y, origin.alt);
        setEPSGCode(epsgCode);
    }

    /**
     * 緯度経度座標を平面直角座標に投影後{@code origin}からの相対座標に変換します。
     * @param coordinate 緯度経度座標
     */
    public Vec3f project(GeoCoordinate coordinate) {
        // 変換する座標
        ProjCoordinate srcCoord = new ProjCoordinate(coordinate.lon, coordinate.lat);
        ProjCoordinate destCoord = new ProjCoordinate();

        // 座標変換の実行
        projectTransform.transform(srcCoord, destCoord);

        var position = new Vec3d(destCoord.x, destCoord.y, coordinate.alt);
        position.sub(origin);
        return new Vec3f((float)position.x, (float)position.y, (float)position.z);
    }

    /**
     * ローカル座標に{@code origin}を加算し平面直角座標に変換後緯度経度座標に逆投影します。
     * @param position ローカル座標
     * @return 緯度経度座標
     */
    public GeoCoordinate unproject(Vec3f position) {
        var position3d = new Vec3d(position.x, position.y, position.z);
        position3d.add(origin);

        ProjCoordinate srcCoord = new ProjCoordinate(position3d.x, position3d.y);
        ProjCoordinate destCoord = new ProjCoordinate();

        // 座標変換の実行
        unprojectTransform.transform(srcCoord, destCoord);

        return new GeoCoordinate(destCoord.y, destCoord.x, position3d.z);
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
