package net.seabears.game.textures;

public class ModelTexture {
  private final int textureId;
  private final float shineDamper;
  private final float reflectivity;

  public ModelTexture(int textureId) {
    this(textureId, 1.0f, 0.0f);
  }

  public ModelTexture(int textureId, float shineDamper, float reflectivity) {
    this.textureId = textureId;
    this.shineDamper = shineDamper;
    this.reflectivity = reflectivity;
  }

  public int getTextureId() {
    return textureId;
  }

  public float getShineDamper() {
    return shineDamper;
  }

  public float getReflectivity() {
    return reflectivity;
  }
}
