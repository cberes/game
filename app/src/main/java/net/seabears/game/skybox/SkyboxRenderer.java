package net.seabears.game.skybox;

import java.io.IOException;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.shaders.StaticShader;

public class SkyboxRenderer {
  public static int loadCube(Loader loader, String dir) throws IOException {
    return loader.loadCubeMap(
        dir + "right",
        dir + "left", 
        dir + "up",
        dir + "down",
        dir + "back",
        dir + "front");
  }

  private final float[] vertices;
  private final SkyboxShader shader;
  private final RawModel cube;
  private final int[] textures;

  public SkyboxRenderer(Loader loader, SkyboxShader shader, Matrix4f projectionMatrix, float size, int textureDay, int textureNight) {
    this.vertices = new float[] {
        -size,  size, -size,
        -size, -size, -size,
         size, -size, -size,
         size, -size, -size,
         size,  size, -size,
        -size,  size, -size,

        -size, -size,  size,
        -size, -size, -size,
        -size,  size, -size,
        -size,  size, -size,
        -size,  size,  size,
        -size, -size,  size,

         size, -size, -size,
         size, -size,  size,
         size,  size,  size,
         size,  size,  size,
         size,  size, -size,
         size, -size, -size,

        -size, -size,  size,
        -size,  size,  size,
         size,  size,  size,
         size,  size,  size,
         size, -size,  size,
        -size, -size,  size,

        -size,  size, -size,
         size,  size, -size,
         size,  size,  size,
         size,  size,  size,
        -size,  size,  size,
        -size,  size, -size,

        -size, -size, -size,
        -size, -size,  size,
         size, -size, -size,
         size, -size, -size,
        -size, -size,  size,
         size, -size,  size
    };
    this.cube = loader.loadToVao(vertices, 3);
    this.textures = new int[] {textureDay, textureNight};
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadCubeMaps();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public SkyboxShader getShader() {
    return shader;
  }

  public void render(Skybox skybox) {
    GL30.glBindVertexArray(cube.getVaoId());
    GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textures[0]);
    GL13.glActiveTexture(GL13.GL_TEXTURE1);
    GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textures[1]);
    shader.loadBlendFactor(skybox.getBlendFactor());
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());
    GL20.glDisableVertexAttribArray(StaticShader.ATTR_POSITION);
    GL30.glBindVertexArray(0);
  }

  public void setCubeMap(int texture, boolean day) {
    textures[day ? 0 : 1] = texture;
  }
}
