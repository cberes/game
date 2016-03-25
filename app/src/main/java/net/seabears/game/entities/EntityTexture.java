package net.seabears.game.entities;

import net.seabears.game.models.TexturedModel;

public class EntityTexture {
  private final TexturedModel model;
  private final int textureIndex;

  public EntityTexture(TexturedModel model) {
    this(model, 0);
  }

  public EntityTexture(TexturedModel model, int textureIndex) {
    this.model = model;
    this.textureIndex = textureIndex;
  }

  public TexturedModel getModel() {
    return model;
  }

  public int getTextureIndex() {
    return textureIndex;
  }
}
