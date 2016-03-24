package net.seabears.game.terrains;

import java.awt.image.BufferedImage;

import org.joml.Vector3f;

import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.textures.TerrainTexture;
import net.seabears.game.textures.TerrainTexturePack;

public class Terrain {
  private static final double HALF_MAX_PIXEL_COLOR = Math.pow(2, 23);

  private final float size;
  private final double maxHeight;
  private final float x, z;
  private final RawModel model;
  private final TerrainTexturePack texture;
  private final TerrainTexture blendMap;

  public Terrain(float x, float z, Loader loader, TerrainTexturePack texture, TerrainTexture blendMap, BufferedImage heightMap) {
    this(800, 40, x, z, loader, texture, blendMap, heightMap);
  }

  public Terrain(float size, double maxHeight, float x, float z, Loader loader, TerrainTexturePack texture, TerrainTexture blendMap, BufferedImage heightMap) {
    this.size = size;
    this.maxHeight = maxHeight;
    this.x = x * size;
    this.z = z * size;
    this.model = generateTerrain(loader, heightMap);
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

  private final RawModel generateTerrain(Loader loader, BufferedImage heightMap) {
    final int vertexCount = heightMap.getHeight();
    final int count = vertexCount * vertexCount;
    float[] vertices = new float[count * 3];
    float[] normals = new float[count * 3];
    float[] textureCoords = new float[count * 2];
    int[] indices = new int[6 * (vertexCount - 1) * (vertexCount - 1)];
    int vertexPointer = 0;
    for (int i = 0; i < vertexCount; i++) {
      for (int j = 0; j < vertexCount; j++) {
        vertices[vertexPointer * 3] = (float) j / ((float) vertexCount - 1) * size;
        vertices[vertexPointer * 3 + 1] = getHeight(j, i, heightMap);
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
