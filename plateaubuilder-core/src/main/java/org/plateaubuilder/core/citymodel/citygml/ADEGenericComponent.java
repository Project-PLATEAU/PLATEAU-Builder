package org.plateaubuilder.core.citymodel.citygml;

import java.util.Arrays;
import java.util.List;

import org.citygml4j.builder.copy.CopyBuilder;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.ade.ADEClass;
import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.binding.ADEBoundingBoxHelper;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.base.ModelObjects;
import org.citygml4j.model.common.child.ChildList;
import org.citygml4j.model.common.visitor.FeatureFunctor;
import org.citygml4j.model.common.visitor.FeatureVisitor;
import org.citygml4j.model.common.visitor.GMLFunctor;
import org.citygml4j.model.common.visitor.GMLVisitor;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.util.bbox.BoundingBoxOptions;
import org.w3c.dom.Node;

public class ADEGenericComponent extends AbstractCityObject implements ADEGenericModuleComponent {

    private Code clazz;
    private List<Code> function;
    private List<Code> usage;
    private MultiSurfaceProperty lod1MultiSurface;
    private List<ADEComponent> ade;

    private String nodeName;

    public ADEGenericComponent() {
    }

    public ADEGenericComponent(ADEGenericElement genericADEElement) {
        addGenericADEElement(genericADEElement);
        getGenericApplicationPropertyOfADEGenericComponent().add(genericADEElement);

        var content = genericADEElement.getContent();
        this.nodeName = content.getNodeName();
        this.setName(Arrays.asList(new Code(content.getLocalName())));

        var attributes = content.getAttributes();
        for (var i = 0; i < attributes.getLength(); i++) {
            var attribute = attributes.item(i);
            if (attribute.getNodeName().equals("gml:id")) {
                setId(attribute.getNodeValue());
                break;
            }
        }

        var childNodes = content.getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("urf:lod1MultiSurface")) {
                lod1MultiSurface = createMultiSurfaceProperty(childNode);
            }
        }
    }

    public void addFunction(Code function) {
        getFunction().add(function);
    }

    public void addUsage(Code usage) {
        getUsage().add(usage);
    }

    public void addGenericApplicationPropertyOfADEGenericComponent(ADEComponent ade) {
        getGenericApplicationPropertyOfADEGenericComponent().add(ade);
    }

    public Code getClazz() {
        return clazz;
    }

    public List<Code> getFunction() {
        if (function == null)
            function = new ChildList<>(this);

        return function;
    }

    public List<Code> getUsage() {
        if (usage == null)
            usage = new ChildList<>(this);

        return usage;
    }

    public List<ADEComponent> getGenericApplicationPropertyOfADEGenericComponent() {
        if (ade == null)
            ade = new ChildList<>(this);

        return ade;
    }

    public MultiSurfaceProperty getLod1MultiSurface() {
        return lod1MultiSurface;
    }

    public void setClazz(Code clazz) {
        this.clazz = ModelObjects.setParent(clazz, this);
    }

    public void setFunction(List<Code> function) {
        this.function = new ChildList<>(this, function);
    }

    public void setUsage(List<Code> usage) {
        this.usage = new ChildList<>(this, usage);
    }

    public void setGenericApplicationPropertyOfADEGenericComponent(List<ADEComponent> ade) {
        this.ade = new ChildList<>(this, ade);
    }

    public void setLod1MultiSurface(MultiSurfaceProperty lod1MultiSurface) {
        this.lod1MultiSurface = lod1MultiSurface;
    }

    public boolean isSetClazz() {
        return clazz != null;
    }

    public boolean isSetFunction() {
        return function != null && !function.isEmpty();
    }

    public boolean isSetUsage() {
        return usage != null && !usage.isEmpty();
    }

    public boolean isSetGenericApplicationPropertyOfADEGenericComponent() {
        return ade != null && !ade.isEmpty();
    }

    public boolean isSetLod1MultiSurface() {
        return lod1MultiSurface != null;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void unsetLod1MultiSurface() {
        lod1MultiSurface = ModelObjects.setNull(lod1MultiSurface);
    }

    @Override
    public CityGMLClass getCityGMLClass() {
        return CityGMLClass.ADE_COMPONENT;
    }

    @Override
    public BoundingShape calcBoundedBy(BoundingBoxOptions options) {
        BoundingShape boundedBy = super.calcBoundedBy(options);
        if (options.isUseExistingEnvelopes() && !boundedBy.isEmpty())
            return boundedBy;

        MultiSurfaceProperty multiSurfaceProperty = lod1MultiSurface;
        if (multiSurfaceProperty != null) {
            if (multiSurfaceProperty.isSetMultiSurface()) {
                boundedBy.updateEnvelope(multiSurfaceProperty.getMultiSurface().calcBoundingBox());
            } else {
                // xlink
            }
        }

        if (isSetGenericApplicationPropertyOfADEGenericComponent()) {
            for (ADEComponent ade : getGenericApplicationPropertyOfADEGenericComponent()) {
                if (ade.getADEClass() == ADEClass.MODEL_OBJECT)
                    boundedBy.updateEnvelope(ADEBoundingBoxHelper.calcBoundedBy((ADEModelObject) ade, options).getEnvelope());
            }
        }

        if (options.isAssignResultToFeatures())
            setBoundedBy(boundedBy);

        return boundedBy;
    }

    @Override
    public Object copy(CopyBuilder copyBuilder) {
        return copyTo(new ADEGenericComponent(), copyBuilder);
    }

    @Override
    public Object copyTo(Object target, CopyBuilder copyBuilder) {
        ADEGenericComponent copy = (target == null) ? new ADEGenericComponent() : (ADEGenericComponent) target;
        super.copyTo(copy, copyBuilder);

        if (isSetClazz())
            copy.setClazz((Code) copyBuilder.copy(clazz));

        if (isSetFunction()) {
            for (Code part : function) {
                Code copyPart = (Code) copyBuilder.copy(part);
                copy.addFunction(copyPart);

                if (part != null && copyPart == part)
                    part.setParent(this);
            }
        }

        if (isSetUsage()) {
            for (Code part : usage) {
                Code copyPart = (Code) copyBuilder.copy(part);
                copy.addUsage(copyPart);

                if (part != null && copyPart == part)
                    part.setParent(this);
            }
        }

        if (isSetLod1MultiSurface()) {
            copy.setLod1MultiSurface((MultiSurfaceProperty) copyBuilder.copy(lod1MultiSurface));
            if (copy.getLod1MultiSurface() == lod1MultiSurface)
                lod1MultiSurface.setParent(this);
        }

        if (isSetGenericApplicationPropertyOfADEGenericComponent()) {
            for (ADEComponent part : ade) {
                ADEComponent copyPart = (ADEComponent) copyBuilder.copy(part);
                copy.addGenericApplicationPropertyOfADEGenericComponent(copyPart);

                if (part != null && copyPart == part)
                    part.setParent(this);
            }
        }

        copy.nodeName = nodeName;

        return copy;
    }

    @Override
    public void accept(FeatureVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept'");
    }

    @Override
    public <T> T accept(FeatureFunctor<T> visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept'");
    }

    @Override
    public void accept(GMLVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept'");
    }

    @Override
    public <T> T accept(GMLFunctor<T> visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept'");
    }

    private MultiSurfaceProperty createMultiSurfaceProperty(Node node) {
        var childNodes = node.getFirstChild().getChildNodes(); // gml:MultiSurface/gml:surfaceMember
        var multiSurface = new MultiSurface();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("gml:surfaceMember")) {
                multiSurface.addSurfaceMember(createSurfaceProperty(childNode));
            }
        }

        return new MultiSurfaceProperty(multiSurface);
    }

    private SurfaceProperty createSurfaceProperty(Node node) {
        var childNode = node.getFirstChild(); // gml:Polygon
        var polygon = new Polygon();
        polygon.setExterior(createExterior(childNode));

        return new SurfaceProperty(polygon);
    }

    private AbstractRingProperty createExterior(Node node) {
        var childNode = node.getFirstChild(); // gml:exterior
        var exterior = new Exterior(createLinearRing(childNode));

        return exterior;
    }

    private LinearRing createLinearRing(Node node) {
        var childNode = node.getFirstChild(); // gml:LinearRing
        var linearRing = new LinearRing();
        linearRing.setPosList(createDirectPositionList(childNode));

        return linearRing;
    }

    private DirectPositionList createDirectPositionList(Node node) {
        var childNode = node.getFirstChild(); // gml:posList
        var posList = childNode.getTextContent().split(" ");
        var directPositionList = new DirectPositionList();
        for (var i = 0; i < posList.length; i++) {
            directPositionList.addValue(Double.parseDouble(posList[i]));
        }

        return directPositionList;
    }
}
