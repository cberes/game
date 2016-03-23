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
      currentTurnSpeed += turnSpeed;
    }
    if (keys.left.get()) {
      currentTurnSpeed -= turnSpeed;
    }

    // jump (if not already in the air)
    if (upwardsSpeed == 0.0f && keys.jump.get()) {
      upwardsSpeed += jumpPower;
    }

    final float time = fps.get();
    upwardsSpeed += gravity * time;
    float y = upwardsSpeed * time;
    if (y < terrainHeight) {
      y = terrainHeight;
      upwardsSpeed = 0.0f;
    }

    super.increaseRotation(new Vector3f(0.0f, currentTurnSpeed * time, 0.0f));

    final float distance = currentSpeed * time;
    final double angle = Math.toRadians(getRotation().y);
    super.increasePosition(new Vector3f((float) (distance * Math.sin(angle)), y, (float) (distance * Math.cos(angle))));
  }
}
