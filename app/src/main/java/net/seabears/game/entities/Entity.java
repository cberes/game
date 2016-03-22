package net.seabears.game.entities;

import org.joml.Vector3f;

import net.seabears.game.models.TexturedModel;

public class Entity {
  private final TexturedModel model;
  private final Vector3f position;
  private final Vector3f rotation;
  private final float scale;

  public Entity(TexturedModel model, Vector3f position, Vector3f rotation, float scale) {
    this.model = model;
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
