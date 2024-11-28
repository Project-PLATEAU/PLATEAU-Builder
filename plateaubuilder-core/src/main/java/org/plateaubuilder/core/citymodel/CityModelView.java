package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.xml.schema.SchemaHandler;

import java.util.List;
import java.util.stream.Collectors;

public class CityModelView extends ManagedGMLView<CityModel> {
    private SchemaHandler schemaHandler;

    private String gmlPath;

    private AppearanceView appearance;

    public SchemaHandler getSchemaHandler() {
        return this.schemaHandler;
    }

    public CityModelView(CityModel gmlObject, SchemaHandler schemaHandler) {
        super(gmlObject);

        this.schemaHandler = schemaHandler;
    }

    public AppearanceView getAppearance() {
        return this.appearance;
    }

    public void setAppearance(AppearanceView appearance) {
        this.appearance = appearance;
    }

    public String getGmlPath() {
        return gmlPath;
    }

    public void setGmlPath(String gmlPath) {
        this.gmlPath = gmlPath;
    }

    public List<IFeatureView> getCityObjectMembersUnmodifiable() {
        return getChildrenUnmodifiable()
                .stream()
                .map((node) -> (IFeatureView) node)
                .collect(Collectors.toList());
    }
    
  public List<IFeatureView> getFeatureViews() {
        return getChildrenUnmodifiable()
                .stream()
                .filter(node -> node instanceof IFeatureView) // IFeatureViewのインスタンスにフィルタリング
                .map(node -> (IFeatureView) node) // IFeatureViewにキャスト
                .collect(Collectors.toList());
    }
}
