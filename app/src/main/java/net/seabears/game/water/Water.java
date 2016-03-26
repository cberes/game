package net.seabears.game.water;

import net.seabears.game.util.FpsCalc;

public class Water {
  private final FpsCalc fps;
  private final float waveSpeed;
  private final float wavePeriod;
  private float moveFactor;
  private final float reflectivity;
  private final float shineDamper;

  public Water(FpsCalc fps, float waveSpeed, float wavePeriod) {
    this(fps, waveSpeed, wavePeriod, 0.5f, 20.0f);
  }

  public Water(FpsCalc fps, float waveSpeed, float wavePeriod, float reflectivity, float shineDamper) {
    this.fps = fps;
    this.waveSpeed = waveSpeed;
    this.wavePeriod = wavePeriod;
    this.reflectivity = reflectivity;
    this.shineDamper = shineDamper;
  }

  public float getReflectivity() {
    return reflectivity;
  }

  public float getShineDamper() {
    return shineDamper;
  }

  public float update() {
    moveFactor =  (moveFactor + waveSpeed * fps.get()) % wavePeriod;
    return moveFactor;
  }
}
