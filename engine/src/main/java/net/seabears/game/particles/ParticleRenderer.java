package net.seabears.game.particles;

import static java.util.stream.IntStream.range;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.render.Renderer;
import net.seabears.game.util.InsertionSort;

public class ParticleRenderer implements Renderer {
  private static final int INSTANCE_FLOATS = 21;

  private final FloatBuffer buffer;
  private final int maxInstances;
  private final Loader loader;
  private final ParticleShader shader;
  private final float[] vertices;
  private final RawModel quad;
  private final int vboId;
  private final int attributes;
  private int pointer;

  public ParticleRenderer(Loader loader, ParticleShader shader, Matrix4f projectionMatrix, float size, int maxInstances) {
    this.maxInstances = maxInstances;
    this.vertices = new float[] {-size, size, -size, -size, size, size, size, -size};
    this.loader = loader;
    this.quad = loader.loadToVao(vertices, 2, null);
    this.vboId = loader.emptyVbo(INSTANCE_FLOATS * maxInstances);
    this.attributes = 1 + addInstancedAttributes(INSTANCE_FLOATS, 4);
    this.buffer = BufferUtils.createFloatBuffer(maxInstances * INSTANCE_FLOATS);
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  protected final int addInstancedAttributes(final int floats, final int floatsPerAttribute) {
    int floatsRemaining = floats;
    int i = 0;
    for (; floatsRemaining > 0; ++i) {
      final int dataSize = Math.min(floatsRemaining, floatsPerAttribute);
      floatsRemaining -= dataSize;
      loader.addInstancedAttribute(quad.getVaoId(), vboId, i + 1, dataSize, INSTANCE_FLOATS, i << 2);
    }
    return i;
  }

  public ParticleShader getShader() {
    return shader;
  }

  public void render(Map<ParticleTexture, List<Particle>> particles, Matrix4f viewMatrix) {
    if (particles.isEmpty()) {
      return;
    }

    // bind values that are the same for all particles
    shader.start();
    GL30.glBindVertexArray(quad.getVaoId());
    range(0, attributes).forEach(GL20::glEnableVertexAttribArray);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDepthMask(false);

    // iterate over types of particles
    for (Map.Entry<ParticleTexture, List<Particle>> entry : particles.entrySet()) {
      // checl that there's enough room for all the particles
      final int particleCount = entry.getValue().size();
      if (particleCount > maxInstances) {
        throw new IllegalStateException("Too many " + Particle.class + "! Allowed " + maxInstances + " but given " + particleCount);
      }

      // load texture data
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, entry.getKey().getBlendFunc());
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, entry.getKey().getTextureId());
      shader.loadTexture(entry.getKey());

      // sort so that farthest-away particles are first IF not using addditive blending
      if (entry.getKey().getBlendFunc() != GL11.GL_ONE) {
        InsertionSort.sortDescending(entry.getValue(), (a, b) -> (int) Math.signum(a.getDistance() - b.getDistance()));
      }

      // copy data to an array and load
      pointer = 0;
      final float[] vboData = new float[particleCount * INSTANCE_FLOATS];
      for (Particle particle : entry.getValue()) {
        storeMatrix(getModelViewMatrix(particle, viewMatrix), vboData);
        storeTextureOffsets(particle, vboData);
      }
      loader.updateVbo(vboId, vboData, buffer);

      // draw the particles
      GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), particleCount);
    }

    // restore settings
    GL11.glDepthMask(true);
    GL11.glDisable(GL11.GL_BLEND);
    range(0, attributes).forEach(GL20::glDisableVertexAttribArray);
    GL30.glBindVertexArray(0);
    shader.stop();
  }

  private Matrix4f getModelViewMatrix(Particle particle, Matrix4f viewMatrix) {
    // get model-view matrix, but transpose some elements so there is no rotation
    // this keeps particles always visible to the camera
    Matrix4f modelMatrix = new Matrix4f();
    modelMatrix.translate(particle.getPosition());
    modelMatrix.m00 = viewMatrix.m00;
    modelMatrix.m01 = viewMatrix.m10;
    modelMatrix.m02 = viewMatrix.m20;
    modelMatrix.m10 = viewMatrix.m01;
    modelMatrix.m11 = viewMatrix.m11;
    modelMatrix.m12 = viewMatrix.m21;
    modelMatrix.m20 = viewMatrix.m02;
    modelMatrix.m21 = viewMatrix.m12;
    modelMatrix.m22 = viewMatrix.m22;
    modelMatrix.rotate((float) Math.toRadians(particle.getRotation()), new Vector3f(0.0f, 0.0f, 1.0f));
    modelMatrix.scale(particle.getScale());
    return viewMatrix.mul(modelMatrix, new Matrix4f());
  }

  private void storeMatrix(Matrix4f matrix, float[] vboData) {
    matrix.get(vboData, pointer);
    pointer += 4 * 4;
  }

  private void storeTextureOffsets(Particle particle, float[] vboData) {
    vboData[pointer++] = particle.getTextureOffset().x;
    vboData[pointer++] = particle.getTextureOffset().y;
    vboData[pointer++] = particle.getTextureOffsetNext().x;
    vboData[pointer++] = particle.getTextureOffsetNext().x;
    vboData[pointer++] = particle.getBlend();
  }

  @Override
  public void close() {
    shader.close();
  }
}
