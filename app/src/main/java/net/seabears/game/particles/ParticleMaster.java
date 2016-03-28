package net.seabears.game.particles;

import java.util.ArrayList;
import java.util.List;

import net.seabears.game.util.FpsCalc;

public class ParticleMaster {
  private final List<Particle> particles;
  private final FpsCalc fps;

  public ParticleMaster(FpsCalc fps) {
    this.fps = fps;
    this.particles = new ArrayList<>();
  }

  public void update(List<Particle> toAdd) {
    particles.removeIf(p -> !p.update(fps));
    particles.addAll(toAdd);
  }

  public List<Particle> getParticles() {
    return particles;
  }
}
