package org.plateau.citygmleditor.exporters;

import de.javagl.jgltf.model.io.GltfWriter;
import de.javagl.jgltf.model.creation.GltfModelBuilder;

import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;
import java.io.FileOutputStream;
import java.io.IOException;

public class GltfExporter {
    public static void export(String fileUrl, CityModel cityModel, ILODSolid lodSolid) {
        GltfModelBuilder builder = GltfModelBuilder.create();

        // TODO: CityModel→GltfModel

        try (FileOutputStream stream = new FileOutputStream("E:\\Temp\\export\\test.gltf")) {
            GltfWriter writer = new GltfWriter();
            writer.write(builder.build(), stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
