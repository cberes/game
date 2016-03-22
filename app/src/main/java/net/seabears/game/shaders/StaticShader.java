package net.seabears.game.shaders;

public class StaticShader extends ShaderProgram {
  public StaticShader() {
    super("src/main/shaders/vertexShader.txt", "src/main/shaders/fragmentShader.txt");
  }

  @Override
  protected void bindAttributes() {
    // use attribute 0 of the VAO because that's where we stored our vertex positions
    super.bindAttribute(0, "position");
  }

  @Override
  protected void getAllUniformLocations() {}
}
