package net.seabears.game.entities.normalmap;

import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.shadows.ShadowShader;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.util.TransformationMatrix;

public class NormalMappingShader extends ShadowShader {
  public static final int TEXTURE_SHADOW = 2;

  private final int lights;
  private int locationClippingPlane;
  private int locationModelTexture;
  private int locationNormalMap;
  private int[] locationLightAttenuation;
  private int[] locationLightColor;
  private int[] locationLightPosition;
  private int locationProjectionMatrix;
  private int locationReflectivity;
  private int locationShineDamper;
  private int locationSkyColor;
  private int locationTextureRows;
  private int locationTextureOffset;
  private int locationTransformationMatrix;
  private int locationViewMatrix;

  public NormalMappingShader(int lights) throws IOException {
    super(SHADER_ROOT + "normalmap/", TEXTURE_SHADOW);
    this.lights = lights;
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(ATTR_TEXTURE, "textureCoords");
    super.bindAttribute(ATTR_NORMAL, "normal");
    super.bindAttribute(ATTR_TANGENT, "tangent");
  }

  @Override
  protected void getAllUniformLocations() {
    super.getAllUniformLocations();
    locationClippingPlane = super.getUniformLocation("clippingPlane");
    locationModelTexture = super.getUniformLocation("modelTexture");
    locationNormalMap = super.getUniformLocation("normalMap");
    locationLightAttenuation = super.getUniformLocations("attenuation", lights);
    locationLightColor = super.getUniformLocations("lightColor", lights);
    locationLightPosition = super.getUniformLocations("lightPosition", lights);
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationReflectivity = super.getUniformLocation("reflectivity");
    locationShineDamper = super.getUniformLocation("shineDamper");
    locationSkyColor = super.getUniformLocation("skyColor");
    locationTextureRows = super.getUniformLocation("textureRows");
    locationTextureOffset = super.getUniformLocation("textureOffset");
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
  }

  public void loadClippingPlane(Vector4f plane) {
    this.loadFloat(locationClippingPlane, plane);
  }

  public void loadLights(final List<Light> lights, Matrix4f viewMatrix) {
    range(0, this.lights)
        .forEach(i -> loadLight(i < lights.size() ? lights.get(i) : OFF_LIGHT, i, viewMatrix));
  }

  private void loadLight(Light light, int index, Matrix4f viewMatrix) {
    super.loadFloat(locationLightAttenuation[index], light.getAttenuation());
    super.loadFloat(locationLightColor[index], light.getColor());
    super.loadFloat(locationLightPosition[index],
        getEyeSpacePosition(light.getPosition(), viewMatrix));
  }

  public void loadProjectionMatrix(Matrix4f matrix) {
    super.loadMatrix(locationProjectionMatrix, matrix);
  }

  public void loadSky(Vector3f color) {
    super.loadFloat(locationSkyColor, color);
  }

  public void loadNormalMap() {
    // refers to texture units
    // TEXTURE_SHADOW occupies one unit
    super.loadInt(locationModelTexture, 0);
    super.loadInt(locationNormalMap, 1);
  }

  public void loadTexture(ModelTexture texture) {
    super.loadFloat(locationReflectivity, texture.getReflectivity());
    super.loadFloat(locationShineDamper, texture.getShineDamper());
    super.loadFloat(locationTextureRows, texture.getRows());
  }

  public void loadEntity(Entity entity) {
    loadTransformationMatrix(
        new TransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale())
            .toMatrix());
    super.loadFloat(locationTextureOffset, entity.getTextureOffset());
  }

  public void loadTransformationMatrix(Matrix4f matrix) {
    super.loadMatrix(locationTransformationMatrix, matrix);
  }

  public void loadViewMatrix(Matrix4f matrix) {
    super.loadMatrix(locationViewMatrix, matrix);
  }

  private static Vector3f getEyeSpacePosition(Vector3f position, Matrix4f viewMatrix) {
    final Vector4f eyeSpacePos = new Vector4f(position.x, position.y, position.z, 1.0f);
    viewMatrix.transform(eyeSpacePos);
    return new Vector3f(eyeSpacePos.x, eyeSpacePos.y, eyeSpacePos.z);
  }
}
