package net.seabears.game.entities.normalmap;

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
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.Renderer;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.textures.ModelTexture;

public class NormalMappingRenderer implements Renderer {
  private final NormalMappingShader shader;

  public NormalMappingRenderer(NormalMappingShader shader, Matrix4f projectionMatrix) {
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadNormalMap();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public NormalMappingShader getShader() {
    return shader;
  }

  public void render(Map<TexturedModel, List<Entity>> entities) {
    for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
      final TexturedModel model = entry.getKey();
      final RawModel rawModel = model.getRawModel();
      final ModelTexture texture = model.getTexture();
      GL30.glBindVertexArray(rawModel.getVaoId());
      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_POSITION);
      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
      GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_TANGENT);
      if (texture.isTransparent()) {
        MasterRenderer.disableCulling();
      }
      shader.loadTexture(model.getTexture());
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
      GL13.glActiveTexture(GL13.GL_TEXTURE1);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getNormalMapId());
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
      GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_TANGENT);
      GL30.glBindVertexArray(0);
    }
  }

  @Override
  public void close() {
    shader.close();
  }
}
