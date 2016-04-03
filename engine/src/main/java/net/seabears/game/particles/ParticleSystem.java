package net.seabears.game.particles;

import java.util.List;

public interface ParticleSystem {
  List<Particle> generate(float t);
}
