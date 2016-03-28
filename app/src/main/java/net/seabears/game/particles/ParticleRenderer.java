package net.seabears.game.particles;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Camera;
import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.render.Renderer;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.util.ViewMatrix;

public class ParticleRenderer implements Renderer {
  private final ParticleShader shader;
  private final RawModel quad;
  private final float[] vertices;

  public ParticleRenderer(Loader loader, ParticleShader shader, Matrix4f projectionMatrix, float size) {
    this.vertices = new float[] {-size, size, -size, -size, size, size, size, -size};
    this.quad = loader.loadToVao(vertices, 2, null);
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public ParticleShader getShader() {
    return shader;
  }

  public void render(Map<ParticleTexture, List<Particle>> particles, Camera camera) {
    if (particles.isEmpty()) {
      return;
    }

    final Matrix4f viewMatrix = new ViewMatrix(camera).toMatrix();
    shader.start();
    GL30.glBindVertexArray(quad.getVaoId());
    GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_POSITION);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDepthMask(false);
    for (Map.Entry<ParticleTexture, List<Particle>> entry : particles.entrySet()) {
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, entry.getKey().getBlendFunc());
//      final TexturedModel model = entry.getKey();
//      final RawModel rawModel = model.getRawModel();
//      final ModelTexture texture = model.getTexture();
//      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
//      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
//      if (texture.isTransparent()) {
//        MasterRenderer.disableCulling();
//      }
//      shader.loadTexture(model.getTexture());
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, entry.getKey().getTextureId());
      for (Particle particle : entry.getValue()) {
        shader.loadParticle(particle, viewMatrix);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
      }
//      if (texture.isTransparent()) {
//        MasterRenderer.enableCulling();
//      }
//      GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
//      GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
    }
    GL11.glDepthMask(true);
    GL11.glDisable(GL11.GL_BLEND);
    GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_POSITION);
    GL30.glBindVertexArray(0);
    shader.stop();
  }

  @Override
  public void close() {
    shader.close();
  }
}
