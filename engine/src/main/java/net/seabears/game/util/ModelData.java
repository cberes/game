package net.seabears.game.util;

public class ModelData {
  private final float[] vertices;
  private final float[] textureCoords;
  private final float[] normals;
  private final float[] tangents;
  private final int[] indices;
  private final float furthestPoint;

  public ModelData(float[] vertices, float[] textureCoords, float[] normals, int[] indices, float furthestPoint) {
    this(vertices, textureCoords, normals, null, indices, furthestPoint);
  }

  public ModelData(float[] vertices, float[] textureCoords, float[] normals, float[] tangents,
      int[] indices, float furthestPoint) {
    this.vertices = vertices;
    this.textureCoords = textureCoords;
    this.normals = normals;
    this.indices = indices;
    this.furthestPoint = furthestPoint;
    this.tangents = tangents;
  }

  public float[] getVertices() {
    return vertices;
  }

  public float[] getTextureCoords() {
    return textureCoords;
  }

  public float[] getTangents() {
    return tangents;
  }

  public float[] getNormals() {
    return normals;
  }

  public int[] getIndices() {
    return indices;
  }

  public float getFurthestPoint() {
    return furthestPoint;
  }

}
