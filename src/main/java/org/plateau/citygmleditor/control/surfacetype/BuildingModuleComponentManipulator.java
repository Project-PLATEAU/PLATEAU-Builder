package org.plateau.citygmleditor.control.surfacetype;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;

import java.util.*;
import java.util.stream.Collectors;

public class BuildingModuleComponentManipulator {
    private final AbstractBuilding building;
    private final int lod;

    public BuildingModuleComponentManipulator(AbstractBuilding building, int lod) {
        this.building = building;
        this.lod = lod;
    }

    public Map<AbstractSurface, BuildingModuleComponent> getPropertyMap() {
        var map = new HashMap<AbstractSurface, BuildingModuleComponent>();
        for (var boundedBy : building.getBoundedBySurface()) {
            var boundarySurface = boundedBy.getBoundarySurface();
            if (boundarySurface == null)
                continue;

            {
                var multiSurfaceProperty = getMultiSurfaceProperty(boundarySurface);
                if (multiSurfaceProperty != null) {
                    var surfaces = multiSurfaceProperty.getMultiSurface().getSurfaceMember();
                    for (var surface : surfaces) {
                        map.put(surface.getSurface(), boundedBy);
                    }
                }
            }

            for (var opening : boundarySurface.getOpening()) {
                var multiSurfaceProperty = getMultiSurfaceProperty(opening.getOpening());
                if (multiSurfaceProperty == null)
                    continue;

                var surfaces = multiSurfaceProperty.getMultiSurface().getSurfaceMember();
                for (var surface : surfaces) {
                    map.put(surface.getSurface(), opening);
                }
            }
        }
        return map;
    }

    public void reconstructWithSurfaces(List<AbstractSurface> surfaces, List<BuildingModuleComponent> srcProperties, BuildingModuleComponent dstProperty) {
        var originalSurfaces = new ArrayList<AbstractSurface>();
        if (dstProperty instanceof OpeningProperty) {
            for (var surface : getMultiSurfaceProperty(((OpeningProperty) dstProperty).getOpening()).getMultiSurface().getSurfaceMember()) {
                originalSurfaces.add(surface.getSurface());
            }
        } else {
            for (var surface : getMultiSurfaceProperty(((BoundarySurfaceProperty) dstProperty).getBoundarySurface()).getMultiSurface().getSurfaceMember()) {
                originalSurfaces.add(surface.getSurface());
            }
        }

        for (int i = 0; i < surfaces.size(); ++i) {
            var surface = surfaces.get(i);
            var srcProperty = srcProperties.get(i);

            if (srcProperty == dstProperty) {
                originalSurfaces.remove(surface);
                continue;
            }

            if (srcProperty instanceof OpeningProperty) {
                var openingProperty = (OpeningProperty) srcProperty;
                var parent = Objects.requireNonNull(findParentBoundarySurfaceProperty(openingProperty));
                removeSurfaceFromOpening(parent, openingProperty, surface);
            } else {
                var boundedBy = (BoundarySurfaceProperty) srcProperty;
                removeSurfaceFromBoundary(boundedBy, surface);
            }

            if (dstProperty instanceof OpeningProperty)
                getMultiSurfaceProperty(((OpeningProperty) dstProperty).getOpening()).getMultiSurface().getSurfaceMember().add(new SurfaceProperty(surface));
            else
                getMultiSurfaceProperty(((BoundarySurfaceProperty) dstProperty).getBoundarySurface()).getMultiSurface().getSurfaceMember().add(new SurfaceProperty(surface));
        }

        if (dstProperty instanceof OpeningProperty) {
            var openingProperty = (OpeningProperty) dstProperty;
            for (var surface : originalSurfaces) {
                moveSurfaceIntoNewOpening(openingProperty, openingProperty.getCityGMLClass(), surface);
            }
        } else {
            var boundedBy = (BoundarySurfaceProperty) dstProperty;
            for (var surface : originalSurfaces) {
                moveSurfaceIntoNewBoundedBy(boundedBy, boundedBy.getBoundarySurface().getCityGMLClass(), surface);
            }
        }
    }

