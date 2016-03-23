package net.seabears.game.textures;

public class ModelTexture {
  private final int textureId;
  private final float reflectivity;
  private final float shineDamper;
  private final boolean transparent;
  private final boolean fakeLighting;

  public ModelTexture(int textureId) {
    this(textureId, false, false);
  }

  public ModelTexture(int textureId, boolean transparent, boolean fakeLighting) {
    this(textureId, 0.0f, 1.0f, transparent, fakeLighting);
  }

  public ModelTexture(int textureId, float reflectivity, float shineDamper) {
    this(textureId, reflectivity, shineDamper, false, false);
  }

  public ModelTexture(int textureId, float reflectivity, float shineDamper, boolean transparent, boolean fakeLighting) {
    this.textureId = textureId;
    this.reflectivity = reflectivity;
    this.shineDamper = shineDamper;
    this.transparent = transparent;
    this.fakeLighting = fakeLighting;
  }

  public int getTextureId() {
    return textureId;
  }

  public float getReflectivity() {
    return reflectivity;
  }

  public float getShineDamper() {
    return shineDamper;
  }

  public boolean isTransparent() {
    return transparent;
  }

  public boolean isFakeLighting() {
    return fakeLighting;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(reflectivity);
    result = prime * result + Float.floatToIntBits(shineDamper);
    result = prime * result + textureId;
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

    final ModelTexture other = (ModelTexture) obj;
    return textureId == other.textureId &&
            Float.floatToIntBits(reflectivity) == Float.floatToIntBits(other.reflectivity) &&
            Float.floatToIntBits(shineDamper) == Float.floatToIntBits(other.shineDamper);
  }
}
