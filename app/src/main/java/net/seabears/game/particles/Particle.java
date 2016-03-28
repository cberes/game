package net.seabears.game.particles;

import org.joml.Vector3f;

import net.seabears.game.util.FpsCalc;

public class Particle {
  private final Vector3f position;
  private final Vector3f velocity;
  private final float gravity;
  private final float ttl;
  private final float rotation;
  private final float scale;
  private float timeAlive;

  public Particle(Vector3f position, Vector3f velocity, float gravity, float ttl, float rotation, float scale) {
    this.position = new Vector3f(position);
    this.velocity = new Vector3f(velocity);
    this.gravity = gravity;
    this.ttl = ttl;
    this.rotation = rotation;
    this.scale = scale;
  }

  public Vector3f getPosition() {
    return position;
  }

  public float getRotation() {
    return rotation;
  }

  public float getScale() {
    return scale;
  }

  public boolean update(FpsCalc fps) {
    final float t = fps.get();
    velocity.y += gravity * t;
    final Vector3f change = new Vector3f(velocity).mul(t);
    position.add(change);
    timeAlive += t;
    return timeAlive < ttl;
  }
}
