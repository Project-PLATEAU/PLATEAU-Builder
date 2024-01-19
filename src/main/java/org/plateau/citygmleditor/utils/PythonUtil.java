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
        Map<String, String> result = new HashMap<>();
        boolean invalidParam1 = param1.length % 3 != 0;
        if (invalidParam1) {
            result.put("ERROR_PARAM_1", Arrays.toString(param1));
        }
        boolean invalidParam2 = param2.length % 3 != 0;
        if (invalidParam2) {
            result.put("ERROR_PARAM_2", Arrays.toString(param2));
        }
        if (invalidParam1 || invalidParam2) return result;
        List<String> pythonCommand = Arrays.asList(language, filePy, Arrays.toString(param1), Arrays.toString(param2));
        // Create a ProcessBuilder with cmd to run Python and script path
        Process processBuilder = new ProcessBuilder(pythonCommand).start();
        String error = IOUtils.toString(processBuilder.getErrorStream(), Charset.defaultCharset());
        String outPy = IOUtils.toString(processBuilder.getInputStream(), Charset.defaultCharset());
        result.put("ERROR", error);
        result.put("OUTPUT", outPy);
        return result;
    }
}
