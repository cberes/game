package net.seabears.game.guis.fonts;

import java.io.IOException;

import net.seabears.game.shaders.ShaderProgram;

public class FontShader extends ShaderProgram {
  private int locationTranslation;
  private int locationOffset;
  private int locationColor;
  private int locationEdge;
  private int locationWidth;
  private int locationBorderColor;
  private int locationBorderEdge;
  private int locationBorderWidth;

  public FontShader() throws IOException {
    super(SHADER_ROOT + "fonts/");
  }

  @Override
  protected void getAllUniformLocations() {
    locationTranslation = super.getUniformLocation("translation");
    locationOffset = super.getUniformLocation("offset");
    locationColor = super.getUniformLocation("color");
    locationEdge = super.getUniformLocation("edge");
    locationWidth = super.getUniformLocation("width");
    locationBorderColor = super.getUniformLocation("borderColor");
    locationBorderEdge = super.getUniformLocation("borderEdge");
    locationBorderWidth = super.getUniformLocation("borderWidth");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(ATTR_TEXTURE, "textureCoords");
  }

  public void loadText(GuiText text) {
    super.loadFloat(locationTranslation, text.getPosition());
    super.loadFloat(locationColor, text.getAttr().getColor());
    super.loadFloat(locationEdge, text.getAttr().getEdge());
    super.loadFloat(locationWidth, text.getAttr().getWidth());
    super.loadFloat(locationBorderColor, text.getBorderAttr().getColor());
    super.loadFloat(locationBorderEdge, text.getBorderAttr().getEdge());
    super.loadFloat(locationBorderWidth, text.getBorderAttr().getWidth());
    super.loadFloat(locationOffset, text.getBorderAttr().getOffset());
  }
}
