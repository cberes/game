package net.seabears.game.example;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.IntStream.range;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3f;

import net.seabears.game.particles.Particle;
import net.seabears.game.particles.ParticleSystem;
import net.seabears.game.particles.ParticleTexture;

public class SimpleParticleSystem implements ParticleSystem {
  private final ParticleTexture texture;
  private final Vector3f position;
  private final float scale;
  private final float gravity;
  private final float ttl;
  private final int n;

  public SimpleParticleSystem(ParticleTexture texture, Vector3f position, float scale, float gravity, float ttl, int n) {
    this.texture = texture;
    this.position = position;
    this.scale = scale;
    this.gravity = gravity;
    this.ttl = ttl;
    this.n = n;
  }

  @Override
  public List<Particle> generate(float t) {
    return range(0, n).mapToObj(i -> new Particle(texture, position,
        new Vector3f((float) Math.random() * 10.0f - 5.0f,
                     30.0f + (float) Math.random() * 5.0f,
                     (float) Math.random() * 10.0f - 5.0f),
        gravity, ttl, 0.0f, scale)).collect(toCollection(LinkedList::new));
  }
}
