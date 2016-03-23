package net.seabears.game.entities;

import org.joml.Vector3f;

import net.seabears.game.input.MovementKeys;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.util.FpsCalc;

public class Player extends Entity {
  private final FpsCalc fps;
  private final float runSpeed;
  private final float turnSpeed;
  private final float gravity;
  private final float jumpPower;
  private float upwardsSpeed;
  private boolean inAir;

  public Player(TexturedModel model, Vector3f position, Vector3f rotation, float scale, FpsCalc fps,
      float runSpeed, float turnSpeed, float jumpPower, float gravity) {
    super(model, position, rotation, scale);
    this.fps = fps;
    this.runSpeed = runSpeed;
    this.turnSpeed = turnSpeed;
    this.jumpPower = jumpPower;
    this.gravity = gravity;
  }

  public void move(final MovementKeys keys, final float terrainHeight) {
    float currentSpeed = 0;
    if (keys.forward.get()) {
      currentSpeed += runSpeed;
    }
    if (keys.backward.get()) {
      currentSpeed -= runSpeed;
    }

    float currentTurnSpeed = 0;
    if (keys.right.get()) {
      currentTurnSpeed -= turnSpeed;
    }
    if (keys.left.get()) {
      currentTurnSpeed += turnSpeed;
    }

    // rotation
    final float time = fps.get();
    super.increaseRotation(new Vector3f(0.0f, currentTurnSpeed * time, 0.0f));

    // distance along the plane (walking)
    final float distance = currentSpeed * time;
    final double angle = Math.toRadians(getRotation().y);
    final float dx = (float) (distance * Math.sin(angle));
    final float dz = (float) (distance * Math.cos(angle));

    // jump (if not already in the air)
    if (!inAir && keys.jump.get()) {
      upwardsSpeed += jumpPower;
      inAir = true;
    }

    // distance orthogonal to the plane (jumping)
    upwardsSpeed += gravity * time;
    float dy = upwardsSpeed * time;

    // update position
    super.increasePosition(new Vector3f(dx, dy, dz));

    // ensure we're not below the terrain
    if (getPosition().y < terrainHeight) {
      getPosition().y = terrainHeight;
      upwardsSpeed = 0.0f; // stop falling
      inAir = false; // at terrain level
    }
  }
}
