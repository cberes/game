package net.seabears.game.terrains;

import java.util.List;
import java.util.Optional;

import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.textures.TerrainTexture;
import net.seabears.game.textures.TerrainTexturePack;
import net.seabears.game.util.Barycentric;
import net.seabears.game.util.Tile;

public class Terrain extends Tile {
  private final float size;
  private final float x, z;
  private final RawModel model;
  private final TerrainTexturePack texture;
  private final TerrainTexture blendMap;
  private final float[][] heights;

  public Terrain(float x, float z, Loader loader, TerrainTexturePack texture, TerrainTexture blendMap, HeightGenerator heightGen) {
    this(800, x, z, loader, texture, blendMap, heightGen);
  }

  public Terrain(float size, float x, float z, Loader loader, TerrainTexturePack texture, TerrainTexture blendMap, HeightGenerator heightGen) {
    super(new Vector3f(x * size + (size * 0.5f), 0.0f, z * size + (size * 0.5f)), new Vector3f(size, 0.0f, size));
    this.size = size;
    this.x = x * size;
    this.z = z * size;
    final int vertexCount = heightGen.getVertexCount();
    this.heights = new float[vertexCount][vertexCount];
    this.model = generateTerrain(loader, heightGen, vertexCount);
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

  public static Optional<Terrain> find(List<Terrain> terrains, final float x, final float z) {
    return terrains.stream()
        .filter(t -> x - t.x >= 0 && x - t.x < t.size)
        .filter(t -> z - t.z >= 0 && z - t.z < t.size)
        .findFirst();
  }

  public static float getHeight(Optional<Terrain> terrain, final float x, final float z) {
    return terrain.flatMap(t -> Optional.of(t.getHeight(x, z))).orElse(0.0f);
  }

  public static float getHeight(List<Terrain> terrains, final float x, final float z) {
    return getHeight(find(terrains, x, z), x, z);
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

  private final RawModel generateTerrain(Loader loader, HeightGenerator heightGen, int vertexCount) {
    final int count = vertexCount * vertexCount;
    float[] vertices = new float[count * 3];
    float[] normals = new float[count * 3];
    float[] textureCoords = new float[count * 2];
    int[] indices = new int[6 * (vertexCount - 1) * (vertexCount - 1)];
    int vertexPointer = 0;
    for (int i = 0; i < vertexCount; i++) {
      for (int j = 0; j < vertexCount; j++) {
        heights[j][i] = heightGen.generate(j, i);
        vertices[vertexPointer * 3] = (float) j / ((float) vertexCount - 1) * size;
        vertices[vertexPointer * 3 + 1] = heights[j][i];
        vertices[vertexPointer * 3 + 2] = (float) i / ((float) vertexCount - 1) * size;
        final Vector3f normal = getNormal(j, i, heightGen);
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
    return loader.loadToVao(vertices, textureCoords, normals, null, indices);
  }

  private Vector3f getNormal(int x, int z, HeightGenerator heightGen) {
    return new Vector3f(
        heightGen.generate(x - 1, z) - heightGen.generate(x + 1, z),
        2.0f,
        heightGen.generate(x, z - 1) - heightGen.generate(x, z + 1)).normalize();
  }
}
