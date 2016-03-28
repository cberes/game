package net.seabears.game.guis.fonts;

import java.io.IOException;

import org.joml.Vector3f;

import net.seabears.game.guis.fonts.creator.GuiText;
import net.seabears.game.shaders.ShaderProgram;

public class FontShader extends ShaderProgram {
  private int locationColor;
  private int locationTranslation;

  public FontShader() throws IOException {
    super(SHADER_ROOT + "fonts/");
  }

  @Override
  protected void getAllUniformLocations() {
    locationColor = super.getUniformLocation("color");
    locationTranslation = super.getUniformLocation("translation");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(ATTR_TEXTURE, "textureCoords");
  }

  public void loadText(GuiText text) {
    super.loadFloat(locationColor, text.getColor());
    super.loadFloat(locationTranslation, text.getPosition());
  }

  public void loadTranslation(Vector3f translation) {
  }
}
