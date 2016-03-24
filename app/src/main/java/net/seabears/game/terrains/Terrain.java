package net.seabears.game.terrains;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.textures.TerrainTexture;
import net.seabears.game.textures.TerrainTexturePack;
import net.seabears.game.util.Barycentric;

public class Terrain {
  private static final double HALF_MAX_PIXEL_COLOR = Math.pow(2, 23);

  private final float size;
  private final double maxHeight;
  private final float x, z;
  private final RawModel model;
  private final TerrainTexturePack texture;
  private final TerrainTexture blendMap;
  private final float[][] heights;

  public Terrain(float x, float z, Loader loader, TerrainTexturePack texture, TerrainTexture blendMap, BufferedImage heightMap) {
    this(800, 40, x, z, loader, texture, blendMap, heightMap);
  }

  public Terrain(float size, double maxHeight, float x, float z, Loader loader, TerrainTexturePack texture, TerrainTexture blendMap, BufferedImage heightMap) {
    this.size = size;
    this.maxHeight = maxHeight;
    this.x = x * size;
    this.z = z * size;
    final int vertexCount = heightMap.getHeight();
    this.heights = new float[vertexCount][vertexCount];
    this.model = generateTerrain(loader, heightMap, vertexCount);
    this.texture = texture;
    this.blendMap = blendMap;
  }

  public float getX() {
    return x;
  }

  public float getZ() {
    return z;
  }

  public RawModel getModel() {
    return model;
  }

  public TerrainTexturePack getTexture() {
    return texture;
  }

  public TerrainTexture getBlendMap() {
    return blendMap;
  }

  public static float getHeight(List<Terrain> terrains, final float x, final float z) {
    return terrains.stream()
        .filter(t -> x - t.x >= 0 && x - t.x < t.size)
        .filter(t -> z - t.z >= 0 && z - t.z < t.size)
        .findFirst()
        .flatMap(t -> Optional.of(t.getHeight(x, z)))
        .orElse(0.0f);
  }

  public float getHeight(float x, float z) {
    final float tx = x - this.x;
    final float tz = z - this.z;
    final float gridSquareSize = (float) size / (heights.length - 1);
    final int gx = (int) Math.floor(tx / gridSquareSize);
    final int gz = (int) Math.floor(tz / gridSquareSize);
    if (gx < 0 || gx >= heights.length - 1 || gz < 0 || gz >= heights.length - 1) {
      return 0.0f;
    }
    final float xc = (tx % gridSquareSize) / gridSquareSize;
    final float zc = (tz % gridSquareSize) / gridSquareSize;
    if (xc <= (1 - zc)) {
      return Barycentric.get(new Vector3f(0, heights[gx][gz], 0),
                             new Vector3f(1, heights[gx + 1][gz], 0),
                             new Vector3f(0, heights[gx][gz + 1], 1),
                             new Vector2f(xc, zc));
    } else {
      return Barycentric.get(new Vector3f(1, heights[gx + 1][gz], 0),
                             new Vector3f(1, heights[gx + 1][gz + 1], 1),
                             new Vector3f(0, heights[gx][gz + 1], 1),
                             new Vector2f(xc, zc));
    }
  }

  private final RawModel generateTerrain(Loader loader, BufferedImage heightMap, int vertexCount) {
    final int count = vertexCount * vertexCount;
    float[] vertices = new float[count * 3];
    float[] normals = new float[count * 3];
    float[] textureCoords = new float[count * 2];
    int[] indices = new int[6 * (vertexCount - 1) * (vertexCount - 1)];
    int vertexPointer = 0;
    for (int i = 0; i < vertexCount; i++) {
      for (int j = 0; j < vertexCount; j++) {
        heights[j][i] = getHeight(j, i, heightMap);
        vertices[vertexPointer * 3] = (float) j / ((float) vertexCount - 1) * size;
        vertices[vertexPointer * 3 + 1] = heights[j][i];
        vertices[vertexPointer * 3 + 2] = (float) i / ((float) vertexCount - 1) * size;
        final Vector3f normal = getNormal(j, i, heightMap);
        normals[vertexPointer * 3] = normal.x;
        normals[vertexPointer * 3 + 1] = normal.y;
        normals[vertexPointer * 3 + 2] = normal.z;
        textureCoords[vertexPointer * 2] = (float) j / ((float) vertexCount - 1);
        textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) vertexCount - 1);
        vertexPointer++;
      }
    }
    int pointer = 0;
    for (int gz = 0; gz < vertexCount - 1; gz++) {
      for (int gx = 0; gx < vertexCount - 1; gx++) {
        int topLeft = (gz * vertexCount) + gx;
        int topRight = topLeft + 1;
        int bottomLeft = ((gz + 1) * vertexCount) + gx;
        int bottomRight = bottomLeft + 1;
        indices[pointer++] = topLeft;
        indices[pointer++] = bottomLeft;
        indices[pointer++] = topRight;
        indices[pointer++] = topRight;
        indices[pointer++] = bottomLeft;
        indices[pointer++] = bottomRight;
      }
    }
    return loader.loadToVao(vertices, textureCoords, normals, indices);
  }

  private Vector3f getNormal(int x, int y, BufferedImage heightMap) {
    return new Vector3f(
        getHeight(x - 1, y, heightMap) - getHeight(x + 1, y, heightMap),
        2.0f,
        getHeight(x, y - 1, heightMap) - getHeight(x, y + 1, heightMap)).normalize();
  }

  private float getHeight(int x, int y, BufferedImage heightMap) {
    if (x < 0 || x >= heightMap.getWidth() || y < 0 || y >= heightMap.getHeight()) {
      return 0.0f;
    }
    return (float) ((heightMap.getRGB(x, y) + HALF_MAX_PIXEL_COLOR) / HALF_MAX_PIXEL_COLOR * maxHeight);
  }
}
