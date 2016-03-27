package net.seabears.game.water;

import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.List;

import org.joml.Matrix4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.StaticShader;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.util.ViewMatrix;

public class WaterShader extends ShaderProgram {
  private final int lights;
  private int[] locationLightAttenuation;
  private int[] locationLightColor;
  private int[] locationLightPosition;
  private int locationNearPlane;
  private int locationFarPlane;
  private int locationCameraPosition;
  private int locationDepthMap;
  private int locationDuDvMap;
  private int locationNormalMap;
  private int locationMoveFactor;
  private int locationModelMatrix;
  private int locationViewMatrix;
  private int locationProjectionMatrix;
  private int locationReflectionTexture;
  private int locationRefractionTexture;
  private int locationReflectivity;
  private int locationShineDamper;

  public WaterShader(int lights) throws IOException {
    super(SHADER_ROOT + "water/");
    this.lights = lights;
  }

  @Override
  protected void bindAttributes() {
    bindAttribute(StaticShader.ATTR_POSITION, "position");
  }

  @Override
  protected void getAllUniformLocations() {
    locationLightAttenuation = super.getUniformLocations("attenuation", lights);
    locationLightColor = super.getUniformLocations("lightColor", lights);
    locationLightPosition = super.getUniformLocations("lightPosition", lights);
    locationNearPlane = super.getUniformLocation("nearPlane");
    locationFarPlane = super.getUniformLocation("farPlane");
    locationCameraPosition = super.getUniformLocation("cameraPosition");
    locationDepthMap = super.getUniformLocation("depthMap");
    locationDuDvMap = super.getUniformLocation("dudvMap");
    locationNormalMap = super.getUniformLocation("normalMap");
    locationMoveFactor = super.getUniformLocation("moveFactor");
    locationModelMatrix = super.getUniformLocation("modelMatrix");
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
    locationReflectionTexture = super.getUniformLocation("reflectionTexture");
    locationRefractionTexture = super.getUniformLocation("refractionTexture");
    locationReflectivity = super.getUniformLocation("reflectivity");
    locationShineDamper = super.getUniformLocation("shineDamper");
  }

  public void loadPlanes(float near, float far) {
    super.loadFloat(locationNearPlane, near);
    super.loadFloat(locationFarPlane, far);
  }

  public void loadLights(final List<Light> lights) {
    range(0, this.lights).forEach(i -> loadLight(i < lights.size() ? lights.get(i) : OFF_LIGHT, i));
  }

  private void loadLight(Light light, int index) {
    super.loadFloat(locationLightAttenuation[index], light.getAttenuation());
    super.loadFloat(locationLightColor[index], light.getColor());
    super.loadFloat(locationLightPosition[index], light.getPosition());
  }

  public void loadTexture(Water water) {
    super.loadFloat(locationMoveFactor, water.update());
    super.loadFloat(locationReflectivity, water.getReflectivity());
    super.loadFloat(locationShineDamper, water.getShineDamper());
  }

  public void loadWater() {
    // these refer to texture units
    super.loadInt(locationReflectionTexture, 0);
    super.loadInt(locationRefractionTexture, 1);
    super.loadInt(locationDuDvMap, 2);
    super.loadInt(locationNormalMap, 3);
    super.loadInt(locationDepthMap, 4);
  }

  public void loadModelMatrix(Matrix4f modelMatrix) {
    super.loadMatrix(locationModelMatrix, modelMatrix);
  }

  public void loadProjectionMatrix(Matrix4f projection) {
    super.loadMatrix(locationProjectionMatrix, projection);
  }

  public void loadViewMatrix(Camera camera) {
    super.loadFloat(locationCameraPosition, camera.getPosition());
    super.loadMatrix(locationViewMatrix, new ViewMatrix(camera).toMatrix());
  }
}
