package org.plateau.citygmleditor.utils;

import javafx.geometry.Point3D;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.plateau.citygmleditor.constant.PolygonRelationship;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.validation.exception.GeometryPyException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class PythonUtil {
    private static final String language = "python";

    public static PolygonRelationship checkPolygonRelationship(String filePy, String[] coornidatePolygon1, String[] coornidatePolygon2) throws IOException {
        List<String> cmd = Arrays.asList(language, filePy, Arrays.toString(coornidatePolygon1), Arrays.toString(coornidatePolygon2));
        // Create a ProcessBuilder with cmd to run Python and script path
        Process processBuilder = new ProcessBuilder(cmd).start();
        String error = IOUtils.toString(processBuilder.getErrorStream(), Charset.defaultCharset());

        if (StringUtils.isNotEmpty(error)) {
            System.out.println(error);
            throw new GeometryPyException("Got error when check 2 polygons intersect");
        }
        String output = IOUtils.toString(processBuilder.getInputStream(), Charset.defaultCharset()).trim();
        return PolygonRelationship.getRelationshipByText(output);
    }

    public static double[] findBestFitPlane(String path, String[] coornidates) throws IOException {
        List<String> cmd = Arrays.asList(language, path, Arrays.toString(coornidates));
        Process processBuilder = new ProcessBuilder(cmd).start();
        String error = IOUtils.toString(processBuilder.getErrorStream(), Charset.defaultCharset());

        if (StringUtils.isNotEmpty(error)) {
            System.out.println(error);
            throw new GeometryPyException("Got error when check 2 polygons intersect");
        }
        String[] output = IOUtils.toString(processBuilder.getInputStream(), Charset.defaultCharset()).trim().split(" ");

        Vec3f normal = new Vec3f(Float.parseFloat(output[0]), Float.parseFloat(output[1]), Float.parseFloat(output[2]));
        normal.normalize();
        Point3D point = new Point3D(Double.parseDouble(output[3]), (Double.parseDouble(output[4])), (Double.parseDouble(output[5])));
        double a = normal.x;
        double b = normal.y;
        double c = normal.z;
        double d = -(a * point.getX() + b * point.getY() + c * point.getZ());
        double[] plane = new double[4];
        plane[0] = a;
        plane[1] = b;
        plane[2] = c;
        plane[3] = d;
        return plane;
    }
}
