package net.seabears.game.shaders;

import java.io.IOException;

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

  public static final int ATTR_POSITION = 0;
  public static final int ATTR_TEXTURE = 1;
  public static final int ATTR_NORMAL = 2;

  private int locationFakeLighting;
  private int locationLightColor;
  private int locationLightPosition;
  private int locationProjectionMatrix;
  private int locationReflectivity;
  private int locationShineDamper;
  private int locationSkyColor;
  private int locationTextureRows;
  private int locationTextureOffset;
  private int locationTransformationMatrix;
  private int locationViewMatrix;

  public StaticShader() throws IOException {
    super(SHADER_ROOT + "vertexShader.txt", SHADER_ROOT + "fragmentShader.txt");
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
    locationLightColor = super.getUniformLocation("lightColor");
    locationLightPosition = super.getUniformLocation("lightPosition");
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationReflectivity = super.getUniformLocation("reflectivity");
    locationShineDamper = super.getUniformLocation("shineDamper");
    locationSkyColor = super.getUniformLocation("skyColor");
    locationTextureRows = super.getUniformLocation("textureRows");
    locationTextureOffset = super.getUniformLocation("textureOffset");
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
  }

  public void loadLight(Light light) {
    super.loadFloat(locationLightColor, light.getColor());
    super.loadFloat(locationLightPosition, light.getPosition());
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
