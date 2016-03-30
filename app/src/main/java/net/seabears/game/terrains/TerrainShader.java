package net.seabears.game.terrains;

import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Light;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.shadows.ShadowBox;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.util.TransformationMatrix;
import net.seabears.game.util.ViewMatrix;

public class TerrainShader extends ShaderProgram {
  public static final int TEXTURE_UNIT_BACKGROUND = 0;
  public static final int TEXTURE_UNIT_R = 1;
  public static final int TEXTURE_UNIT_G = 2;
  public static final int TEXTURE_UNIT_B = 3;
  public static final int TEXTURE_UNIT_BLEND = 4;
  public static final int TEXTURE_SHADOW = 5;

  private final int lights;
  private int locationClippingPlane;
  private int locationFakeLighting;
  private int[] locationLightAttenuation;
  private int[] locationLightColor;
  private int[] locationLightPosition;
  private int locationProjectionMatrix;
  private int locationReflectivity;
  private int locationShineDamper;
  private int locationPcfCount;
  private int locationSkyColor;
  private int locationShadowMapSize;
  private int locationShadowDistance;
  private int locationTransitionDistance;
  private int locationToShadowMapSpace;
  private int locationTransformationMatrix;
  private int locationViewMatrix;
  private int locationBackgroundTexture;
  private int locationrTexture;
  private int locationgTexture;
  private int locationbTexture;
  private int locationBlendMap;
  private int locationShadowMap;

  public TerrainShader(int lights) throws IOException {
    super(SHADER_ROOT + "terrain/");
    this.lights = lights;
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(ATTR_TEXTURE, "textureCoords");
    super.bindAttribute(ATTR_NORMAL, "normal");
  }

  @Override
  protected void getAllUniformLocations() {
    locationClippingPlane = super.getUniformLocation("clippingPlane");
    locationFakeLighting = super.getUniformLocation("fakeLighting");
    locationLightAttenuation = super.getUniformLocations("attenuation", lights);
    locationLightColor = super.getUniformLocations("lightColor", lights);
    locationLightPosition = super.getUniformLocations("lightPosition", lights);
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationReflectivity = super.getUniformLocation("reflectivity");
    locationShineDamper = super.getUniformLocation("shineDamper");
    locationPcfCount = super.getUniformLocation("pcfCount");
    locationSkyColor = super.getUniformLocation("skyColor");
    locationShadowMapSize = super.getUniformLocation("shadowMapSize");
    locationToShadowMapSpace = super.getUniformLocation("toShadowMapSpace");
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
    locationBackgroundTexture = super.getUniformLocation("backgroundTexture");
    locationrTexture = super.getUniformLocation("rTexture");
    locationgTexture = super.getUniformLocation("gTexture");
    locationbTexture = super.getUniformLocation("bTexture");
    locationBlendMap = super.getUniformLocation("blendMap");
    locationShadowMap = super.getUniformLocation("shadowMap");
    locationShadowDistance = super.getUniformLocation("shadowDistance");
    locationTransitionDistance = super.getUniformLocation("transitionDistance");
  }

  public void loadClippingPlane(Vector4f plane) {
    this.loadFloat(locationClippingPlane, plane);
  }

  public void loadLights(final List<Light> lights) {
    range(0, this.lights).forEach(i -> loadLight(i < lights.size() ? lights.get(i) : OFF_LIGHT, i));
  }

  private void loadLight(Light light, int index) {
    super.loadFloat(locationLightAttenuation[index], light.getAttenuation());
    super.loadFloat(locationLightColor[index], light.getColor());
    super.loadFloat(locationLightPosition[index], light.getPosition());
  }

  public void loadProjectionMatrix(Matrix4f matrix) {
    super.loadMatrix(locationProjectionMatrix, matrix);
  }

  public void loadPercentageCloserFiltering(int count) {
    super.loadInt(locationPcfCount, count);
  }

  public void loadSky(Vector3f color) {
    super.loadFloat(locationSkyColor, color);
  }

  public void loadTerrain() {
    super.loadInt(locationBackgroundTexture, TEXTURE_UNIT_BACKGROUND);
    super.loadInt(locationrTexture, TEXTURE_UNIT_R);
    super.loadInt(locationgTexture, TEXTURE_UNIT_G);
    super.loadInt(locationbTexture, TEXTURE_UNIT_B);
    super.loadInt(locationBlendMap, TEXTURE_UNIT_BLEND);
    super.loadInt(locationShadowMap, TEXTURE_SHADOW);
  }

  public void loadTexture(ModelTexture texture) {
    super.loadFloat(locationFakeLighting, texture.isFakeLighting());
    super.loadFloat(locationReflectivity, texture.getReflectivity());
    super.loadFloat(locationShineDamper, texture.getShineDamper());
  }

  public void loadShadowBox(ShadowBox box) {
    super.loadFloat(locationShadowDistance, box.getShadowDistance());
    super.loadFloat(locationTransitionDistance, box.getTransitionDistance());
  }

  public void loadShadowMapSize(float size) {
    super.loadFloat(locationShadowMapSize, size);
  }

  public void loadShadowMapSpaceMatrix(Matrix4f matrix) {
    super.loadMatrix(locationToShadowMapSpace, matrix);
  }

  public void loadTransformationMatrix(Terrain terrain) {
    loadTransformationMatrix(new TransformationMatrix(new Vector3f(terrain.getX(), 0.0f, terrain.getZ()), new Vector3f(), 1.0f).toMatrix());
  }

  public void loadTransformationMatrix(Matrix4f matrix) {
    super.loadMatrix(locationTransformationMatrix, matrix);
  }

  public void loadViewMatrix(Camera camera) {
    loadViewMatrix(new ViewMatrix(camera).toMatrix());
  }

  public void loadViewMatrix(Matrix4f matrix) {
    super.loadMatrix(locationViewMatrix, matrix);
  }
}
