package net.seabears.game.entities;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.models.RawModel;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.render.EntitiesByTexture;
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.Renderer;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.textures.ModelTexture;

public class SimpleRenderer implements Renderer {
  private final SimpleShader shader;

  public SimpleRenderer(SimpleShader shader, Matrix4f projectionMatrix) {
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public SimpleShader getShader() {
    return shader;
  }

  public void render(List<Entity> entities, Matrix4f viewMatrix) {
    // simple-rendered entities
    if (!entities.isEmpty()) {
      final EntitiesByTexture e = new EntitiesByTexture();
      e.addAll(entities);
      shader.start();
      shader.loadViewMatrix(viewMatrix);
      render(e.get());
      shader.stop();
    }
  }

  private void render(Map<TexturedModel, List<Entity>> entities) {
    for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
      final TexturedModel model = entry.getKey();
      final RawModel rawModel = model.getRawModel();
      final ModelTexture texture = model.getTexture();
      GL30.glBindVertexArray(rawModel.getVaoId());
      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_POSITION);
      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
      if (texture.isTransparent()) {
        MasterRenderer.disableCulling();
      }
      shader.loadTexture(model.getTexture());
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
      for (Entity entity : entry.getValue()) {
        shader.loadEntity(entity);
        GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
      }
      if (texture.isTransparent()) {
        MasterRenderer.enableCulling();
      }
      GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_POSITION);
      GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
      GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
      GL30.glBindVertexArray(0);
    }
  }

  @Override
  public void close() {
    shader.close();
  }
}
