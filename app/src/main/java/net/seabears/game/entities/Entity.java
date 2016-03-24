package net.seabears.game.entities;

import org.joml.Vector3f;

import net.seabears.game.models.TexturedModel;

public class Entity {
  private final TexturedModel model;
  private final int textureIndex;
  private final Vector3f position;
  private final Vector3f rotation;
  private final float scale;

  public Entity(TexturedModel model, Vector3f position, Vector3f rotation, float scale) {
      this(model, 0, position, rotation, scale);
  }

  public Entity(TexturedModel model, int textureIndex, Vector3f position, Vector3f rotation, float scale) {
    this.model = model;
    this.textureIndex = textureIndex;
    this.position = position;
    this.rotation = rotation;
    this.scale = scale;
  }

  public void increasePosition(Vector3f delta) {
    this.position.add(delta);
  }

  public void increaseRotation(Vector3f delta) {
    this.rotation.add(delta);
  }

  public TexturedModel getModel() {
    return model;
  }

  public float getTextureOffsetX() {
    final int column = textureIndex % model.getTexture().getRows();
    return (float) column / model.getTexture().getRows();
  }

  public float getTextureOffsetY() {
    // this integer division is intentional
    final int row = textureIndex / model.getTexture().getRows();
    return (float) row / model.getTexture().getRows();
  }

  public Vector3f getPosition() {
    return position;
  }

  public Vector3f getRotation() {
    return rotation;
  }

  public float getScale() {
    return scale;
  }
}