    public void moveComponentsIntoOpening(List<BuildingModuleComponent> srcProperties, BoundarySurfaceProperty dstProperty) {
        for (var srcProperty : srcProperties) {
            if (srcProperty == dstProperty)
                continue;

            if (srcProperty instanceof BoundarySurfaceProperty) {
                var boundedBy = (BoundarySurfaceProperty) srcProperty;
                var openings = createWindowsForcibly(boundedBy);
                building.unsetBoundedBySurface(boundedBy);
                dstProperty.getBoundarySurface().getOpening().addAll(openings);
            } else if (srcProperty instanceof OpeningProperty) {
                var openingProperty = (OpeningProperty) srcProperty;
                var srcParent = Objects.requireNonNull(findParentBoundarySurfaceProperty(openingProperty));

                dstProperty.getBoundarySurface().getOpening().add(openingProperty);
                srcParent.getBoundarySurface().unsetOpening(openingProperty);
                cleanUp(srcParent);
            }
        }
    }

    private List<OpeningProperty> createWindowsForcibly(BoundarySurfaceProperty boundedBy) {
        var openings = new ArrayList<OpeningProperty>();

        var newOpening = (AbstractOpening) createCityObjectInstance(CityGMLClass.BUILDING_WINDOW);
        var oldBoundarySurface = boundedBy.getBoundarySurface();

        newOpening.setLod3MultiSurface(oldBoundarySurface.getLod3MultiSurface());
        newOpening.setLod4MultiSurface(oldBoundarySurface.getLod4MultiSurface());
        newOpening.setGenericApplicationPropertyOfOpening(oldBoundarySurface.getGenericApplicationPropertyOfBoundarySurface());

        openings.add(new OpeningProperty(newOpening));
        openings.addAll(boundedBy.getBoundarySurface().getOpening());

        return openings;
    }

    public void changeCityGMLClass(BoundarySurfaceProperty boundedBy, CityGMLClass clazz) {
        var newBoundarySurface = (AbstractBoundarySurface) createCityObjectInstance(clazz);
        var oldBoundarySurface = boundedBy.getBoundarySurface();

        newBoundarySurface.setOpening(oldBoundarySurface.getOpening());
        newBoundarySurface.setLod2MultiSurface(oldBoundarySurface.getLod2MultiSurface());
        newBoundarySurface.setLod3MultiSurface(oldBoundarySurface.getLod3MultiSurface());
        newBoundarySurface.setLod4MultiSurface(oldBoundarySurface.getLod4MultiSurface());
        newBoundarySurface.setGenericApplicationPropertyOfBoundarySurface(oldBoundarySurface.getGenericApplicationPropertyOfBoundarySurface());

        boundedBy.setBoundarySurface(newBoundarySurface);
    }

    public void changeCityGMLClass(OpeningProperty openingProperty, CityGMLClass clazz) {
        var newOpening = (AbstractOpening) createCityObjectInstance(clazz);
        var oldOpening = openingProperty.getOpening();

        newOpening.setLod3MultiSurface(oldOpening.getLod3MultiSurface());
        newOpening.setLod4MultiSurface(oldOpening.getLod4MultiSurface());
        newOpening.setGenericApplicationPropertyOfOpening(oldOpening.getGenericApplicationPropertyOfOpening());

        openingProperty.setOpening(newOpening);
    }

    public void moveSurfaceIntoNewOpening(OpeningProperty openingProperty, CityGMLClass newClazz, AbstractSurface surface) {
        var parent = Objects.requireNonNull(findParentBoundarySurfaceProperty(openingProperty));
        var surfaces = new ArrayList<AbstractSurface>();
        surfaces.add(surface);
        addNewOpening(parent.getBoundarySurface(), newClazz, surfaces);
        removeSurfaceFromOpening(parent, openingProperty, surface);
    }

    public void moveSurfaceIntoNewBoundedBy(BoundarySurfaceProperty boundedBy, CityGMLClass newClazz, AbstractSurface surface) {
        addNewBoundarySurface(newClazz, surface);
        removeSurfaceFromBoundary(boundedBy, surface);
    }

