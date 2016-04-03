package net.seabears.game.entities;

import org.joml.Vector3f;

public class EntityLight {
  private final Light light;
  private final Vector3f offset;

  public EntityLight(Light light, Vector3f offset) {
    this.light = light;
    this.offset = offset;
  }

  public Light getLight() {
    return light;
  }

  public Vector3f getOffset() {
    return offset;
  }
}
