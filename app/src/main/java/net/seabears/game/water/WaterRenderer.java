package net.seabears.game.water;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Light;
import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.util.TransformationMatrix;

public class WaterRenderer implements AutoCloseable {
  private final RawModel quad;
  private final WaterShader shader;
  private final WaterFrameBuffers fbs;
  private final int dudvMapId;
  private final int normalMapId;

  public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix, WaterFrameBuffers fbs, int dudvMapId, int normalMapId) {
    // Just x and z vertex positions here: y is set to 0 in vertex shader
    float[] vertices = {-1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1};
    this.quad = loader.loadToVao(vertices, 2);
    this.fbs = fbs;
    this.dudvMapId = dudvMapId;
    this.normalMapId = normalMapId;
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadWater();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public WaterShader getShader() {
    return shader;
  }

  public void render(List<WaterTile> water, List<Light> lights, Camera camera) {
    prepareRender(lights, camera);
    for (WaterTile tile : water) {
      shader.loadModelMatrix(new TransformationMatrix(new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), new Vector3f(), tile.getSize()).toMatrix());
      shader.loadTexture(tile.getWater());
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
    }
    unbind();
  }

  private void prepareRender(List<Light> lights, Camera camera) {
    shader.start();
    shader.loadLights(lights);
    shader.loadViewMatrix(camera);
    GL30.glBindVertexArray(quad.getVaoId());
    GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbs.getReflectionTexture());
    GL13.glActiveTexture(GL13.GL_TEXTURE1);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbs.getRefractionTexture());
    GL13.glActiveTexture(GL13.GL_TEXTURE2);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvMapId);
    GL13.glActiveTexture(GL13.GL_TEXTURE3);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMapId);
  }

  private void unbind() {
    GL20.glDisableVertexAttribArray(0);
    GL30.glBindVertexArray(0);
    shader.stop();
  }

  @Override
  public void close() {
    shader.close();
  }
}
