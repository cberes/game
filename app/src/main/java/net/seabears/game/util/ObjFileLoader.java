package net.seabears.game.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

public final class ObjFileLoader {
    private static final String MODEL_ROOT = "src/main/res/";

    public static ModelData load(String objFileName) throws IOException {
        final String path = MODEL_ROOT + objFileName + ".obj";
        FileReader isr = new FileReader(new File(path));
        final BufferedReader reader = new BufferedReader(isr);
        final List<Vertex> vertices = new ArrayList<Vertex>();
        final List<Vector2f> textures = new ArrayList<Vector2f>();
        final List<Vector3f> normals = new ArrayList<Vector3f>();
        final List<Integer> indices = new ArrayList<Integer>();
        String line;
        while (true) {
            line = reader.readLine();
            if (line.startsWith("v ")) {
                String[] currentLine = line.split(" ");
                Vector3f vertex = toVector3f(currentLine);
                Vertex newVertex = new Vertex(vertices.size(), vertex);
                vertices.add(newVertex);
            } else if (line.startsWith("vt ")) {
                String[] currentLine = line.split(" ");
                Vector2f texture = toVector2f(currentLine);
                textures.add(texture);
            } else if (line.startsWith("vn ")) {
                String[] currentLine = line.split(" ");
                Vector3f normal = toVector3f(currentLine);
                normals.add(normal);
            } else if (line.startsWith("f ")) {
                break;
            }
        }
        while (line != null && line.startsWith("f ")) {
            String[] currentLine = line.split(" ");
            String[] vertex1 = currentLine[1].split("/");
            String[] vertex2 = currentLine[2].split("/");
            String[] vertex3 = currentLine[3].split("/");
            processVertex(vertex1, vertices, indices);
            processVertex(vertex2, vertices, indices);
            processVertex(vertex3, vertices, indices);
            line = reader.readLine();
        }
        reader.close();
        removeUnusedVertices(vertices);
        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];
        float furthest = convertDataToArrays(vertices, textures, normals, verticesArray,
                texturesArray, normalsArray);
        int[] indicesArray = listToUnboxedArray(indices);
        return new ModelData(verticesArray, texturesArray, normalsArray, indicesArray, furthest);
    }

    private static void processVertex(String[] vertex, List<Vertex> vertices, List<Integer> indices) {
        int index = Integer.parseInt(vertex[0]) - 1;
        Vertex currentVertex = vertices.get(index);
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        if (!currentVertex.isSet()) {
            currentVertex.setTextureIndex(textureIndex);
            currentVertex.setNormalIndex(normalIndex);
            indices.add(index);
        } else {
            dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
        }
    }

    private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
            List<Vector3f> normals, float[] verticesArray, float[] texturesArray, float[] normalsArray) {
        float furthestPoint = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Vertex currentVertex = vertices.get(i);
            furthestPoint = Math.max(furthestPoint, currentVertex.getLength());
            Vector3f position = currentVertex.getPosition();
            Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
            Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
            verticesArray[i * 3] = position.x;
            verticesArray[i * 3 + 1] = position.y;
            verticesArray[i * 3 + 2] = position.z;
            texturesArray[i * 2] = textureCoord.x;
            texturesArray[i * 2 + 1] = 1 - textureCoord.y;
            normalsArray[i * 3] = normalVector.x;
            normalsArray[i * 3 + 1] = normalVector.y;
            normalsArray[i * 3 + 2] = normalVector.z;
        }
        return furthestPoint;
    }

    private static void dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex,
            int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            indices.add(previousVertex.getIndex());
        } else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null) {
                dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex,
                        indices, vertices);
            } else {
                Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
                duplicateVertex.setTextureIndex(newTextureIndex);
                duplicateVertex.setNormalIndex(newNormalIndex);
                previousVertex.setDuplicateVertex(duplicateVertex);
                vertices.add(duplicateVertex);
                indices.add(duplicateVertex.getIndex());
            }
        }
    }

    private static void removeUnusedVertices(List<Vertex> vertices) {
        vertices.stream().filter(v -> !v.isSet()).forEach(v -> {
            v.setTextureIndex(0);
            v.setNormalIndex(0);
        });
    }

    private static Vector2f toVector2f(String[] line) {
        return new Vector2f(Float.parseFloat(line[1]), Float.parseFloat(line[2]));
    }

    private static Vector3f toVector3f(String[] line) {
        return new Vector3f(Float.parseFloat(line[1]), Float.parseFloat(line[2]), Float.parseFloat(line[3]));
    }

    private static int[] listToUnboxedArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private ObjFileLoader() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }
}
