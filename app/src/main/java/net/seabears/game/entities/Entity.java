package net.seabears.game.entities;

import org.joml.Vector3f;

import net.seabears.game.models.TexturedModel;

public class Entity {
  private final EntityTexture model;
  private final Vector3f position;
  private final Vector3f rotation;
  private final float scale;
  private final EntityLight light;

  public Entity(EntityTexture model, Vector3f position, Vector3f rotation, float scale) {
    this(model, position, rotation, scale, null);
  }

  public Entity(EntityTexture model, Vector3f position, Vector3f rotation, float scale, EntityLight light) {
    this.model = model;
    this.position = position;
    this.rotation = rotation;
    this.scale = scale;
    this.light = light;
    this.updateLight();
  }

  private final void updateLight() {
    // move light to this entity
    if (light != null) {
      light.getLight().getPosition().set(position).add(new Vector3f(scale).mul(light.getOffset()));
    }
  }

  public void place(Vector3f position) {
    this.position.set(position);
    this.updateLight();
  }

  public void increasePosition(Vector3f delta) {
    this.position.add(delta);
    if (light != null) {
      light.getLight().getPosition().add(delta);
    }
  }

  public void increaseRotation(Vector3f delta) {
    // TODO should light rotate as well?
    this.rotation.add(delta);
  }

  public TexturedModel getModel() {
    return model.getModel();
  }

  public float getTextureOffsetX() {
    final int column = model.getTextureIndex() % model.getModel().getTexture().getRows();
    return (float) column / model.getModel().getTexture().getRows();
  }

  public float getTextureOffsetY() {
    // this integer division is intentional
    final int row = model.getTextureIndex() / model.getModel().getTexture().getRows();
    return (float) row / model.getModel().getTexture().getRows();
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
