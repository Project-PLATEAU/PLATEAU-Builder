package org.plateaubuilder.core.io.gml;

import org.citygml4j.CityGMLContext;
import org.citygml4j.ade.iur.UrbanRevitalizationADEContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.factory.CityModelViewFactory;
import org.plateaubuilder.core.geospatial.GeoCoordinate;
import org.plateaubuilder.core.geospatial.GeoReference;
import org.plateaubuilder.core.world.World;

import java.io.File;
import java.io.IOException;

public class GmlImporter {

    public static CityModelView loadGml(CityModelGroup group, String fileUrl, String epsgCode) throws Exception {
        // get extension
        final int dot = fileUrl.lastIndexOf('.');
        if (dot <= 0) {
            throw new IOException("Unknown format, url missing extension [" + fileUrl + "]");
        }

        CityGMLContext context = CityGMLContext.getInstance();

        if (!context.hasADEContexts())
            context.registerADEContext(new UrbanRevitalizationADEContext());

        CityGMLBuilder builder = context.createCityGMLBuilder();
        CityGMLInputFactory in = builder.createCityGMLInputFactory();
        CityGMLReader reader = in.createCityGMLReader(new File(fileUrl));

        CityModelView cityModelView = null;

        while (reader.hasNext()) {
            CityGML citygml = reader.nextFeature();
            if (citygml.getCityGMLClass() != CityGMLClass.CITY_MODEL)
                continue;

            var world = World.getActiveInstance();
            if (group.getChildren().isEmpty()) {
                // 座標投影設定
                var envelope = ((org.citygml4j.model.citygml.core.CityModel) citygml).getBoundedBy().getEnvelope();
                var lowerCorner = envelope.getLowerCorner().toList3d();
                var min = new GeoCoordinate(lowerCorner);
                var upperCorner = envelope.getUpperCorner().toList3d();
                var max = new GeoCoordinate(upperCorner);
                var center = min.add(max).divide(2);
                center.alt = 0;
                world.setGeoReference(new GeoReference(center, epsgCode));
            }
            var cityModelViewFactory = new CityModelViewFactory();
            cityModelView = cityModelViewFactory.createCityModel(group, (CityModel) citygml, fileUrl, in.getSchemaHandler());
        }
        reader.close();

        return cityModelView;
    }
}