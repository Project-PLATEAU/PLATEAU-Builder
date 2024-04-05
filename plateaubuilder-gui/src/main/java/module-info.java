open module plateaubuilder.gui {
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
    requires proj4j;
    requires vecmath;
    requires com.sun.xml.bind;
    requires xercesImpl;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires plateaubuilder.core;
    requires plateaubuilder.validation;
}
