package net.seabears.game.guis;

import org.joml.Vector2f;

public class GuiTexture {
  private final int textureId;
  private final Vector2f position;
  private final Vector2f scale;

  public GuiTexture(int textureId, Vector2f position, Vector2f scale) {
    this.textureId = textureId;
    this.position = position;
    this.scale = scale;
  }

  public int getTextureId() {
    return textureId;
  }

  public Vector2f getPosition() {
    return position;
  }

  public Vector2f getScale() {
    return scale;
  }
}
