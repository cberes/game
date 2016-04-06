package net.seabears.game.util;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.joml.Vector3f;

public class Frustum {
  enum Plane {
    FAR, NEAR, TOP, BOTTOM, RIGHT, LEFT
  }

  private final Map<Plane, Vector3f> normals;
  private final Map<Plane, Vector3f> points;

  public Frustum(final CameraOrientation c, float distance) {
    this.normals = new EnumMap<>(Plane.class);
    this.points = new EnumMap<>(Plane.class);

    // get points on each plane
    // the camera position is on 4 of the planes
    this.points.put(Plane.NEAR, c.nc);
    this.points.put(Plane.FAR, c.farCenter(distance));
    this.points.put(Plane.TOP, c.position);
    this.points.put(Plane.BOTTOM, c.position);
    this.points.put(Plane.LEFT, c.position);
    this.points.put(Plane.RIGHT, c.position);

    // NEAR and FAR are the look-at and negative look-at vectors
    this.normals.put(Plane.NEAR, c.lookAt);
    this.normals.put(Plane.FAR, c.lookAt.negate(new Vector3f()));

    // calculate remaining normals
    this.normals.put(Plane.TOP, calcNormal(c.up, c.right, c.hNear, c.nc, c.position));
    this.normals.put(Plane.BOTTOM, calcNormal(c.down, c.left, c.hNear, c.nc, c.position));
    this.normals.put(Plane.RIGHT, calcNormal(c.right, c.down, c.wNear, c.nc, c.position));
    this.normals.put(Plane.LEFT, calcNormal(c.left, c.up, c.wNear, c.nc, c.position));
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
    toString.append("Points:").append(eol);
    for (Map.Entry<Plane, Vector3f> plane : points.entrySet()) {
      final Vector3f p = plane.getValue();
      toString.append(plane.getKey()).append(": (").append(p.x).append(", ").append(p.y).append(", ").append(p.z).append(')').append(eol);
    }
    return toString.substring(0, toString.length() - eol.length()).toString();
  }
}
