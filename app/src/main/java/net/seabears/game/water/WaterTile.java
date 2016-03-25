package net.seabears.game.water;

import org.joml.Vector3f;

public class WaterTile {
  private final float height;
  private final float x, z;
  private final Vector3f size;

  public WaterTile(float centerX, float centerZ, float height, float sizeX, float sizeZ) {
    this.x = centerX;
    this.z = centerZ;
    this.height = height;
    this.size = new Vector3f(sizeX, 1.0f, sizeZ);
  }

  public float getHeight() {
    return height;
  }

  public float getX() {
    return x;
  }

  public float getZ() {
    return z;
  }

  public Vector3f getSize() {
    return size;
  }
}
