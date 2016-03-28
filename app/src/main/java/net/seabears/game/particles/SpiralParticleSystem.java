package net.seabears.game.particles;

import java.util.Collections;
import java.util.List;

import org.joml.Vector3f;

import net.seabears.game.entities.Player;

public class SpiralParticleSystem implements ParticleSystem {
  private final Player player;
  private final float scale;
  private final float gravity;
  private final float ttl;

  public SpiralParticleSystem(Player player, float scale, float gravity, float ttl) {
    this.player = player;
    this.scale = scale;
    this.gravity = gravity;
    this.ttl = ttl;
  }

  @Override
  public List<Particle> generate(float t) {
    return Collections.singletonList(new Particle(player.getPosition(), new Vector3f(player.getPosition().x * -0.5f, 30.0f, player.getPosition().z * -0.5f), gravity, ttl, -player.getRotation().y, scale));
  }

}
