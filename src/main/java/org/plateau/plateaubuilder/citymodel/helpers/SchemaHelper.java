package org.plateau.plateaubuilder.citymodel.helpers;

import org.citygml4j.xml.schema.Schema;
import org.citygml4j.xml.schema.SchemaHandler;

public class SchemaHelper {
    public static String getSchemaLocation(Schema schema) {
        var annotation = schema.getXSSchema().getAnnotation();
        if (annotation == null || annotation.getLocator() == null)
            return null;
        return annotation.getLocator().getSystemId();
    }

    public static String getRelativeSchemaLocation(Schema schema) {
        var absoluteLocation = getSchemaLocation(schema);
        if (absoluteLocation == null || !absoluteLocation.startsWith("file"))
            return null;

        String keyword = "/schemas/";
        return "../../schemas/" + absoluteLocation.substring(absoluteLocation.lastIndexOf(keyword) + keyword.length()).trim();
    }

    public static String getSchemaURI(Schema schema) {
        return schema.getNamespaceURI();
    }

    public static Schema getUroSchema(SchemaHandler schemaHandler) {
        var nss = schemaHandler.getTargetNamespaces();
        for (var ns : nss) {
            var schema = schemaHandler.getSchema(ns);
            var uri = schema.getNamespaceURI();
            var location = getSchemaLocation(schema);

            if (uri == null || location == null)
                continue;

            if (uri.contains("iur/uro") && (location.startsWith("file") || location.startsWith("http")))
                return schema;
        }

        return null;
    }
}
