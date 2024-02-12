package org.plateau.citygmleditor.utils3d.polygonmesh;

import org.plateau.citygmleditor.utils3d.geom.Vec2f;

import java.util.ArrayList;
import java.util.List;

/**
 * ポリゴンメッシュのテクスチャ座標(UV)の配列を保持し追加、削除、取得を行う機能を提供します。
 * テクスチャ座標データは内部的には{@code ArrayList<Float>}で保持され、各座標はx, yの2要素で定義されます。
 */
public class TexCoordBuffer {
    private final List<Float> buffer = new ArrayList<>();

    /**
     * 内部保持されている生データを取得します。
     */
    public List<Float> getBuffer() {
        return buffer;
    }

    /**
     * 内部保持されている生データを配列に変換して取得します。
     */
    public float[] getBufferAsArray() {
        var result = new float[buffer.size()];
        var index = 0;
        for (var value : buffer) {
            result[index++] = value;
        }
        return result;
    }

    /**
     * 内部保持されている生データを配列に変換して取得します。
     */
    public float[] getBufferAsArray(boolean flip) {
        var result = new float[buffer.size()];

        if (!flip) {
            var index = 0;
            for (var value : buffer) {
                result[index++] = value;
            }
            return result;
        }

        for (int i = 0; i < buffer.size(); i += 2) {
            result[i] = buffer.get(i);
            result[i + 1] = 1.0f - buffer.get(i + 1);
        }
        return result;
    }

    /**
     * {@code getTexCoord}関数で扱えるインデックス数を取得します。
     */
    public int getTexCoordCount() {
        return buffer.size() / 2;
    }

    public Vec2f getTexCoord(int index) {
        return new Vec2f(buffer.get(index * 2), buffer.get(index * 2 + 1));
    }

    public Vec2f getTexCoord(int index, boolean flip) {
        return flip
                ? new Vec2f(buffer.get(index * 2), 1.0f - buffer.get(index * 2 + 1))
                : new Vec2f(buffer.get(index * 2), buffer.get(index * 2 + 1));
    }

    /**
     * テクスチャ座標を追加します。
     */
    public void addTexCoord(Vec2f texCoord, boolean flip) {
        buffer.add(texCoord.x);
        buffer.add(flip ? 1.0f - texCoord.y : texCoord.y);
    }

    /**
     * テクスチャ座標を追加します。
     */
    public void addTexCoords(float[] texCoords, boolean flip) {
        if (!flip) {
            for (var value : texCoords) {
                buffer.add(value);
            }
            return;
        }

        for (int i = 0; i < texCoords.length; i += 2) {
            var texCoord = new Vec2f(texCoords[i], texCoords[i + 1]);
            addTexCoord(texCoord, true);
        }
    }

    /**
     * テクスチャ座標を追加します。
     */
    public void addTexCoords(List<Float> texCoords, boolean flip) {
        if (!flip) {
            buffer.addAll(texCoords);
            return;
        }

        for (int i = 0; i < texCoords.size(); i += 2) {
            var texCoord = new Vec2f(texCoords.get(i), texCoords.get(i + 1));
            addTexCoord(texCoord, true);
        }
    }
}
