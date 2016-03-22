package net.seabears.game.models;

import net.seabears.game.RawModel;
import net.seabears.game.textures.ModelTexture;

public class TexturedModel {
  private final RawModel rawModel;
  private final ModelTexture texture;

  public TexturedModel(RawModel rawModel, ModelTexture texture) {
    this.rawModel = rawModel;
    this.texture = texture;
  }

  public RawModel getRawModel() {
    return rawModel;
  }

  public ModelTexture getTexture() {
    return texture;
  }
}
