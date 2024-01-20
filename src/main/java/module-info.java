import org.plateau.citygmleditor.importers.Importer;
import org.plateau.citygmleditor.importers.obj.ObjOrPolyObjImporter;

module CityGMLEditor {
    requires java.desktop;
    requires java.logging;
    requires java.xml;

    requires javafx.swing;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;

    requires citygml4j;
    requires org.citygml4j.ade;
    requires transitive java.xml.bind;
    requires transitive com.sun.xml.xsom;
    requires j3d.core.utils;
    requires org.apache.commons.lang3;
    requires org.locationtech.jts;
    requires jgltf.model;
    requires jgltf.model.builder;
    requires jgltf.impl.v1;
    requires jgltf.impl.v2;
    requires com.fasterxml.jackson.core;
    requires commons.math3;
    requires proj4j;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;

    provides Importer with
            ObjOrPolyObjImporter;

    exports org.plateau.citygmleditor.importers;
    exports org.plateau.citygmleditor.importers.obj;

    exports org.plateau.citygmleditor.modelstandard;
    exports org.plateau.citygmleditor.citygmleditor;
    opens org.plateau.citygmleditor.citygmleditor to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.geometry;
    opens org.plateau.citygmleditor.geometry to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.importers.gml;
    opens org.plateau.citygmleditor.importers.gml to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.validation;
    exports org.plateau.citygmleditor.citymodel;
    opens org.plateau.citygmleditor.citymodel to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.citymodel.geometry;
    opens org.plateau.citygmleditor.citymodel.geometry to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.utils3d.geom;
    opens org.plateau.citygmleditor.utils3d.geom to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.utils3d.polygonmesh;
    opens org.plateau.citygmleditor.utils3d.polygonmesh to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.fxml;
    opens org.plateau.citygmleditor.fxml to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.control;
    opens org.plateau.citygmleditor.control to javafx.fxml, javafx.graphics;
    exports org.plateau.citygmleditor.world;
    opens org.plateau.citygmleditor.world to javafx.fxml, javafx.graphics;

    uses Importer;
}
