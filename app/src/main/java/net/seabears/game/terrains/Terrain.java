package net.seabears.game.terrains;

import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.textures.ModelTexture;

public class Terrain {
  private final float size;
  private final int vertexCount;

  private final float x, z;
  private final RawModel model;
  private final ModelTexture texture;

  public Terrain(float x, float z, Loader loader, ModelTexture texture) {
    this(800, 128, x, z, loader, texture);
  }

  public Terrain(float size, int vertexCount, float x, float z, Loader loader, ModelTexture texture) {
    this.size = size;
    this.vertexCount = vertexCount;
    this.x = x * size;
    this.z = z * size;
    this.model = generateTerrain(loader);
    this.texture = texture;
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

  public ModelTexture getTexture() {
    return texture;
  }

  private final RawModel generateTerrain(Loader loader) {
    int count = vertexCount * vertexCount;
    float[] vertices = new float[count * 3];
    float[] normals = new float[count * 3];
    float[] textureCoords = new float[count * 2];
    int[] indices = new int[6 * (vertexCount - 1) * (vertexCount - 1)];
    int vertexPointer = 0;
    for (int i = 0; i < vertexCount; i++) {
      for (int j = 0; j < vertexCount; j++) {
        vertices[vertexPointer * 3] = (float) j / ((float) vertexCount - 1) * size;
        vertices[vertexPointer * 3 + 1] = 0;
        vertices[vertexPointer * 3 + 2] = (float) i / ((float) vertexCount - 1) * size;
        normals[vertexPointer * 3] = 0;
        normals[vertexPointer * 3 + 1] = 1;
        normals[vertexPointer * 3 + 2] = 0;
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
}
