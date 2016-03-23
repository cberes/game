package net.seabears.game.render;

import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.models.RawModel;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.shaders.TerrainShader;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.textures.ModelTexture;

public class TerrainRenderer {
  private final TerrainShader shader;

  public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
    this.shader = shader;
    this.shader.start();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public TerrainShader getShader() {
    return shader;
  }

  public void render(List<Terrain> terrains) {
    for (Terrain terrain : terrains) {
      final RawModel model = terrain.getModel();
      final ModelTexture texture = terrain.getTexture();
      GL30.glBindVertexArray(model.getVaoId());
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_NORMAL);
      shader.loadTexture(texture);
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
      shader.loadTransformationMatrix(terrain);
      GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_NORMAL);
      GL30.glBindVertexArray(0);
    }
  }
}
