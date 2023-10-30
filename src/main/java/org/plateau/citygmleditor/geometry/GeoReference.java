package org.plateau.citygmleditor.geometry;

import org.plateau.citygmleditor.utils3d.geom.Vec3d;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;

public class GeoReference {
    private static double WGS84_A = 6378137.0;
    private static double WGS84_IF = 298.257223563;
    private static double WGS84_F = 1 / WGS84_IF;
    private static double WGS84_B = WGS84_A * (1 - WGS84_F);
    private static double WGS84_E = Math.sqrt(2 * WGS84_F - WGS84_F * WGS84_F);

    private GeoCoordinate origin;
    private Vec3d referenceECEF;

    public GeoReference(GeoCoordinate origin) {
        this.origin = origin;
        this.referenceECEF = latLonToECEF(origin);
    }

    /**
     * 緯度経度から原点中心の平面座標(ENU)に変換します。
     * 参考：<a href="https://en.wikipedia.org/wiki/Local_tangent_plane_coordinates">Local tangent plane coordinates</a>
     */
    public Vec3f Project(GeoCoordinate coordinate) {
        var ecef = latLonToECEF(coordinate);

        double clat = Math.cos(Math.toRadians(origin.lat));
        double slat = Math.sin(Math.toRadians(origin.lat));
        double clon = Math.cos(Math.toRadians(origin.lon));
        double slon = Math.sin(Math.toRadians(origin.lon));
        ecef.sub(referenceECEF);

        return new Vec3f(
                (float) (-slon * ecef.x + clon * ecef.y),
                (float) (-slat * clon * ecef.x - slat * slon * ecef.y + clat * ecef.z),
                (float) (clat * clon * ecef.x + clat * slon * ecef.y + slat * ecef.z)
        );
    }

    public static Vec3d latLonToECEF(GeoCoordinate coordinate) {
        double clat = Math.cos(Math.toRadians(coordinate.lat));
        double slat = Math.sin(Math.toRadians(coordinate.lat));
        double clon = Math.cos(Math.toRadians(coordinate.lon));
        double slon = Math.sin(Math.toRadians(coordinate.lon));

        double N = WGS84_A / Math.sqrt(1.0 - WGS84_E * WGS84_E * slat * slat);
        return new Vec3d(
                (N + coordinate.alt) * clat * clon,
                (N + coordinate.alt) * clat * slon,
                (N * (1.0 - WGS84_E * WGS84_E) + coordinate.alt) * slat
        );
    }
}
