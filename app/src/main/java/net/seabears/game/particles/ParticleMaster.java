package net.seabears.game.particles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.seabears.game.entities.Camera;
import net.seabears.game.util.FpsCalc;
import net.seabears.game.util.InsertionSort;

public class ParticleMaster {
  private final List<Particle> particles;
  private final FpsCalc fps;

  public ParticleMaster(FpsCalc fps) {
    this.fps = fps;
    this.particles = new ArrayList<>();
  }

  public void update(List<Particle> toAdd, Camera camera) {
    final float t = fps.get();
    toAdd.forEach(p -> p.update(0L, camera));
    // update all particles, remove expired ones
    particles.removeIf(p -> !p.update(t, camera));
    // add new particles to the front of the list so they are rendered first (and appear behind others)
    particles.addAll(0, toAdd);
    // sort so that farthest-away particles are first
    InsertionSort.sortDescending(particles, (a, b) -> (int) Math.signum(a.getDistance() - b.getDistance()));
  }

  public Map<ParticleTexture, List<Particle>> getParticles() {
    final ParticlesByTexture byTexture = new ParticlesByTexture();
    byTexture.addAll(particles);
    return byTexture.get();
  }
}
