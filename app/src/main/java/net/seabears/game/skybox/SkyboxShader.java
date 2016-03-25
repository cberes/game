package net.seabears.game.skybox;

import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.seabears.game.entities.Camera;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.util.FpsCalc;
import net.seabears.game.util.ViewMatrix;

public class SkyboxShader extends ShaderProgram {
  private final FpsCalc fps;
  private final float rotationSpeed;
  private float rotation;
  private int locationBlendFactor;
  private int locationCubeMapDay;
  private int locationCubeMapNight;
  private int locationProjectionMatrix;
  private int locationSkyColor;
  private int locationViewMatrix;

  public SkyboxShader(FpsCalc fps, float rotationSpeed) throws IOException {
    super(SHADER_ROOT + "skybox/");
    this.fps = fps;
    this.rotationSpeed = rotationSpeed;
  }

  @Override
  protected void getAllUniformLocations() {
    locationBlendFactor = super.getUniformLocation("blendFactor");
    locationCubeMapDay = super.getUniformLocation("cubeMapDay");
    locationCubeMapNight = super.getUniformLocation("cubeMapNight");
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationSkyColor = super.getUniformLocation("fogColor");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(StaticShader.ATTR_POSITION, "position");
  }

  public void loadBlendFactor(float factor) {
    super.loadFloat(locationBlendFactor, factor);
  }

  public void loadCubeMaps() {
    super.loadInt(locationCubeMapDay, 0);
    super.loadInt(locationCubeMapNight, 1);
  }

  public void loadSky(Vector3f color) {
    super.loadFloat(locationSkyColor, color);
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
    rotation += rotationSpeed * fps.get();
    super.loadMatrix(locationViewMatrix, matrix.rotate((float) Math.toRadians(rotation), new Vector3f(0.0f, 1.0f, 0.0f)));
  }
}