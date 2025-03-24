package org.plateaubuilder.core.geospatial;

import java.util.List;

public class GeoCoordinate {
    public double lat;
    public double lon;
    public double alt;

    public GeoCoordinate(double lat, double lon, double alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    public GeoCoordinate(List<Double> coord) {
        this.lat = coord.get(0);
        this.lon = coord.get(1);
        this.alt = coord.get(2);
    }

    public GeoCoordinate add(GeoCoordinate other) {
        return new GeoCoordinate(
                this.lat + other.lat,
                this.lon + other.lon,
                this.alt + other.alt
        );
    }

    public GeoCoordinate sub(GeoCoordinate other) {
        return new GeoCoordinate(
                this.lat - other.lat,
                this.lon - other.lon,
                this.alt - other.alt
        );
    }

    public GeoCoordinate divide(double value) {
        return new GeoCoordinate(
                this.lat / value,
                this.lon / value,
                this.alt / value
        );
    }
}
