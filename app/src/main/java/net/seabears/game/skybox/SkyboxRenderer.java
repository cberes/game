package net.seabears.game.skybox;

import java.io.IOException;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Camera;
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
  private final int texture;

  public SkyboxRenderer(Loader loader, SkyboxShader shader, Matrix4f projectionMatrix, float size, int texture) {
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
    this.texture = texture;
    this.shader = shader;
    this.shader.init();
    this.shader.start();
    this.shader.loadProjectionMatrix(projectionMatrix);
    this.shader.stop();
  }

  public void render(Camera camera) {
    shader.start();
    shader.loadViewMatrix(camera);
    GL30.glBindVertexArray(cube.getVaoId());
    GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());
    GL20.glDisableVertexAttribArray(StaticShader.ATTR_POSITION);
    GL30.glBindVertexArray(0);
    shader.stop();
  }
}
