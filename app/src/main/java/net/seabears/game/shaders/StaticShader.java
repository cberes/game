package net.seabears.game.shaders;

import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.util.TransformationMatrix;
import net.seabears.game.util.ViewMatrix;

public class StaticShader extends ShaderProgram {
  private static final String SHADER_ROOT = "src/main/shaders/";
  static final Light OFF_LIGHT = new Light(new Vector3f(), new Vector3f());

  public static final int ATTR_POSITION = 0;
  public static final int ATTR_TEXTURE = 1;
  public static final int ATTR_NORMAL = 2;

  private final int lights;
  private int locationFakeLighting;
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

  public StaticShader(int lights) throws IOException {
    super(SHADER_ROOT + "vertexShader.txt", SHADER_ROOT + "fragmentShader.txt");
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
    locationFakeLighting = super.getUniformLocation("fakeLighting");
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

  public void loadLights(final List<Light> lights) {
    range(0, this.lights).forEach(i -> loadLight(i < lights.size() ? lights.get(i) : OFF_LIGHT, i));
  }

  public void loadLight(Light light) {
    loadLight(light, 0);
  }

  private void loadLight(Light light, int index) {
    super.loadFloat(locationLightColor[index], light.getColor());
    super.loadFloat(locationLightPosition[index], light.getPosition());
  }

  public void loadProjectionMatrix(Matrix4f matrix) {
    super.loadMatrix(locationProjectionMatrix, matrix);
  }

  public void loadSky(Vector3f color) {
    super.loadFloat(locationSkyColor, color);
  }

  public void loadTexture(ModelTexture texture) {
    super.loadFloat(locationFakeLighting, texture.isFakeLighting());
    super.loadFloat(locationReflectivity, texture.getReflectivity());
    super.loadFloat(locationShineDamper, texture.getShineDamper());
    super.loadFloat(locationTextureRows, texture.getRows());
  }

  public void loadEntity(Entity entity) {
    loadTransformationMatrix(new TransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale()).toMatrix());
    super.loadFloat(locationTextureOffset, new Vector2f(entity.getTextureOffsetX(), entity.getTextureOffsetY()));
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
