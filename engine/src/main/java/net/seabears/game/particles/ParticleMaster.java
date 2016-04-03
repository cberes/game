package net.seabears.game.particles;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.seabears.game.entities.Camera;
import net.seabears.game.util.FpsCalc;

public class ParticleMaster {
  private final List<ParticleSystem> systems;
  private final List<Particle> particles;
  private final FpsCalc fps;

  public ParticleMaster(FpsCalc fps) {
    this.fps = fps;
    this.particles = new ArrayList<>();
    this.systems = new ArrayList<>();
  }

  public void add(ParticleSystem system) {
    systems.add(system);
  }

  public void update(Camera camera) {
    final float t = fps.get();
    // get particles from all systems
    final List<Particle> toAdd = systems.stream().flatMap(s -> s.generate(t).stream()).collect(toCollection(LinkedList::new));
    toAdd.forEach(p -> p.update(0L, camera));
    // update all particles, remove expired ones
    particles.removeIf(p -> !p.update(t, camera));
    // add new particles to the front of the list so they are rendered first (and appear behind others)
    particles.addAll(0, toAdd);
  }

  public List<Particle> getParticles() {
    return particles;
  }

  public static Map<ParticleTexture, List<Particle>> sortParticles(List<Particle> particles) {
    final ParticlesByTexture byTexture = new ParticlesByTexture();
    byTexture.addAll(particles);
    return byTexture.get();
  }
}
