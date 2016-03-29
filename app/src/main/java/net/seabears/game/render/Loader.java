package net.seabears.game.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import de.matthiasmann.twl.utils.PNGDecoder;
import net.seabears.game.entities.StaticShader;
import net.seabears.game.guis.fonts.creator.TextMeshData;
import net.seabears.game.models.RawModel;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.textures.TextureData;
import net.seabears.game.util.ModelData;

public class Loader implements AutoCloseable {
  private static final String RES_ROOT = "src/main/res/";
  private static final int FLOAT_IN_BYTES = Float.SIZE >> 3;
  private final List<Integer> vaos = new ArrayList<>();
  private final List<Integer> vbos = new ArrayList<>();
  private final List<Integer> textures = new ArrayList<>();

  public int emptyVbo(int floats) {
    final int vboId = GL15.glGenBuffers();
    vbos.add(vboId);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floats * FLOAT_IN_BYTES, GL15.GL_STREAM_DRAW);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    return vboId;
  }

  public void addInstancedAttribute(int vaoId, int vboId, int attribute, int dataSize, int instancedDataLength, int offset) {
    final int floatBytes = Float.SIZE >> 3;
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
    GL30.glBindVertexArray(vaoId);
    GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false, instancedDataLength * floatBytes, offset * floatBytes);
    GL33.glVertexAttribDivisor(attribute, 1);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // unbind VBO
    GL30.glBindVertexArray(0); // unbind VAO
  }

  public void updateVbo(int vboId, float[] data, FloatBuffer buffer) {
    buffer.clear();
    buffer.put(data);
    buffer.flip();
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * FLOAT_IN_BYTES, GL15.GL_STREAM_DRAW);
    GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // unbind VBO
  }

  public BufferedImage loadImage(String filename) throws IOException {
    return ImageIO.read(new File(RES_ROOT + filename + ".png"));
  }

  public RawModel loadToVao(ModelData data) {
      return loadToVao(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getTangents(), data.getIndices());
  }

  public RawModel loadToVao(TextMeshData data) {
      return loadToVao(data.getVertices(), 2, data.getTextureCoords());
  }

  public RawModel loadToVao(float[] positions, float[] textureCoords, float[] normals, float[] tangents, int[] indices) {
    final int vaoId = createVao();
    vaos.add(vaoId);
    bindIndicesBuffer(indices);
    storeDataInAttributeList(ShaderProgram.ATTR_POSITION, 3, positions);
    storeDataInAttributeList(ShaderProgram.ATTR_TEXTURE, 2, textureCoords);
    storeDataInAttributeList(ShaderProgram.ATTR_NORMAL, 3, normals);
    if (tangents != null) {
      storeDataInAttributeList(ShaderProgram.ATTR_TANGENT, 3, tangents);
    }
    unbindVao(); // VAO remains bound until here
    return new RawModel(vaoId, indices.length);
  }

  public RawModel loadToVao(float[] positions) {
    return loadToVao(positions, 2, null);
  }

  public RawModel loadToVao(float[] positions, int dimensions, float[] textureCoords) {
    final int vaoId = createVao();
    vaos.add(vaoId);
    storeDataInAttributeList(StaticShader.ATTR_POSITION, dimensions, positions);
    if (textureCoords != null) {
      storeDataInAttributeList(ShaderProgram.ATTR_TEXTURE, 2, textureCoords);
    }
    unbindVao();
    return new RawModel(vaoId, positions.length / dimensions);
  }

  public TextureData loadPng(String filename) throws IOException {
    // Open the PNG file as an InputStream
    try (InputStream in = new FileInputStream(RES_ROOT + filename + ".png")) {
      // Link the PNG decoder to this stream
      PNGDecoder decoder = new PNGDecoder(in);

      // Get the width and height of the texture
      final int tWidth = decoder.getWidth();
      final int tHeight = decoder.getHeight();

      // Decode the PNG file in a ByteBuffer
      final ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
      decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
      buf.flip();
      return new TextureData(buf, tWidth, tHeight);
    }
  }

  public int loadTexture(String filename) throws IOException {
    return loadTexture(filename, 4.0f);
  }

  private int initTexture(final int target) {
    // Create a new texture object in memory and bind it
    final int texId = GL11.glGenTextures();
    textures.add(texId);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(target, texId);
    return texId;
  }

  private void uploadTexture(final int target, final TextureData data) {
    // All RGB bytes are aligned to each other and each component is 1 byte
    GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

    // Upload the texture data
    GL11.glTexImage2D(target, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
  }

  private void optimizeTexture(final int target, final float anisotropicLevel) {
    // generate mip maps (for scaling)
    GL30.glGenerateMipmap(target);
    if (target == GL13.GL_TEXTURE_CUBE_MAP) {
      GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
      GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    } else {
      GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
    }

    // anisotropic filtering
    if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic && anisotropicLevel > 0.0f) {
      final float amount = Math.min(anisotropicLevel, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
      GL11.glTexParameterf(target, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
      GL11.glTexParameterf(target, GL14.GL_TEXTURE_LOD_BIAS, 0.0f);
    } else {
      // anisotropic filtering is not supported
      System.out.println("Anisotropic filtering is not supported");
      // lessen the mip-mapping
      GL11.glTexParameterf(target, GL14.GL_TEXTURE_LOD_BIAS, -1.0f);
    }
  }

  public int loadTexture(String filename, final float anisotropicLevel) throws IOException {
    final TextureData data = loadPng(filename);
    final int texId = initTexture(GL11.GL_TEXTURE_2D);
    uploadTexture(GL11.GL_TEXTURE_2D, data);
    optimizeTexture(GL11.GL_TEXTURE_2D, anisotropicLevel);
    return texId;
  }

  /**
   * Filenames must be in this order:<ol>
   * <li>[@link GL13#GL_TEXTURE_CUBE_MAP_POSITIVE_X}</li>
   * <li>[@link GL13#GL_TEXTURE_CUBE_MAP_NEGATIVE_X}</li>
   * <li>[@link GL13#GL_TEXTURE_CUBE_MAP_POSITIVE_Y}</li>
   * <li>[@link GL13#GL_TEXTURE_CUBE_MAP_NEGATIVE_Y}</li>
   * <li>[@link GL13#GL_TEXTURE_CUBE_MAP_POSITIVE_Z}</li>
   * <li>[@link GL13#GL_TEXTURE_CUBE_MAP_NEGATIVE_Z}</li>
   * </ul>
   */
  public int loadCubeMap(String... textureFiles) throws IOException {
    return loadCubeMap(4.0f, textureFiles);
  }

  public int loadCubeMap(float anisotropicLevel, String... textureFiles) throws IOException {
    final int texId = initTexture(GL13.GL_TEXTURE_CUBE_MAP);
    for (int i = 0; i < textureFiles.length; ++i) {
      final TextureData data = loadPng(textureFiles[i]);
      uploadTexture(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, data);
      optimizeTexture(GL13.GL_TEXTURE_CUBE_MAP, anisotropicLevel);
    }
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
