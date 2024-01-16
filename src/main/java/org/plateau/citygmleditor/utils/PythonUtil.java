package org.plateau.citygmleditor.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PythonUtil {
    private static final String language = "python";

    public static Map<String, String> checkIntersecWithPyCmd(String filePy, String[] param1, String[] param2) throws IOException {
        List<String> pythonCommand = Arrays.asList(language, filePy, Arrays.toString(param1), Arrays.toString(param2));
        // Create a ProcessBuilder with cmd to run Python and script path
        Process processBuilder = new ProcessBuilder(pythonCommand).start();
        String error = IOUtils.toString(processBuilder.getErrorStream(), Charset.defaultCharset());
        String outPy = IOUtils.toString(processBuilder.getInputStream(), Charset.defaultCharset());
        Map<String, String> result = new HashMap<>();
        result.put("ERROR", error);
        result.put("OUTPUT", outPy);
        return result;
    }
}
