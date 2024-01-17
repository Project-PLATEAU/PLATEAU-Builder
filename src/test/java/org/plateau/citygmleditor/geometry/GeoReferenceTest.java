package org.plateau.citygmleditor.geometry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class GeoReferenceTest {
    @Test
    public void unproject_is_the_inverse_function_of_project() throws Exception {
        var origin = new GeoCoordinate(35.5330796818897, 139.7950744000569, 27.64626276);
        var geoReference = new GeoReference(origin, "EPSG:2451");
        var coordinate = new GeoCoordinate(35.53308230875351, 139.79636246227483, 3.602);
        var convertedCoordinate = geoReference.unproject(geoReference.project(coordinate));

        assertEquals(coordinate.lat, convertedCoordinate.lat, 0.0001f);
        assertEquals(coordinate.lon, convertedCoordinate.lon, 0.0001f);
        assertEquals(coordinate.alt, convertedCoordinate.alt, 0.0001f);
    }
}
