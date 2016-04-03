package net.seabears.game.guis;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.entities.StaticShader;
import net.seabears.game.shaders.ShaderProgram;

public class GuiShader extends ShaderProgram {
  private int locationTransformationMatrix;

  public GuiShader() throws IOException {
    super(SHADER_ROOT + "gui/");
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
