package org.plateau.citygmleditor.importers.gltf;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import javafx.scene.Node;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GltfImporter {
    public static Node loadGltf(String fileUrl) throws Exception {
        Path inputFile = Paths.get(fileUrl);
        GltfModelReader gltfModelReader = new GltfModelReader();
        GltfModel gltfModel = gltfModelReader.read(inputFile);

        // TODO:GltfModel→CityModel変換
        System.out.println(gltfModel);

        return null;
    }
}
