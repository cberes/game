package net.seabears.game.water;

import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.StaticShader;
import net.seabears.game.models.RawModel;
import net.seabears.game.render.DisplayManager;
import net.seabears.game.render.Loader;
import net.seabears.game.render.Renderer;
import net.seabears.game.util.TransformationMatrix;

public class WaterRenderer implements Renderer {
  private final RawModel quad;
  private final WaterShader shader;
  private final WaterFrameBuffers fbs;
  private final int dudvMapId;
  private final int normalMapId;

  public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix, WaterFrameBuffers fbs, int dudvMapId, int normalMapId, float nearPlane, float farPlane) {
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
    this.shader.loadPlanes(nearPlane, farPlane);
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public WaterShader getShader() {
    return shader;
  }

  public void preRender(List<WaterTile> water, List<Light> lights, Camera camera, DisplayManager display, Consumer<Vector4f> renderAction) {
    // water
    if (!water.isEmpty()) {
      // water reflection
      fbs.bindReflection();
      final float distance = 2.0f * (camera.getPosition().y - water.get(0).getHeight());
      camera.moveForReflection(distance);
      renderAction.accept(water.get(0).toReflectionPlane());
      camera.undoReflectionMove(distance);

      // water refraction
      fbs.bindRefraction();
      renderAction.accept(water.get(0).toRefractionPlane());

      // unbind the buffer
      fbs.unbind(display.getWidth(), display.getHeight());
    }
  }

  public void render(List<WaterTile> water, List<Light> lights, Camera camera) {
    if (!water.isEmpty()) {
      prepareRender(lights, camera);
      for (WaterTile tile : water) {
        shader.loadModelMatrix(new TransformationMatrix(new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), new Vector3f(), tile.getSize()).toMatrix());
        shader.loadTexture(tile.getWater());
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
      }
      unbind();
    }
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
    GL13.glActiveTexture(GL13.GL_TEXTURE4);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbs.getRefractionDepthTexture());
    // enable alpha-blending
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
  }

  private void unbind() {
    GL11.glDisable(GL11.GL_BLEND);
    GL20.glDisableVertexAttribArray(0);
    GL30.glBindVertexArray(0);
    shader.stop();
  }

  @Override
  public void close() {
    fbs.close();
    shader.close();
  }
}
