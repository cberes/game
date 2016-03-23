package net.seabears.game.render;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import de.matthiasmann.twl.utils.PNGDecoder;
import net.seabears.game.models.RawModel;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.util.ModelData;

public class Loader implements AutoCloseable {
  private final List<Integer> vaos = new ArrayList<>();
  private final List<Integer> vbos = new ArrayList<>();
  private final List<Integer> textures = new ArrayList<>();

  public RawModel loadToVao(ModelData data) {
      return loadToVao(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());
  }

  public RawModel loadToVao(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
    final int vaoId = createVao();
    vaos.add(vaoId);
    bindIndicesBuffer(indices);
    storeDataInAttributeList(StaticShader.ATTR_POSITION, 3, positions);
    storeDataInAttributeList(StaticShader.ATTR_TEXTURE, 2, textureCoords);
    storeDataInAttributeList(StaticShader.ATTR_NORMAL, 3, normals);
    unbindVao(); // VAO remains bound until here
    return new RawModel(vaoId, indices.length);
  }

  public int loadTexture(String filename) {
    ByteBuffer buf = null;
    int tWidth = 0;
    int tHeight = 0;

    try {
      // Open the PNG file as an InputStream
      InputStream in = new FileInputStream("src/main/res/" + filename + ".png");
      // Link the PNG decoder to this stream
      PNGDecoder decoder = new PNGDecoder(in);

      // Get the width and height of the texture
      tWidth = decoder.getWidth();
      tHeight = decoder.getHeight();

      // Decode the PNG file in a ByteBuffer
      buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
      decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
      buf.flip();

      in.close();
    } catch (IOException e) {
      System.err.println("Could not load image");
      e.printStackTrace();
      System.exit(4);
    }

    // Create a new texture object in memory and bind it
    final int texId = GL11.glGenTextures();
    textures.add(texId);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

    // All RGB bytes are aligned to each other and each component is 1 byte
    GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

    // Upload the texture data and generate mip maps (for scaling)
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
    GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
    // lessen the mip-mapping
    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -1.0f);
    return texId;
  }

  private int createVao() {
    final int vaoId = GL30.glGenVertexArrays();
    GL30.glBindVertexArray(vaoId);
    return vaoId;
  }

  private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
    final int vboId = GL15.glGenBuffers();
    vbos.add(vboId);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
    final FloatBuffer buffer = storeDataInFloatBuffer(data);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }

  private void unbindVao() {
    GL30.glBindVertexArray(0);
  }

  private void bindIndicesBuffer(int[] indices) {
    final int vboId = GL15.glGenBuffers();
    vbos.add(vboId);
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
    final IntBuffer buffer = storeDataInIntBuffer(indices);
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
  }

  private IntBuffer storeDataInIntBuffer(int[] data) {
    final IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
    buffer.put(data);
    buffer.flip(); // prepare buffer for reads
    return buffer;
  }

  private FloatBuffer storeDataInFloatBuffer(float[] data) {
    final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
    buffer.put(data);
    buffer.flip(); // prepare buffer for reads
    return buffer;
  }

  @Override
  public void close() {
    vaos.forEach(GL30::glDeleteVertexArrays);
    vbos.forEach(GL15::glDeleteBuffers);
    textures.forEach(GL11::glDeleteTextures);
  }
}
