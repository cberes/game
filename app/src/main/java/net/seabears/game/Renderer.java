package net.seabears.game;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Entity;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.shaders.StaticTextureShader;
import net.seabears.game.util.ProjectionMatrix;

public class Renderer {
  private static final float FOV = 70.0f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000.0f;

  private final Matrix4f projectionMatrix;

  public Renderer(int w, int h, StaticTextureShader shader) {
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

  public void render(Entity entity, StaticTextureShader shader) {
    final TexturedModel tmodel = entity.getModel();
    final RawModel model = tmodel.getRawModel();
    GL30.glBindVertexArray(model.getVaoId());
    GL20.glEnableVertexAttribArray(0);
    GL20.glEnableVertexAttribArray(1);
    shader.loadTransformationMatrix(entity);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, tmodel.getTexture().getTextureId());
    GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
    GL20.glDisableVertexAttribArray(0);
    GL20.glDisableVertexAttribArray(1);
    GL30.glBindVertexArray(0);
  }

  public void render(TexturedModel tmodel) {
    final RawModel model = tmodel.getRawModel();
    GL30.glBindVertexArray(model.getVaoId());
    GL20.glEnableVertexAttribArray(0);
    GL20.glEnableVertexAttribArray(1);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, tmodel.getTexture().getTextureId());
    GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
    GL20.glDisableVertexAttribArray(0);
    GL20.glDisableVertexAttribArray(1);
    GL30.glBindVertexArray(0);
  }

  public void render(RawModel model) {
    GL30.glBindVertexArray(model.getVaoId());
    GL20.glEnableVertexAttribArray(0);
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
    GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
    GL20.glDisableVertexAttribArray(0);
    GL30.glBindVertexArray(0);
  }
}
