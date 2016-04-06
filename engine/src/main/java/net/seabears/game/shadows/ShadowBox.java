package net.seabears.game.shadows;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.seabears.game.util.CameraOrientation;

/**
 * Represents the 3D cuboidal area of the world in which objects will cast shadows (basically
 * represents the orthographic projection area for the shadow render pass). It is updated each frame
 * to optimise the area, making it as small as possible (to allow for optimal shadow map resolution)
 * while not being too small to avoid objects not having shadows when they should. Everything inside
 * the cuboidal area represented by this object will be rendered to the shadow map in the shadow
 * render pass. Everything outside the area won't be.
 * 
 * @author Karl
 *
 */
public class ShadowBox {
  private static final float OFFSET = 10;

  private final Matrix4f lightViewMatrix;
  private final float shadowDistance, transitionDistance;
  private float minX, maxX;
  private float minY, maxY;
  private float minZ, maxZ;

  /**
   * Creates a new shadow box and calculates some initial values relating to the camera's view
   * frustum, namely the width and height of the near plane and (possibly adjusted) far plane.
   * 
   * @param lightViewMatrix - basically the "view matrix" of the light. Can be used to transform a
   *        point from world space into "light" space (i.e. changes a point's coordinates from being
   *        in relation to the world's axis to being in terms of the light's local axis).
   * @param camera - the in-game camera.
   */
  public ShadowBox(float shadowDistance, float transitionDistance) {
    this.lightViewMatrix = new Matrix4f();
    this.shadowDistance = shadowDistance;
    this.transitionDistance = transitionDistance;
  }

  /**
   * Updates the bounds of the shadow box based on the light direction and the camera's view
   * frustum, to make sure that the box covers the smallest area possible while still ensuring that
   * everything inside the camera's view (within a certain range) will cast shadows.
   */
  protected void update(CameraOrientation c) {
    boolean first = true;
    for (Vector4f point : calculateFrustumVertices(c)) {
      if (first) {
        minX = point.x;
        maxX = point.x;
        minY = point.y;
        maxY = point.y;
        minZ = point.z;
        maxZ = point.z;
        first = false;
        continue;
      }
      if (point.x > maxX) {
        maxX = point.x;
      } else if (point.x < minX) {
        minX = point.x;
      }
      if (point.y > maxY) {
        maxY = point.y;
      } else if (point.y < minY) {
        minY = point.y;
      }
      if (point.z > maxZ) {
        maxZ = point.z;
      } else if (point.z < minZ) {
        minZ = point.z;
      }
    }
    maxZ += OFFSET;

  }

  /**
   * Calculates the center of the "view cuboid" in light space first, and then converts this to
   * world space using the inverse light's view matrix.
   * 
   * @return The center of the "view cuboid" in world space.
   */
  protected Vector3f getCenter() {
    float x = (minX + maxX) / 2f;
    float y = (minY + maxY) / 2f;
    float z = (minZ + maxZ) / 2f;
    Vector4f cen = new Vector4f(x, y, z, 1);
    Matrix4f invertedLight = lightViewMatrix.invert(new Matrix4f());
    invertedLight.transform(cen);
    return new Vector3f(cen.x, cen.y, cen.z);
  }

  protected Matrix4f getLightViewMatrix() {
    return lightViewMatrix;
  }

  /**
   * @return The width of the "view cuboid" (orthographic projection area).
   */
  protected float getWidth() {
    return maxX - minX;
  }

  /**
   * @return The height of the "view cuboid" (orthographic projection area).
   */
  protected float getHeight() {
    return maxY - minY;
  }

  /**
   * @return The length of the "view cuboid" (orthographic projection area).
   */
  protected float getLength() {
    return maxZ - minZ;
  }

  public float getShadowDistance() {
    return shadowDistance;
  }

  public float getTransitionDistance() {
    return transitionDistance;
  }

  /**
   * Calculates the position of the vertex at each corner of the view frustum in light space (8
   * vertices in total, so this returns 8 positions).
   * 
   * @param rotation - camera's rotation.
   * @param forwardVector - the direction that the camera is aiming, and thus the direction of the
   *        frustum.
   * @param centerNear - the center point of the frustum's near plane.
   * @param centerFar - the center point of the frustum's (possibly adjusted) far plane.
   * @return The positions of the vertices of the frustum in light space.
   */
  private Vector4f[] calculateFrustumVertices(CameraOrientation c) {
    final Vector2f far = c.far(shadowDistance);
    final Vector3f fc = c.farCenter(shadowDistance);
    Vector3f farTop = new Vector3f(c.up).mul(far.y).add(fc);
    Vector3f farBottom =new Vector3f(c.down).mul(far.y).add(fc);
    Vector3f nearTop = new Vector3f(c.up).mul(c.hNear).add(c.nc);
    Vector3f nearBottom = new Vector3f(c.down).mul(c.hNear).add(c.nc);
    Vector4f[] points = new Vector4f[8];
    points[0] = calculateLightSpaceFrustumCorner(farTop, c.right, far.x);
    points[1] = calculateLightSpaceFrustumCorner(farTop, c.left, far.x);
    points[2] = calculateLightSpaceFrustumCorner(farBottom, c.right, far.x);
    points[3] = calculateLightSpaceFrustumCorner(farBottom, c.left, far.x);
    points[4] = calculateLightSpaceFrustumCorner(nearTop, c.right, c.wNear);
    points[5] = calculateLightSpaceFrustumCorner(nearTop, c.left, c.wNear);
    points[6] = calculateLightSpaceFrustumCorner(nearBottom, c.right, c.wNear);
    points[7] = calculateLightSpaceFrustumCorner(nearBottom, c.left, c.wNear);
    return points;
  }

  /**
   * Calculates one of the corner vertices of the view frustum in world space and converts it to
   * light space.
   * 
   * @param startPoint - the starting center point on the view frustum.
   * @param direction - the direction of the corner from the start point.
   * @param width - the distance of the corner from the start point.
   * @return - The relevant corner vertex of the view frustum in light space.
   */
  private Vector4f calculateLightSpaceFrustumCorner(Vector3f startPoint, Vector3f direction, float width) {
    Vector3f point = new Vector3f(direction).mul(width).add(startPoint);
    Vector4f point4f = new Vector4f(point.x, point.y, point.z, 1.0f);
    return lightViewMatrix.transform(point4f);
  }
}
