package net.seabears.game.shaders;

import org.joml.Matrix4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.util.TransformationMatrix;
import net.seabears.game.util.ViewMatrix;

public class StaticTextureShader extends ShaderProgram {
  private int locationProjectionMatrix;
  private int locationTransformationMatrix;
  private int locationViewMatrix;

  public StaticTextureShader() {
    super("src/main/shaders/textureVertexShader.txt", "src/main/shaders/textureFragmentShader.txt");
  }

  @Override
  protected void bindAttributes() {
    // use attribute 0 of the VAO because that's where we stored our vertex positions
    super.bindAttribute(0, "position");
    // use attribute 1 of the VAO because that's where we stored our texture coords
    super.bindAttribute(1, "textureCoords");
  }

  @Override
  protected void getAllUniformLocations() {
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
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
