package net.seabears.game.shaders;

import org.joml.Matrix4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.util.TransformationMatrix;
import net.seabears.game.util.ViewMatrix;

public class StaticTextureShader extends ShaderProgram {
  public static final int ATTR_POSITION = 0;
  public static final int ATTR_TEXTURE = 1;
  public static final int ATTR_NORMAL = 2;

  private int locationLightColor;
  private int locationLightPosition;
  private int locationProjectionMatrix;
  private int locationTransformationMatrix;
  private int locationViewMatrix;

  public StaticTextureShader() {
    super("src/main/shaders/textureVertexShader.txt", "src/main/shaders/textureFragmentShader.txt");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(ATTR_TEXTURE, "textureCoords");
    super.bindAttribute(ATTR_NORMAL, "normal");
  }

  @Override
  protected void getAllUniformLocations() {
    locationLightColor = super.getUniformLocation("lightColor");
    locationLightPosition = super.getUniformLocation("lightPosition");
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
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

  public void loadTransformationMatrix(Entity entity) {
    loadTransformationMatrix(new TransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale()).toMatrix());
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
