package net.seabears.game.example;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;

import net.seabears.game.entities.Player;
import net.seabears.game.particles.Particle;
import net.seabears.game.particles.ParticleSystem;
import net.seabears.game.particles.ParticleTexture;

public class PlayerParticleSystem implements ParticleSystem {
  private final Random r = new Random();
  private final ParticleTexture texture;
  private final Player player;
  private final float scale;
  private final float gravity;
  private final float ttl;

  public PlayerParticleSystem(ParticleTexture texture, Player player, float scale, float gravity, float ttl) {
    this.texture = texture;
    this.player = player;
    this.scale = scale;
    this.gravity = gravity;
    this.ttl = ttl;
  }

  @Override
  public List<Particle> generate(float t) {
        return Collections.singletonList(new Particle(texture, player.getPosition(),
                new Vector3f((float) Math.sin(Math.toRadians(player.getRotation().y + r.nextInt(10))),
                        30.0f + r.nextInt(3),
                        (float) Math.cos(Math.toRadians(player.getRotation().y + r.nextInt(10)))),
                gravity, ttl, 0.0f, scale));
  }
}
