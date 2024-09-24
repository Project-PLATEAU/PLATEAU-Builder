import org.plateaubuilder.core.io.mesh.importers.Importer;
import org.plateaubuilder.core.io.mesh.importers.ObjOrPolyObjImporter;

open module plateaubuilder.core {
    exports org.plateaubuilder.core.editor;
    exports org.plateaubuilder.core.citymodel;
    exports org.plateaubuilder.core.citymodel.citygml;
    exports org.plateaubuilder.core.editor.attribute;
    exports org.plateaubuilder.core.editor.commands;
    exports org.plateaubuilder.core.citymodel.geometry;
    exports org.plateaubuilder.core.world;
    exports org.plateaubuilder.core.io.gml;
    exports org.plateaubuilder.core.citymodel.helpers;
    exports org.plateaubuilder.core.citymodel.factory;
    exports org.plateaubuilder.core.editor.transform;
    exports org.plateaubuilder.core.io.mesh;
    exports org.plateaubuilder.core.io.mesh.exporters;
    exports org.plateaubuilder.core.utils3d.geom;
    exports org.plateaubuilder.core.io.mesh.converters;
    exports org.plateaubuilder.core.editor.surfacetype;
    exports org.plateaubuilder.core.utils;
    exports org.plateaubuilder.core.geospatial;
    exports org.plateaubuilder.core.utils3d.polygonmesh;
    exports org.plateaubuilder.core.citymodel.attribute;
    exports org.plateaubuilder.core.citymodel.attribute.manager;
    exports org.plateaubuilder.core.citymodel.attribute.wrapper;
    exports org.plateaubuilder.core.citymodel.attribute.reader;

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
    requires com.sun.xml.fastinfoset;
    requires commons.math3;
    requires vecmath;
    requires com.sun.xml.bind;
    requires xercesImpl;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;

    provides Importer with
            ObjOrPolyObjImporter;

    uses Importer;
}
