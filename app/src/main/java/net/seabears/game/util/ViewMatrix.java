package net.seabears.game.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.seabears.game.entities.Camera;

public class ViewMatrix {
  private final Matrix4f matrix;

  public ViewMatrix(Camera camera) {
    this.matrix = new Matrix4f();
    this.matrix.identity();
    this.matrix.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1.0f, 0.0f, 0.0f));
    this.matrix.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0.0f, 1.0f, 0.0f));
    this.matrix.rotate((float) Math.toRadians(camera.getRoll()), new Vector3f(0.0f, 0.0f, 1.0f));
    this.matrix.translate(new Vector3f(camera.getPosition()).negate());
  }

  public Matrix4f toMatrix() {
    return matrix;
  }
}
