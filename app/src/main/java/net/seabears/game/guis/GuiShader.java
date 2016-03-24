package net.seabears.game.guis;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.shaders.StaticShader;

public class GuiShader extends ShaderProgram {
  private static final String SHADER_ROOT = "src/main/shaders/";
  private static final String VERTEX_FILE = SHADER_ROOT + "guiVertexShader.txt";
  private static final String FRAGMENT_FILE = SHADER_ROOT + "guiFragmentShader.txt";

  private int locationTransformationMatrix;

  public GuiShader() throws IOException {
    super(VERTEX_FILE, FRAGMENT_FILE);
  }

  @Override
  protected void getAllUniformLocations() {
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
  }

  public void loadTransformation(Matrix4f matrix) {
    super.loadMatrix(locationTransformationMatrix, matrix);
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(StaticShader.ATTR_POSITION, "position");
  }
}
