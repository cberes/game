package net.seabears.game.shaders;

import org.joml.Matrix4f;

public class StaticTextureShader extends ShaderProgram {
  private int locationTransformationMatrix;

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
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
  }

  public void loadTransformationMatrix(Matrix4f matrix) {
    super.loadMatrix(locationTransformationMatrix, matrix);
  }
}
