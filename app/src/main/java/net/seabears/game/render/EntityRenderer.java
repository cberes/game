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
import net.seabears.game.textures.ModelTexture;

public class EntityRenderer {
  private final StaticShader shader;

  public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public StaticShader getShader() {
    return shader;
  }

  public void render(Map<TexturedModel, List<Entity>> entities) {
    for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
      final TexturedModel model = entry.getKey();
      final RawModel rawModel = model.getRawModel();
      final ModelTexture texture = model.getTexture();
      GL30.glBindVertexArray(rawModel.getVaoId());
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_NORMAL);
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
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_NORMAL);
      GL30.glBindVertexArray(0);
    }
  }
}