    /**
     * 建物に新しいboundedBy要素を追加し、surfaceを格納します。
     *
     * @param clazz 境界面のCityGMLクラスです。
     * @param surface 境界面として追加されるサーフェスです。
     * @return 新しく作成された境界面を返します。
     **/
    private AbstractBoundarySurface addNewBoundarySurface(CityGMLClass clazz, AbstractSurface surface) {
        var surfaces = new ArrayList<AbstractSurface>();
        surfaces.add(surface);
        return addNewBoundarySurface(clazz, surfaces);
    }

    /**
     * 建物に新しいboundedBy要素を追加し、surfacesを格納します。
     *
     * @param clazz 境界面のCityGMLクラスです。
     * @param surfaces 境界面として追加されるサーフェスです。
     * @return 新しく作成された境界面を返します。
     **/
    private AbstractBoundarySurface addNewBoundarySurface(CityGMLClass clazz, List<AbstractSurface> surfaces) {
        var newBoundarySurface = (AbstractBoundarySurface) createCityObjectInstance(clazz);
        setMultiSurfaceProperty(newBoundarySurface, new MultiSurfaceProperty(new MultiSurface(surfaces)));
        building.addBoundedBySurface(new BoundarySurfaceProperty(newBoundarySurface));
        return newBoundarySurface;
    }

    private void addNewOpening(AbstractBoundarySurface parentBoundarySurface, CityGMLClass clazz, List<AbstractSurface> surfaces) {
        var newOpening = (AbstractOpening) createCityObjectInstance(clazz);
        setMultiSurfaceProperty(newOpening, new MultiSurfaceProperty(new MultiSurface(surfaces)));
        parentBoundarySurface.getOpening().add(new OpeningProperty(newOpening));
    }

    private static String generateUUID() {
        return String.format("UUID_%s", UUID.randomUUID());
    }

