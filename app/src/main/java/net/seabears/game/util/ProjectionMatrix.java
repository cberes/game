package net.seabears.game.util;

import org.joml.Matrix4f;

public class ProjectionMatrix {
  private final Matrix4f matrix;

  public ProjectionMatrix(final float width, final float height, final float fov, final float near, final float far) {
    final float aspectRatio = width / height;
    final float yScale = (float) (1.0f / Math.tan(Math.toRadians(fov * 0.5f)));
    final float xScale = yScale / aspectRatio;
    final float frustumLength = far - near;

    this.matrix = new Matrix4f();
    this.matrix.m00 = xScale;
    this.matrix.m11 = yScale;
    this.matrix.m22 = -((far + near) / frustumLength);
    this.matrix.m23 = -1;
    this.matrix.m32 = -((2 * near * far) / frustumLength);
    this.matrix.m33 = 0;
  }

  public Matrix4f toMatrix() {
    return matrix;
  }
}
