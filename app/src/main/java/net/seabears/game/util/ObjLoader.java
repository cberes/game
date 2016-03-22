package net.seabears.game.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.Loader;
import net.seabears.game.RawModel;

public final class ObjLoader {
  public static RawModel loadObjModel(String filename, Loader loader) {
    List<String> lines = null;
    try {
      lines = Files.readAllLines(Paths.get("src/main/res/" + filename + ".obj"), StandardCharsets.UTF_8);
    } catch (IOException e) {
      System.err.println("Could not load model.");
      e.printStackTrace();
      System.exit(5);
    }
    final List<Vector3f> vertices = new ArrayList<>();
    final List<Vector2f> textures = new ArrayList<>();
    final List<Vector3f> normals = new ArrayList<>();
    final List<Integer> indices = new ArrayList<>();
    float[] verticesArray = null;
    float[] texturesArray = null;
    float[] normalsArray = null;
    int[] indicesArray = null;
    for (String line : lines) {
      final String[] current = line.split(" ");
      try {
        if (line.startsWith("v ")) {
          vertices.add(toVector3f(current));
        } else if (line.startsWith("vt ")) {
          textures.add(toVector2f(current));
        } else if (line.startsWith("vn ")) {
          normals.add(toVector3f(current));
        } else if (line.startsWith("f ")) {
          if (texturesArray == null) {
            texturesArray = new float[vertices.size() * 2];
            normalsArray = new float[vertices.size() * 3];
          }

          for (int i = 1; i <= 3; ++i) {
            processVertex(current[i], indices, textures, normals, texturesArray, normalsArray);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    verticesArray = new float[vertices.size() * 3];
    int vertexPointer = 0;
    for (Vector3f vertex : vertices) {
      verticesArray[vertexPointer++] = vertex.x;
      verticesArray[vertexPointer++] = vertex.y;
      verticesArray[vertexPointer++] = vertex.z;
    }
    indicesArray = new int[indices.size()];
    for (int i = 0; i < indices.size(); i++) {
        indicesArray[i] = indices.get(i);
    }
    return loader.loadToVao(verticesArray, texturesArray, normalsArray, indicesArray);
  }

  private static void processVertex(String vertex, List<Integer> indices, List<Vector2f> textures, List<Vector3f> normals, float[] texturesArray, float[] normalsArray) {
    String[] vertexData = vertex.split("/");
    int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
    indices.add(currentVertexPointer);
    Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
    texturesArray[currentVertexPointer * 2] = currentTex.x;
    texturesArray[currentVertexPointer * 2 + 1] = 1 - currentTex.y;
    Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
    normalsArray[currentVertexPointer * 3] = currentNorm.x;
    normalsArray[currentVertexPointer * 3 + 1] = currentNorm.y;
    normalsArray[currentVertexPointer * 3 + 2] = currentNorm.z;
  }

  private static Vector2f toVector2f(String[] line) {
    return new Vector2f(
        Float.parseFloat(line[1]),
        Float.parseFloat(line[2])
        );
  }

  private static Vector3f toVector3f(String[] line) {
    return new Vector3f(
        Float.parseFloat(line[1]),
        Float.parseFloat(line[2]),
        Float.parseFloat(line[3])
        );
  }
}
