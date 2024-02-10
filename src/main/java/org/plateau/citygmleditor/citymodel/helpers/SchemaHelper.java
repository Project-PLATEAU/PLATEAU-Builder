package org.plateau.citygmleditor.citymodel.helpers;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.xml.schema.SchemaHandler;

import java.util.HashMap;
import java.util.Objects;

public class SchemaHelper {
    public static String getUroSchemaURI(SchemaHandler schemaHandler) {
        var version = getUroSchemaVersion(schemaHandler);
        if (version == 0)
            return null;

        return getUroSchemaURI(version);
    }

    public static String getUroSchemaLocation(SchemaHandler schemaHandler) {
        var version = getUroSchemaVersion(schemaHandler);
        if (version == 0)
            return null;

        return getUroSchemaLocation(version);
    }

    public static String getUroSchemaURI(int version) {
        switch (version) {
            case 2: return "https://www.geospatial.jp/iur/uro/2.0";
            case 3: return "https://www.geospatial.jp/iur/uro/3.0";
            default:
                throw new OutOfRangeException(version, 2, 3);
        }
    }

    public static String getUroSchemaLocation(int version) {
        switch (version) {
            case 2: return "../../schemas/iur/uro/2.0/urbanObject.xsd";
            case 3: return "../../schemas/iur/uro/3.0/urbanObject.xsd";
            default:
                throw new OutOfRangeException(version, 2, 3);
        }
    }

    public static int getUroSchemaVersion(SchemaHandler schemaHandler) {
        var nss = schemaHandler.getTargetNamespaces();
        for (var ns : nss) {
            var schema = schemaHandler.getSchema(ns);
            if (Objects.equals(schema.getNamespaceURI(), getUroSchemaURI(2)))
                return 2;
            if (Objects.equals(schema.getNamespaceURI(), getUroSchemaURI(3)))
                return 3;
        }

        return 0;
    }
}
