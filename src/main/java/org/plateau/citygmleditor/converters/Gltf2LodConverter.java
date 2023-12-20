package org.plateau.citygmleditor.converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import java.util.UUID;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.module.citygml.AppearanceModule;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurface;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;
import org.plateau.citygmleditor.citymodel.geometry.LOD2Solid;

import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SceneModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import javafx.scene.shape.TriangleMesh;

public class Gltf2LodConverter {
    private static Pattern base64Pattern = Pattern.compile("^data:.+;base64,(?<value>.+)$");

    public static ILODSolid convert(String fileUrl) throws Exception {

        Path inputFile = Paths.get(fileUrl);
        GltfModelReader gltfModelReader = new GltfModelReader();
        GltfModel gltfModel = gltfModelReader.read(inputFile);

        CompositeSurface compositeSurface = new CompositeSurface();
        ArrayList<BoundarySurface> boundedBy = new ArrayList<BoundarySurface>();

        SceneModel sceneModel = gltfModel.getSceneModels().get(0);
        for (NodeModel nodeModel : sceneModel.getNodeModels()) {
            for (MeshModel meshModel : nodeModel.getMeshModels()) {
                var meshName = meshModel.getName();

                // MeshPrimitiveModel → Polygon
                Map<String, ParameterizedTexture> textureMap = new HashMap<>();
                for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
                    AccessorModel indices = meshPrimitiveModel.getIndices();

                    TriangleMesh triangleMesh = new TriangleMesh();
                    triangleMesh.getFaces().setAll(createPolygonFaces(indices.getAccessorData()));

                    Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
                    for (Entry<String, AccessorModel> entry : attributes.entrySet()) {
                        AccessorModel accessorModel = entry.getValue();
                        AccessorData accessorData = accessorModel.getAccessorData();
                        switch (entry.getKey()) {
                            case "POSITION":
                                triangleMesh.getPoints().setAll(createPolygonVertices(accessorData));
                                break;
                            case "TEXCOORD_0":
                                triangleMesh.getTexCoords().setAll(createUVs(accessorData));
                                break;
                            default:
                                break;
                        }
                    }

                    var extras = getExtras(meshPrimitiveModel);

                    // TODO:Triangle → Polygon
                    LinearRing linearRing = new LinearRing();
                    linearRing.setId(extras.getLinearRingId());
                    //linearRing.addCoord(null);

                    Exterior exterior = new Exterior(linearRing);
                    Polygon polygon = new Polygon();
                    polygon.setId(extras.getPolygonId());
                    polygon.setExterior(exterior);
                    compositeSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));

                    List<AbstractSurface> surfaces = new ArrayList<AbstractSurface>();
                    surfaces.add(polygon);
                    boundedBy.add(new BoundarySurface(createBoundarySurface(extras.getType(), extras.getSurfaceId(), surfaces)));

                    // TODO:MaterialModel → AppearanceMember
                    MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
                    ParameterizedTexture parameterizedTexture = createOrGetParameterizedTexture(materialModel, inputFile, textureMap);
                    if (parameterizedTexture == null) continue;

                    // TODO:Triangle → 画像の座標
                    TextureCoordinates textureCoordinates = new TextureCoordinates();
                    textureCoordinates.setRing(String.format("#%s", linearRing.getId()));
                    //textureCoordinates.setValue(null);

                    TexCoordList texCoordList = new TexCoordList();
                    texCoordList.addTextureCoordinates(textureCoordinates);

                    TextureAssociation textureAssociation = new TextureAssociation(texCoordList);
                    textureAssociation.setUri(String.format("#%s", polygon.getId()));
                    parameterizedTexture.addTarget(textureAssociation);

                    SurfaceDataProperty surfaceDataProperty = new SurfaceDataProperty(parameterizedTexture);
                    List<SurfaceDataProperty> surfaceDataMember = new ArrayList<SurfaceDataProperty>();
                    surfaceDataMember.add(surfaceDataProperty);

                    Appearance appearance = new Appearance(AppearanceModule.v2_0_0);
                    appearance.setSurfaceDataMember(surfaceDataMember);
                    AppearanceMember appearanceMember = new AppearanceMember(appearance);

                    // TODO: surfaceDataMemberも返さないとテクスチャが反映できない
                    // appearanceMemberまで返すかどうかは要検討
                }
            }
        }

        Solid solid = new Solid();
        solid.setExterior(new SurfaceProperty(compositeSurface));
        LOD2Solid lod2Solid = new LOD2Solid(solid);
        lod2Solid.setBoundaries(boundedBy);

        return lod2Solid;
    }

    private static int[] createPolygonFaces(AccessorData accessorData) {
        ByteBuffer byteBuffer = accessorData.createByteBuffer();
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        int[] faces = new int[shortBuffer.capacity() * 2];
        for (int i = 0; i < shortBuffer.capacity(); i += 3) {
            faces[i] = shortBuffer.get(i);
            faces[i + 1] = shortBuffer.get(i);
            faces[i + 2] = shortBuffer.get(i + 1);
            faces[i + 3] = shortBuffer.get(i + 1);
            faces[i + 4] = shortBuffer.get(i + 2);
            faces[i + 5] = shortBuffer.get(i + 2);
        }

        return faces;
    }

    private static float[] createPolygonVertices(AccessorData accessorData) {
        ByteBuffer byteBuffer = accessorData.createByteBuffer();
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        float[] vertices = new float[floatBuffer.capacity()];
        for (int i = 0; i < floatBuffer.capacity(); i += 3) {
            vertices[i] = floatBuffer.get(i + 2);
            vertices[i + 1] = floatBuffer.get(i);
            vertices[i + 2] = floatBuffer.get(i + 1);
        }

        return vertices;
    }

    private static float[] createUVs(AccessorData accessorData) {
        ByteBuffer byteBuffer = accessorData.createByteBuffer();
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        float[] uvs = new float[floatBuffer.capacity()];
        for (int i = 0; i < floatBuffer.capacity(); i += 2) {
            uvs[i] = floatBuffer.get(i);
            uvs[i + 1] = 1 - floatBuffer.get(i + 1);
        }

        return uvs;
    }

    private static Extras getExtras(MeshPrimitiveModel meshPrimitiveModel) {
        Map<String, String> extras = (Map<String, String>)meshPrimitiveModel.getExtras();
        if (extras == null) return new Extras();

        String type = extras.get("type");
        String surfaceId = extras.get("surfaceId");
        String polygonId = extras.get("polygonId");
        String linearRingId = extras.get("linearRingId");

        return new Extras(type, surfaceId, polygonId, linearRingId);
    }

    private static ParameterizedTexture createOrGetParameterizedTexture(MaterialModel materialModel, Path inputFile, Map<String, ParameterizedTexture> textureMap) {
        var materialName = materialModel.getName();
        if (textureMap.containsKey(materialName)) return textureMap.get(materialName);
        if (!(materialModel instanceof MaterialModelV2)) return null;

        MaterialModelV2 materialModelV2 = (MaterialModelV2)materialModel;
        TextureModel textureModel = materialModelV2.getBaseColorTexture();
        if (textureModel == null) return null;

        ImageModel imageModel = textureModel.getImageModel();
        String imageURI = null;
        var data = imageModel.getUri().toString();
        var matcher = base64Pattern.matcher(data);
        if (matcher.find()) {
            // 埋め込みだった場合は外部ファイルに出力する
            // TODO:ファイルの出力場所 (とりえあずgLTFと同じ場所に出力している)
            var appearanceFile = new File(inputFile.getParent().toFile().getAbsolutePath(), materialName);
            try (FileOutputStream stream = new FileOutputStream(appearanceFile, false)) {
                stream.write(Base64.getDecoder().decode(matcher.group("value")));
                imageURI = appearanceFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            imageURI = data;
        }

        ParameterizedTexture parameterizedTexture = new ParameterizedTexture();
        parameterizedTexture.setImageURI(imageURI);
        parameterizedTexture.setMimeType(new Code(imageModel.getMimeType()));
        textureMap.put(materialName, parameterizedTexture);

        return parameterizedTexture;
    }

    private static AbstractBoundarySurface createBoundarySurface(String type, String id, List<? extends AbstractSurface> surfaces) {
        AbstractBoundarySurface boundarySurface = null;

        switch (type) {
            case "BUILDING_WALL_SURFACE":
                boundarySurface = new WallSurface();
                break;
            case "BUILDING_ROOF_SURFACE":
                boundarySurface = new RoofSurface();
                break;
            case "BUILDING_GROUND_SURFACE":
                boundarySurface = new GroundSurface();
                break;
            default:
                throw new IllegalArgumentException("Unsupported boundary surface type: " + type);
        }

        boundarySurface.setId(id);
        boundarySurface.setLod2MultiSurface(new MultiSurfaceProperty(new MultiSurface(surfaces)));

        return boundarySurface;
    }

    private static class Extras {
        private String type;
        private String surfaceId;
        private String polygonId;
        private String linearRingId;

        public Extras() {
            this.surfaceId = UUID.randomUUID().toString();
            this.polygonId = UUID.randomUUID().toString();
            this.linearRingId = UUID.randomUUID().toString();
        }

        public Extras(String type, String surfaceId, String polygonId, String linearRingId) {
            this.type = type;
            this.surfaceId = surfaceId;
            this.polygonId = polygonId;
            this.linearRingId = linearRingId;
        }

        public String getType() {
            return type;
        }

        public String getSurfaceId() {
            return surfaceId;
        }

        public String getPolygonId() {
            return polygonId;
        }

        public String getLinearRingId() {
            return linearRingId;
        }
    }
}
