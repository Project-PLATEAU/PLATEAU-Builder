package org.plateau.citygmleditor.importers.gml;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import org.citygml4j.CityGMLContext;
import org.citygml4j.ade.iur.UrbanRevitalizationADEContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.plateau.citygmleditor.citymodel.factory.CityModelFactory;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.geometry.GeoReference;
import org.plateau.citygmleditor.world.World;

import java.io.File;
import java.io.IOException;

public class GmlImporter {

    public static Node loadGml(String fileUrl) throws Exception {
        // get extension
        final int dot = fileUrl.lastIndexOf('.');
        if (dot <= 0) {
            throw new IOException("Unknown format, url missing extension [" + fileUrl + "]");
        }

        var node = new Group();
        node.setId(new File(fileUrl).getName());
        Material material = new PhongMaterial(Color.WHITE);

        CityGMLContext context = CityGMLContext.getInstance();

        if (!context.hasADEContexts())
            context.registerADEContext(new UrbanRevitalizationADEContext());

        CityGMLBuilder builder = context.createCityGMLBuilder();
        CityGMLInputFactory in = builder.createCityGMLInputFactory();
        CityGMLReader reader = in.createCityGMLReader(new File(fileUrl));

        while (reader.hasNext()) {
            CityGML citygml = reader.nextFeature();
            if (citygml.getCityGMLClass() != CityGMLClass.CITY_MODEL)
                continue;

            var world = World.getActiveInstance();

            // 座標投影設定
            var envelope = ((org.citygml4j.model.citygml.core.CityModel) citygml).getBoundedBy().getEnvelope();
            var lowerCorner = envelope.getLowerCorner().toList3d();
            var min = new GeoCoordinate(lowerCorner);
            var upperCorner = envelope.getUpperCorner().toList3d();
            var max = new GeoCoordinate(upperCorner);
            var center = min.add(max).divide(2);
            world.setGeoReference(new GeoReference(center));

            var cityModelFactory = new CityModelFactory();
            var cityModel = cityModelFactory.createCityModel((CityModel) citygml, fileUrl, in.getSchemaHandler());

            node.getChildren().add(cityModel);

            world.setCityModel(cityModel);
        }
        reader.close();

        return node;
    }
}