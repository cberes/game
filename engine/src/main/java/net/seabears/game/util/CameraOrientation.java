package net.seabears.game.util;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CameraOrientation {
  public final Vector3f lookAt, up, right, down, left;
  public final float hNear, wNear;
  public final Vector3f nc;
  public final Vector3f position;
  public final float near;
  private final float angle;
  private final float aspectRatio;

  public CameraOrientation(Vector3f position, Matrix4f viewMatrix, final float fov, final float near, final float aspectRatio) {
    // get left, up, and look-at vectors from the view matrix
    // the view matrix seems to be twisted around a little from the way most people use it
    this.left = new Vector3f(viewMatrix.m00, viewMatrix.m10, viewMatrix.m20).normalize();
    this.up = new Vector3f(viewMatrix.m01, viewMatrix.m11, viewMatrix.m21).normalize();
    this.lookAt = new Vector3f(viewMatrix.m02, viewMatrix.m12, viewMatrix.m22).negate().normalize();

    // right and down are the opposite of left and up
    this.right = left.negate(new Vector3f());
    this.down = up.negate(new Vector3f());

    // get points on each plane
    // the camera position is on 4 of the planes
    this.nc = lookAt.mul(near, new Vector3f()).add(position);

    this.angle = (float) Math.tan(Math.toRadians(fov * 0.5));
    this.hNear = 2.0f * near * angle;
    this.wNear = aspectRatio * hNear;

    this.position = position;
    this.near = near;
    this.aspectRatio = aspectRatio;
  }

  public Vector2f far(float distance) {
    final float h = 2.0f * distance * angle;
    return new Vector2f(aspectRatio * h, h);
  }

  public Vector3f farCenter(float distance) {
    return lookAt.mul(distance, new Vector3f()).add(position);
  }
}
