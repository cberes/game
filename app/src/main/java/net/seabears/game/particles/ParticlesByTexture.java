package net.seabears.game.particles;

import net.seabears.game.render.EntityClassifier;

public class ParticlesByTexture extends EntityClassifier<ParticleTexture, Particle> {
  @Override
  protected ParticleTexture getTexture(Particle particle) {
    return particle.getTexture();
  }
}
