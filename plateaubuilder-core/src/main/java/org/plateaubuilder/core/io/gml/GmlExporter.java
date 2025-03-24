package org.plateaubuilder.core.io.gml;

import org.citygml4j.CityGMLContext;
import org.citygml4j.ade.iur.UrbanRevitalizationADEContext;
import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.xml.io.CityGMLOutputFactory;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityGMLWriter;
import org.citygml4j.xml.io.writer.FeatureWriteMode;
import org.citygml4j.xml.schema.SchemaHandler;
import org.plateaubuilder.core.citymodel.helpers.SchemaHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public class GmlExporter {

    public static void export(String fileUrl, CityModel cityModel, SchemaHandler schemaHandler, String appearanceDirName)
        throws ADEException, CityGMLBuilderException, CityGMLWriteException {
        OutputStream outputStream = null;
        try {
            var file = new File(fileUrl);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            export(outputStream, cityModel, schemaHandler, appearanceDirName);
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

    public static void export(OutputStream outputStream, CityModel cityModel, SchemaHandler schemaHandler)
            throws ADEException, CityGMLBuilderException, CityGMLWriteException {
        export(outputStream, cityModel, schemaHandler, null);
    }

    public static void export(OutputStream outputStream, CityModel cityModel, SchemaHandler schemaHandler, String appearanceDirName)
            throws ADEException, CityGMLBuilderException, CityGMLWriteException {
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

        var uroSchema = SchemaHelper.getUroSchema(schemaHandler);
        var uroSchemaURI = uroSchema == null
                ? null : SchemaHelper.getSchemaURI(uroSchema);
        var uroSchemaLocation = uroSchema == null
                ? null : SchemaHelper.getRelativeSchemaLocation(uroSchema);

        if (uroSchemaURI != null)
            writer.setPrefix("uro", uroSchemaURI);

        writer.setPrefix("core", "http://www.opengis.net/citygml/2.0");
        writer.setWriteXMLDecl(true);

        //writer.setDefaultNamespace(moduleContext.getModule(CityGMLModuleType.CORE));

        writer.setSchemaLocations(moduleContext);
        // Schema locations
        if (uroSchemaLocation != null)
            writer.setSchemaLocation(uroSchemaURI, uroSchemaLocation);

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
        if (appearanceDirName != null) {
            toRelativePath(tmpCityModel, appearanceDirName);
        }
        writer.write(tmpCityModel);
        writer.close();
    }

    private static void toRelativePath(CityModel tmpCityModel, String appearanceDirName) {
        // テクスチャが絶対パスで指定されているものを相対パスに差し替える
        for (var appearanceMember : tmpCityModel.getAppearanceMember()) {
            for (var surfaceDataMember : appearanceMember.getAppearance().getSurfaceDataMember()) {
                var surfaceData = surfaceDataMember.getSurfaceData();
                if (surfaceData instanceof ParameterizedTexture) {
                    var parameterizedTexture = (ParameterizedTexture) surfaceData;
                    var imageURI = parameterizedTexture.getImageURI();
                    if (Paths.get(imageURI).isAbsolute()) {
                        var filePathComponents = imageURI.split("/");
                        var newImageURI = appearanceDirName + "/" + filePathComponents[filePathComponents.length - 1];
                        parameterizedTexture.setImageURI(newImageURI);
                    }
                }
            }
        }
    }
}
