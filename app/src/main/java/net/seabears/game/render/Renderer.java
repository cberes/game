package net.seabears.game.render;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Entity;
import net.seabears.game.models.RawModel;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.util.ProjectionMatrix;

public class Renderer {
  private static final float FOV = 70.0f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000.0f;

  private final Matrix4f projectionMatrix;
  private final StaticShader shader;

  public Renderer(int w, int h, StaticShader shader) {
    // don't render triangles facing away from the camera
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);

    this.shader = shader;
    projectionMatrix = new ProjectionMatrix(w, h, FOV, NEAR_PLANE, FAR_PLANE).toMatrix();
    shader.start();
    shader.loadProjectionMatrix(projectionMatrix);
    shader.stop();
  }

  public void prepare() {
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glClearColor(0.0f, 0.0f, 0.25f, 1.0f);
  }

  public void render(Map<TexturedModel, List<Entity>> entities) {
    for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
      final TexturedModel tmodel = entry.getKey();
      final RawModel model = tmodel.getRawModel();
      GL30.glBindVertexArray(model.getVaoId());
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_NORMAL);
      shader.loadShine(tmodel.getTexture().getReflectivity(), tmodel.getTexture().getShineDamper());
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, tmodel.getTexture().getTextureId());
      for (Entity entity : entry.getValue()) {
        shader.loadTransformationMatrix(entity);
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
      }
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_NORMAL);
      GL30.glBindVertexArray(0);
    }
  }
}
