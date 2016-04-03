package net.seabears.game.water;

import org.joml.Vector3f;
import org.joml.Vector4f;

import net.seabears.game.util.Tile;

public class WaterTile extends Tile {
  private final Water water;

  public WaterTile(Water water, float centerX, float centerZ, float height, float sizeX, float sizeZ) {
    super(new Vector3f(centerX, height, centerZ), new Vector3f(sizeX, 0.0f, sizeZ));
    this.water = water;
  }

  public Water getWater() {
    return water;
  }

  public float getHeight() {
    return getPosition().y;
  }

  public float getX() {
    return getPosition().x;
  }

  public float getZ() {
    return getPosition().z;
  }

  public Vector4f toReflectionPlane() {
    // add a small offset to avoid visible water edges
    return new Vector4f(0.0f, 1.0f, 0.0f, -getPosition().y + 0.5f);
  }

  public Vector4f toRefractionPlane() {
    // add a small offset to avoid visible water edges
    return new Vector4f(0.0f, -1.0f, 0.0f, getPosition().y + 0.5f);
  }
}