    private AbstractCityObject createCityObjectInstance(CityGMLClass clazz) {
        try {
            var ctor = clazz.getModelClass().getDeclaredConstructor();
            var object = (AbstractCityObject) ctor.newInstance();
            object.setId(generateUUID());
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean cleanUp(MultiSurfaceProperty multiSurfaceProperty) {
        boolean isEmpty = true;
        if (multiSurfaceProperty == null)
            return isEmpty;

        var unusedSurfaceProperties = new ArrayList<SurfaceProperty>();
        for (var surfaceProperty : multiSurfaceProperty.getMultiSurface().getSurfaceMember()) {
            if (surfaceProperty.getSurface() != null) {
                isEmpty = false;
                continue;
            }

            unusedSurfaceProperties.add(surfaceProperty);
        }
        multiSurfaceProperty.getMultiSurface().getSurfaceMember().removeAll(unusedSurfaceProperties);

        return isEmpty;
    }

    private boolean cleanUp(OpeningProperty openingProperty) {
        var surfaceExists = false;
        var opening = openingProperty.getOpening();
        if (opening.isSetLod3MultiSurface()) {
            var isEmpty = cleanUp(opening.getLod3MultiSurface());
            if (isEmpty)
                opening.unsetLod3MultiSurface();
            else
                surfaceExists = true;
        }
        if (opening.isSetLod4MultiSurface()) {
            var isEmpty = cleanUp(opening.getLod4MultiSurface());
            if (isEmpty)
                opening.unsetLod4MultiSurface();
            else
                surfaceExists = true;
        }

        return !surfaceExists;
    }

    private void cleanUp(BoundarySurfaceProperty boundedBy) {
        var surfaceExists = false;
        var boundarySurface = boundedBy.getBoundarySurface();

        if (boundarySurface.isSetLod2MultiSurface()) {
            var isEmpty = cleanUp(boundarySurface.getLod2MultiSurface());
            if (isEmpty)
                boundarySurface.unsetLod2MultiSurface();
            else
                surfaceExists = true;
        }
        if (boundarySurface.isSetLod3MultiSurface()) {
            var isEmpty = cleanUp(boundarySurface.getLod3MultiSurface());
            if (isEmpty)
                boundarySurface.unsetLod3MultiSurface();
            else
                surfaceExists = true;
        }
        if (boundarySurface.isSetLod4MultiSurface()) {
            var isEmpty = cleanUp(boundarySurface.getLod4MultiSurface());
            if (isEmpty)
                boundarySurface.unsetLod4MultiSurface();
            else
                surfaceExists = true;
        }
        if (boundarySurface.isSetOpening()) {
            for (var opening : boundarySurface.getOpening()) {
                var isEmpty = cleanUp(opening);
                if (isEmpty)
                    boundarySurface.unsetOpening();
                else
                    surfaceExists = true;
            }
        }

        if (!surfaceExists)
            building.unsetBoundedBySurface(boundedBy);
    }

    private void removeSurfaceFromOpening(BoundarySurfaceProperty parentBoundaryProperty, OpeningProperty opening, AbstractSurface sectionSurface) {
        // ModuleComponentからsurfaceを削除
        var multiSurfaceProperty = getMultiSurfaceProperty(opening.getOpening());
        boolean surfaceRemoved = false;
        for (var surface : multiSurfaceProperty.getMultiSurface().getSurfaceMember()) {
            if (sectionSurface == surface.getSurface()) {
                multiSurfaceProperty.getMultiSurface().getSurfaceMember().remove(surface);
                surfaceRemoved = true;
                break;
            }
        }

        if (!surfaceRemoved)
            throw new RuntimeException("Surface does not exist.");

        cleanUp(parentBoundaryProperty);
    }

    private void removeSurfaceFromBoundary(BoundarySurfaceProperty boundarySurfaceProperty, AbstractSurface surface) {
        var multiSurfaceProperty = getMultiSurfaceProperty(boundarySurfaceProperty.getBoundarySurface());
        boolean surfaceRemoved = false;
        for (var surfaceMember : multiSurfaceProperty.getMultiSurface().getSurfaceMember()) {
            if (surface == surfaceMember.getSurface()) {
                multiSurfaceProperty.getMultiSurface().getSurfaceMember().remove(surfaceMember);
                surfaceRemoved = true;
                break;
            }
        }

        if (!surfaceRemoved)
            throw new RuntimeException("Surface does not exist.");

        cleanUp(boundarySurfaceProperty);
    }

    private MultiSurfaceProperty getMultiSurfaceProperty(AbstractOpening opening) {
        switch (lod) {
            case 2: return null;
            case 3: return opening.getLod3MultiSurface();
        }
        throw new OutOfRangeException(lod, 2, 3);
    }

    private MultiSurfaceProperty getMultiSurfaceProperty(AbstractBoundarySurface boundarySurface) {
        switch (lod) {
            case 2: return boundarySurface.getLod2MultiSurface();
            case 3: return boundarySurface.getLod3MultiSurface();
        }
        throw new OutOfRangeException(lod, 2, 3);
    }

    private void setMultiSurfaceProperty(AbstractBoundarySurface boundarySurface, MultiSurfaceProperty multiSurfaceProperty) {
        switch (lod) {
            case 2:
                boundarySurface.setLod2MultiSurface(multiSurfaceProperty);
                return;
            case 3:
                boundarySurface.setLod3MultiSurface(multiSurfaceProperty);
                return;
        }

        throw new OutOfRangeException(lod, 2, 3);
    }

    private void setMultiSurfaceProperty(AbstractOpening boundarySurface, MultiSurfaceProperty multiSurfaceProperty) {
        if (lod == 3) {
            boundarySurface.setLod3MultiSurface(multiSurfaceProperty);
            return;
        }

        throw new OutOfRangeException(lod, 3, 3);
    }

    private Solid getSolid() {
        switch (lod) {
            case 1: return (Solid)building.getLod1Solid().getSolid();
            case 2: return (Solid)building.getLod2Solid().getSolid();
            case 3: return (Solid)building.getLod3Solid().getSolid();
        }
        throw new OutOfRangeException(lod, 1, 3);
    }

    private BoundarySurfaceProperty findParentBoundarySurfaceProperty(OpeningProperty opening) {
        for (var boundary : building.getBoundedBySurface()) {
            if (boundary.getBoundarySurface().getOpening() == null)
                continue;

            for (var openingProperty : boundary.getBoundarySurface().getOpening()) {
                if (openingProperty == opening)
                    return boundary;
            }
        }

        return null;
    }
}
