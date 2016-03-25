package net.seabears.game.entities;

import org.joml.Vector3f;

import net.seabears.game.input.MousePosition;
import net.seabears.game.input.Scroll;

public class Camera {
  private final Player player;
  private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
  private float pitch, yaw, roll;
  /** relative to the player */
  private double distance, angle;

  public Camera(Player player) {
    this.player = player;
    this.distance = 50.0f;
    this.pitch = 20.0f;
  }

  public void move(Vector3f position) {
    this.position.add(position);
  }

  public void rotate(Vector3f rotation) {
    this.pitch += rotation.x;
    this.yaw += rotation.y;
    this.roll += rotation.z;
  }

  public void move() {
    // TODO pitch: up and down
    // TODO angle: left to right
    // position
    final double heightOffset = player.getSize().getHeight() * 0.5;
    final double pitchRadians = Math.toRadians(pitch);
    final double horizontalDistance = distance * Math.cos(pitchRadians);
    final double verticalDistance = distance * Math.sin(pitchRadians);
    final double theta = player.getRotation().y + angle;
    final double thetaRadians = Math.toRadians(theta);
    position.set(player.getPosition())
        .add(new Vector3f(
            (float) -(horizontalDistance * Math.sin(thetaRadians)),
            (float) (heightOffset + verticalDistance),
            (float) -(horizontalDistance * Math.cos(thetaRadians))));

    // rotation
    yaw = (float) (180.0 - theta);
  }

  public void zoom(Scroll scroll) {
    // don't allow user to zoom past the player
    distance = Math.max(player.getSize().getWidth() * 2.0, distance - scroll.getY());
  }

  public void panTilt(MousePosition pos) {
    angle -= pos.getX() * 2.0;
    pitch = (float) Math.min(90.0, Math.max(0.0, pitch - pos.getY()));
  }

  public Vector3f getPosition() {
    return position;
  }

  public float getPitch() {
    return pitch;
  }

  public float getYaw() {
    return yaw;
  }

  public float getRoll() {
    return roll;
  }
}
