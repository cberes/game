package net.seabears.game;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Entity;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.shaders.StaticTextureShader;
import net.seabears.game.util.TransformationMatrix;

public class Renderer {
  private static Matrix4f createProjectionMatrix(int w, int h) {
    final float aspectRatio = (float) w / (float) h;
    final float yScale = (float) (1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio;
    final float xScale = yScale / aspectRatio;
    final float frustrumLength = FAR_PLANE - NEAR_PLANE;

    final Matrix4f matrix = new Matrix4f();
    matrix.m00 = xScale;
    matrix.m11 = yScale;
    matrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustrumLength);
    matrix.m23 = -1;
    matrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustrumLength);
    matrix.m33 = 0;
    return matrix;
  }

  private static final float FOV = 70f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000f;

  private final Matrix4f projectionMatrix;

  public Renderer(int w, int h, StaticTextureShader shader) {
    projectionMatrix = createProjectionMatrix(w, h);
    shader.start();
    shader.loadProjectionMatrix(projectionMatrix);
    shader.stop();
  }

  public void prepare() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
  }

  public void render(Entity entity, StaticTextureShader shader) {
    final TexturedModel tmodel = entity.getModel();
    final RawModel model = tmodel.getRawModel();
    GL30.glBindVertexArray(model.getVaoId());
    GL20.glEnableVertexAttribArray(0);
    GL20.glEnableVertexAttribArray(1);
    final TransformationMatrix tm = new TransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
    shader.loadTransformationMatrix(tm.toMatrix());
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
