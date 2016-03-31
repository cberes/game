package net.seabears.game.util;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.seabears.game.entities.Camera;

public class Frustum {
  private enum Plane {
    FAR, NEAR, TOP, BOTTOM, RIGHT, LEFT
  }

  private final Map<Plane, Vector3f> normals;
  private final Map<Plane, Vector3f> points;

  public Frustum(Camera camera, final float fov, final float near, final float far, final double aspectRatio) {
    this.normals = new EnumMap<>(Plane.class);
    this.points = new EnumMap<>(Plane.class);
    final Vector3f cameraPosition = camera.getPosition();
    final Matrix4f viewMatrix = new ViewMatrix(camera).toMatrix();

    final Vector3f left = new Vector3f(viewMatrix.m00, viewMatrix.m10, viewMatrix.m20).normalize();
    final Vector3f up = new Vector3f(viewMatrix.m01, viewMatrix.m11, viewMatrix.m21).normalize();
    final Vector3f lookAt = new Vector3f(viewMatrix.m02, viewMatrix.m12, viewMatrix.m22).negate().normalize();

//  final Vector3f right = new Vector3f(-viewMatrix.m00, viewMatrix.m10, viewMatrix.m20).normalize();
//  final Vector3f up = new Vector3f(-viewMatrix.m01, viewMatrix.m11, viewMatrix.m21).normalize();
//  final Vector3f lookAt = new Vector3f(viewMatrix.m02, -viewMatrix.m12, -viewMatrix.m22).normalize();

//  final Vector3f right = new Vector3f(viewMatrix.m00, viewMatrix.m01, viewMatrix.m02).normalize();
//  final Vector3f up = new Vector3f(viewMatrix.m10, viewMatrix.m11, viewMatrix.m12).normalize();
//  final Vector3f lookAt = new Vector3f(viewMatrix.m20,-viewMatrix.m21, viewMatrix.m22).normalize();

//  final Vector3f right = new Vector3f(viewMatrix.m00, viewMatrix.m01, viewMatrix.m02).normalize();
//  final Vector3f up = new Vector3f(viewMatrix.m10, viewMatrix.m11, viewMatrix.m12).normalize();
//  final Vector3f lookAt = new Vector3f(viewMatrix.m20, viewMatrix.m21, viewMatrix.m22).normalize();

//  final Vector3f left = right.negate(new Vector3f());
    final Vector3f right = left.negate(new Vector3f());
//  final Vector3f up = lookAt.cross(left, new Vector3f());
    final Vector3f down = up.negate(new Vector3f());
    final Vector3f nc = lookAt.mul(near, new Vector3f()).add(cameraPosition);
    final Vector3f fc = lookAt.mul(near + far, new Vector3f()).add(cameraPosition);
    this.points.put(Plane.NEAR, nc);
    this.points.put(Plane.FAR, fc);
    this.points.put(Plane.TOP, cameraPosition);
    this.points.put(Plane.BOTTOM, cameraPosition);
    this.points.put(Plane.LEFT, cameraPosition);
    this.points.put(Plane.RIGHT, cameraPosition);
    this.normals.put(Plane.NEAR, lookAt);
    this.normals.put(Plane.FAR, lookAt.negate(new Vector3f()));
    final double hNear = 2.0 * near * Math.tan(Math.toRadians(fov * 0.5));
    //final double hFar = 2.0 * (far + near) * Math.tan(Math.toRadians(fov * 0.5));
    final double wNear = aspectRatio * hNear;
    //final double wFar = aspectRatio * hFar;
    this.normals.put(Plane.TOP, calcNormal(up, right, hNear, nc, cameraPosition));
    this.normals.put(Plane.LEFT, calcNormal(left, up, wNear, nc, cameraPosition));
    this.normals.put(Plane.RIGHT, calcNormal(right, down, wNear, nc, cameraPosition));
    this.normals.put(Plane.BOTTOM, calcNormal(down, left, hNear, nc, cameraPosition));
  }

  private static Vector3f calcNormal(Vector3f dir, Vector3f ortho, double near, Vector3f nc, Vector3f point) {
    Vector3f a = dir.mul((float) near * 0.5f, new Vector3f()).add(nc).sub(point).normalize();
    return ortho.cross(a, a);
  }

  private static void print(String name, Vector3f v) {
    System.out.println(String.format("%s:\t(%f, %f, %f)", name, v.x, v.y, v.z));
  }

  public Vector3f nearPoint() {
    return points.get(Plane.NEAR);
  }

  public Vector3f farPoint() {
    return points.get(Plane.NEAR);
  }

  public void printNormals() {
    for (Plane p : Plane.values()) {
      print(p.name(), normals.get(p));
    }
  }

  public void printPoints() {
    for (Plane p : Plane.values()) {
      print(p.name(), points.get(p));
    }
  }

  public void print(Vector3f pos) {
    for (Plane p : Plane.values()) {
      System.out.println(p.name() + ":\t" + distance(p, pos));
    }
  }

  public boolean contains(final Vector3f position, final float radius) {
    return Arrays.stream(Plane.values()).allMatch(p -> distance(p, position) >= -radius);
  }

  private float distance(Plane p, Vector3f position) {
    return normals.get(p).dot(position.sub(points.get(p), new Vector3f()));
  }

  float distanceLeft(Vector3f position) {
    return distance(Plane.LEFT, position);
  }

  float distanceRight(Vector3f position) {
    return distance(Plane.RIGHT, position);
  }
}
