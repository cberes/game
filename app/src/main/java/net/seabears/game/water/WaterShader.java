package net.seabears.game.water;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.util.ViewMatrix;

public class WaterShader extends ShaderProgram {
  private int locationModelMatrix;
  private int locationViewMatrix;
  private int locationProjectionMatrix;

  public WaterShader() throws IOException {
    super(SHADER_ROOT + "water/");
  }

  @Override
  protected void bindAttributes() {
    bindAttribute(StaticShader.ATTR_POSITION, "position");
  }

  @Override
  protected void getAllUniformLocations() {
    locationModelMatrix = getUniformLocation("modelMatrix");
    locationProjectionMatrix = getUniformLocation("projectionMatrix");
    locationViewMatrix = getUniformLocation("viewMatrix");
  }

  public void loadModelMatrix(Matrix4f modelMatrix) {
    loadMatrix(locationModelMatrix, modelMatrix);
  }

  public void loadProjectionMatrix(Matrix4f projection) {
    loadMatrix(locationProjectionMatrix, projection);
  }

  public void loadViewMatrix(Camera camera) {
    loadMatrix(locationViewMatrix, new ViewMatrix(camera).toMatrix());
  }
}
