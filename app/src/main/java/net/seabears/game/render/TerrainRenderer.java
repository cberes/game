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
import net.seabears.game.textures.TerrainTexture;
import net.seabears.game.textures.TerrainTexturePack;

public class TerrainRenderer {
  private final TerrainShader shader;

  public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
    this.shader = shader;
    this.shader.start();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.loadTerrain();
    this.shader.stop();
  }

  public TerrainShader getShader() {
    return shader;
  }

  public void render(List<Terrain> terrains) {
    for (Terrain terrain : terrains) {
      final RawModel model = terrain.getModel();
      GL30.glBindVertexArray(model.getVaoId());
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glEnableVertexAttribArray(StaticShader.ATTR_NORMAL);
      bindTextures(terrain);
      shader.loadTexture(new ModelTexture(0, 0.0f, 1.0f));
      shader.loadTransformationMatrix(terrain);
      GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_POSITION);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_TEXTURE);
      GL20.glDisableVertexAttribArray(StaticShader.ATTR_NORMAL);
      GL30.glBindVertexArray(0);
    }
  }

  private void bindTextures(Terrain terrain) {
    final TerrainTexturePack pack = terrain.getTexture();
    bindTexture(pack.getBackgroundTexture(), TerrainShader.TEXTURE_UNIT_BACKGROUND);
    bindTexture(pack.getrTexture(), TerrainShader.TEXTURE_UNIT_R);
    bindTexture(pack.getgTexture(), TerrainShader.TEXTURE_UNIT_G);
    bindTexture(pack.getbTexture(), TerrainShader.TEXTURE_UNIT_B);
    bindTexture(terrain.getBlendMap(), TerrainShader.TEXTURE_UNIT_BLEND);
  }

  private void bindTexture(TerrainTexture texture, int textureUnit) {
    GL13.glActiveTexture(getUnitId(textureUnit));
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
  }

  private static int getUnitId(final int unit) {
    switch (unit) {
      case 0:
        return GL13.GL_TEXTURE0;
      case 1:
        return GL13.GL_TEXTURE1;
      case 2:
        return GL13.GL_TEXTURE2;
      case 3:
        return GL13.GL_TEXTURE3;
      case 4:
        return GL13.GL_TEXTURE4;
      default:
        throw new UnsupportedOperationException("add texture unit mapping");
    }
  }
}
