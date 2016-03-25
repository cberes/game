package net.seabears.game.skybox;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.util.ViewMatrix;

public class SkyboxShader extends ShaderProgram {
  private int locationProjectionMatrix;
  private int locationViewMatrix;

  public SkyboxShader() throws IOException {
    super(SHADER_ROOT + "skybox/");
  }

  @Override
  protected void getAllUniformLocations() {
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(StaticShader.ATTR_POSITION, "position");
  }

  public void loadProjectionMatrix(Matrix4f matrix) {
    super.loadMatrix(locationProjectionMatrix, matrix);
  }

  public void loadViewMatrix(Camera camera) {
    // clear these values so that no translation is performed
    // this keeps the skybox centered around the camera
    Matrix4f matrix = new Matrix4f(new ViewMatrix(camera).toMatrix());
    matrix.m30 = 0;
    matrix.m31 = 0;
    matrix.m32 = 0;
    super.loadMatrix(locationViewMatrix, matrix);
  }
}
