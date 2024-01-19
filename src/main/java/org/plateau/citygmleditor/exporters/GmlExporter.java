package org.plateau.citygmleditor.exporters;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.citygml4j.CityGMLContext;
import org.citygml4j.ade.iur.UrbanRevitalizationADEContext;
import org.citygml4j.builder.copy.CopyBuilder;
import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.gmlid.GMLIdManager;
import org.citygml4j.xml.io.CityGMLOutputFactory;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityGMLWriter;
import org.citygml4j.xml.io.writer.FeatureWriteMode;
import org.citygml4j.xml.schema.SchemaHandler;

import java.io.File;

public class GmlExporter {

    public static void export(String fileUrl, CityModel cityModel, SchemaHandler schemaHandler)
        throws ADEException, CityGMLBuilderException, CityGMLWriteException {
        OutputStream outputStream = null;
        try {
            var file = new File(fileUrl);
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            export(outputStream, cityModel, schemaHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void export(OutputStream outputStream, CityModel cityModel, SchemaHandler schemaHandler) throws ADEException, CityGMLBuilderException, CityGMLWriteException {
        CityGMLContext context = CityGMLContext.getInstance();

        if (!context.hasADEContexts())
            context.registerADEContext(new UrbanRevitalizationADEContext());

        CityGMLBuilder builder = context.createCityGMLBuilder();
        CityGMLOutputFactory out = builder.createCityGMLOutputFactory(schemaHandler);
        ModuleContext moduleContext = new ModuleContext(CityGMLVersion.v2_0_0);

        FeatureWriteMode writeMode = FeatureWriteMode.NO_SPLIT;

        out.setModuleContext(moduleContext);
        out.setGMLIdManager(DefaultGMLIdManager.getInstance());
        out.setProperty(CityGMLOutputFactory.FEATURE_WRITE_MODE, writeMode);
        out.setProperty(CityGMLOutputFactory.SPLIT_COPY, false);

        //out.setProperty(CityGMLOutputFactory.EXCLUDE_FROM_SPLITTING, ADEComponent.class);

        CityGMLWriter writer = out.createCityGMLWriter(outputStream, "utf-8");

        writer.setPrefixes(moduleContext);
        writer.setPrefix("uro", "https://www.geospatial.jp/iur/uro/2.0");
        writer.setPrefix("core", "http://www.opengis.net/citygml/2.0");
        writer.setWriteXMLDecl(true);

        //writer.setDefaultNamespace(moduleContext.getModule(CityGMLModuleType.CORE));

        // Schema locations
        writer.setSchemaLocation("https://www.geospatial.jp/iur/uro/2.0", "../../schemas/iur/uro/2.0/urbanObject.xsd");
        writer.setSchemaLocation("http://www.opengis.net/citygml/2.0", "http://schemas.opengis.net/citygml/2.0/cityGMLBase.xsd");
        writer.setSchemaLocation("http://www.opengis.net/citygml/landuse/2.0", "http://schemas.opengis.net/citygml/landuse/2.0/landUse.xsd");
        writer.setSchemaLocation("http://www.opengis.net/citygml/building/2.0", "http://schemas.opengis.net/citygml/building/2.0/building.xsd");
        writer.setSchemaLocation("http://www.opengis.net/citygml/transportation/2.0", "http://schemas.opengis.net/citygml/transportation/2.0/transportation.xsd");
        writer.setSchemaLocation("http://www.opengis.net/citygml/generics/2.0", "http://schemas.opengis.net/citygml/generics/2.0/generics.xsd");
        writer.setSchemaLocation("http://www.opengis.net/citygml/cityobjectgroup/2.0", "http://schemas.opengis.net/citygml/cityobjectgroup/2.0/cityObjectGroup.xsd");
        writer.setSchemaLocation("http://www.opengis.net/gml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
        writer.setSchemaLocation("http://www.opengis.net/citygml/appearance/2.0", "http://schemas.opengis.net/citygml/appearance/2.0/appearance.xsd");
        writer.setIndentString("\t");

        var tmpCityModel = (CityModel)cityModel.copy(new DeepCopyBuilder());
        writer.write(tmpCityModel);
        writer.close();
    }
}
