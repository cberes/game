package net.seabears.game.util;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Frustum {
  enum Plane {
    FAR, NEAR, TOP, BOTTOM, RIGHT, LEFT
  }

  private final Map<Plane, Vector3f> normals;
  private final Map<Plane, Vector3f> points;

  public Frustum(Vector3f cameraPosition, Matrix4f viewMatrix, final float fov, final float near, final float far, final double aspectRatio) {
    this.normals = new EnumMap<>(Plane.class);
    this.points = new EnumMap<>(Plane.class);

    // get left, up, and look-at vectors from the view matrix
    // the view matrix seems to be twisted around a little from the way most people use it
    final Vector3f left = new Vector3f(viewMatrix.m00, viewMatrix.m10, viewMatrix.m20).normalize();
    final Vector3f up = new Vector3f(viewMatrix.m01, viewMatrix.m11, viewMatrix.m21).normalize();
    final Vector3f lookAt = new Vector3f(viewMatrix.m02, viewMatrix.m12, viewMatrix.m22).negate().normalize();

    // right and down are the opposite of left and up
    final Vector3f right = left.negate(new Vector3f());
    final Vector3f down = up.negate(new Vector3f());

    // get points on each plane
    // the camera position is on 4 of the planes
    final Vector3f nc = lookAt.mul(near, new Vector3f()).add(cameraPosition);
    final Vector3f fc = lookAt.mul(near + far, new Vector3f()).add(cameraPosition);
    this.points.put(Plane.NEAR, nc);
    this.points.put(Plane.FAR, fc);
    this.points.put(Plane.TOP, cameraPosition);
    this.points.put(Plane.BOTTOM, cameraPosition);
    this.points.put(Plane.LEFT, cameraPosition);
    this.points.put(Plane.RIGHT, cameraPosition);

    // NEAR and FAR are the look-at and negative look-at vectors
    this.normals.put(Plane.NEAR, lookAt);
    this.normals.put(Plane.FAR, lookAt.negate(new Vector3f()));

    // calculate remaining normals
    final double hNear = 2.0 * near * Math.tan(Math.toRadians(fov * 0.5));
    final double wNear = aspectRatio * hNear;
    this.normals.put(Plane.TOP, calcNormal(up, right, hNear, nc, cameraPosition));
    this.normals.put(Plane.BOTTOM, calcNormal(down, left, hNear, nc, cameraPosition));
    this.normals.put(Plane.RIGHT, calcNormal(right, down, wNear, nc, cameraPosition));
    this.normals.put(Plane.LEFT, calcNormal(left, up, wNear, nc, cameraPosition));
  }

  private static Vector3f calcNormal(Vector3f dir, Vector3f ortho, double near, Vector3f nc, Vector3f point) {
    // a and the ortho(-gonal) vectors should be in the same plane
    Vector3f a = dir.mul((float) near * 0.5f, new Vector3f()).add(nc).sub(point).normalize();
    return ortho.cross(a, a);
  }

  public boolean contains(final Vector3f position, final float radius) {
    return Arrays.stream(Plane.values()).allMatch(p -> distance(p, position) >= -radius);
  }

  float distance(Plane plane, Vector3f position) {
    return normals.get(plane).dot(position.sub(points.get(plane), new Vector3f()));
  }

  @Override
  public String toString() {
    // print normals
    final String eol = System.lineSeparator();
    final StringBuilder toString = new StringBuilder("Normals:").append(eol);
    for (Map.Entry<Plane, Vector3f> plane : normals.entrySet()) {
      final Vector3f v = plane.getValue();
      toString.append(plane.getKey()).append(": (").append(v.x).append(", ").append(v.y).append(", ").append(v.z).append(')').append(eol);
    }
    // print points
    toString.append("Points:");
    for (Map.Entry<Plane, Vector3f> plane : points.entrySet()) {
      final Vector3f p = plane.getValue();
      toString.append(plane.getKey()).append(": (").append(p.x).append(", ").append(p.y).append(", ").append(p.z).append(')').append(eol);
    }
    return toString.substring(0, toString.length() - eol.length()).toString();
  }
}
