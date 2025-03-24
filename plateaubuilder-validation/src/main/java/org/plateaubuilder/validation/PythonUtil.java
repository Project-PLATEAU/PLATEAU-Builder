package org.plateaubuilder.validation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.plateaubuilder.validation.constant.PolygonRelationship;
import org.plateaubuilder.validation.exception.GeometryPyException;

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
}
