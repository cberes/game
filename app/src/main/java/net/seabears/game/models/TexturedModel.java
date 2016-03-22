package net.seabears.game.models;

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rawModel == null) ? 0 : rawModel.hashCode());
    result = prime * result + ((texture == null) ? 0 : texture.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }
    if (obj == null) {
        return false;
    }
    if (getClass() != obj.getClass()) {
        return false;
    }

    final TexturedModel other = (TexturedModel) obj;
    if (rawModel == null) {
        if (other.rawModel != null) {
            return false;
        }
    } else if (!rawModel.equals(other.rawModel)) {
        return false;
    }
    if (texture == null) {
        if (other.texture != null) {
            return false;
        }
    } else if (!texture.equals(other.texture)) {
        return false;
    }
    return true;
  }
}
