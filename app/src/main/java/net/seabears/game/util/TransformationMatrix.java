package net.seabears.game.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformationMatrix {
  private final Matrix4f matrix;

  public TransformationMatrix(Vector3f translation, Vector3f rotation, float scale) {
    this.matrix = new Matrix4f();
    this.matrix.identity();
    this.matrix.translate(translation);
    this.matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1.0f, 0.0f, 0.0f));
    this.matrix.rotate((float) Math.toRadians(rotation.y), new Vector3f(0.0f, 1.0f, 0.0f));
    this.matrix.rotate((float) Math.toRadians(rotation.z), new Vector3f(0.0f, 0.0f, 1.0f));
    this.matrix.scale(scale);
  }

  public Matrix4f toMatrix() {
    return matrix;
  }
}
