package net.seabears.game.input;

import java.util.List;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.util.ViewMatrix;

public class MousePicker {
  private final MouseButton mouse;
  private final Camera camera;
  private final Matrix4f invProjectionMatrix;
  private final Vector3f currentRay;

  public MousePicker(MouseButton mouse, Camera camera, Matrix4f projectionMatrix) {
    this.mouse = mouse;
    this.camera = camera;
    this.invProjectionMatrix = projectionMatrix.invert(new Matrix4f());
    this.currentRay = new Vector3f();
  }

  public Vector3f getCurrentRay() {
    return currentRay;
  }

  public void update(int w, int h) {
    if (mouse.isPressed()) {
      currentRay.set(calculateMouseRay(w, h, new ViewMatrix(camera).toMatrix().invert()));
    }
  }

  private Vector3f calculateMouseRay(int w, int h, Matrix4f invViewMatrix) {
    // y position seems to be inverted, so un-invert it
    final MousePosition mousePos = mouse.getPosition().normalize(w, h);
    final Vector4f clipSpace = new Vector4f((float) mousePos.getX(), (float) -mousePos.getY(), -1.0f, 1.0f);
    final Vector4f eyeSpace = invProjectionMatrix.transform(clipSpace, new Vector4f());
    eyeSpace.z = -1.0f;
    eyeSpace.w = 0.0f;
    final Vector4f worldSpace = invViewMatrix.transform(eyeSpace, new Vector4f());
    return new Vector3f(worldSpace.x, worldSpace.y, worldSpace.z).normalize();
  }

  public Optional<Vector3f> findTerrainPoint(List<Terrain> terrains, float maxRange) {
    if (mouse.isPressed() && intersectionInRange(terrains, 0, maxRange, currentRay)) {
      return binarySearch(terrains, 0, maxRange, maxRange / 1000.0f, currentRay);
    } else {
      return Optional.empty();
    }
  }

  private Optional<Vector3f> binarySearch(List<Terrain> terrains, float start, float end, float tolerance, Vector3f ray) {
    final float guess = start + (end - start) / 2.0f;
    if (Math.abs(guess - start) <= tolerance) {
      final Vector3f endPoint = getPointOnRay(ray, guess);
      return Terrain.find(terrains, endPoint.x, endPoint.z).flatMap(t -> Optional.of(endPoint));
    }

    if (intersectionInRange(terrains, start, guess, ray)) {
      return binarySearch(terrains, start, guess, tolerance, ray);
    } else {
      return binarySearch(terrains, guess, end, tolerance, ray);
    }
  }

  private boolean intersectionInRange(List<Terrain> terrains, float start, float end, Vector3f ray) {
    final Vector3f startPoint = getPointOnRay(ray, start);
    final Vector3f endPoint = getPointOnRay(ray, end);
    return !isUnderGround(terrains, startPoint) && isUnderGround(terrains, endPoint);
  }

  private Vector3f getPointOnRay(Vector3f ray, float distance) {
    return new Vector3f(distance).mul(ray).add(camera.getPosition());
  }

  private boolean isUnderGround(List<Terrain> terrains, Vector3f point) {
    return point.y < Terrain.getHeight(terrains, point.x, point.z);
  }
}
