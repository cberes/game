package net.seabears.game.water;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.util.ViewMatrix;

public class WaterShader extends ShaderProgram {
  private int locationDuDvMap;
  private int locationMoveFactor;
  private int locationModelMatrix;
  private int locationViewMatrix;
  private int locationProjectionMatrix;
  private int locationReflectionTexture;
  private int locationRefractionTexture;

  public WaterShader() throws IOException {
    super(SHADER_ROOT + "water/");
  }

  @Override
  protected void bindAttributes() {
    bindAttribute(StaticShader.ATTR_POSITION, "position");
  }

  @Override
  protected void getAllUniformLocations() {
    locationDuDvMap = getUniformLocation("dudvMap");
    locationMoveFactor = getUniformLocation("moveFactor");
    locationModelMatrix = getUniformLocation("modelMatrix");
    locationProjectionMatrix = getUniformLocation("projectionMatrix");
    locationViewMatrix = getUniformLocation("viewMatrix");
    locationReflectionTexture = getUniformLocation("reflectionTexture");
    locationRefractionTexture = getUniformLocation("refractionTexture");
  }

  public void loadTextures() {
    // these refer to texture units
    super.loadInt(locationReflectionTexture, 0);
    super.loadInt(locationRefractionTexture, 1);
    super.loadInt(locationDuDvMap, 2);
  }

  public void loadMoveFactor(float moveFactor) {
    super.loadFloat(locationMoveFactor, moveFactor);
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
