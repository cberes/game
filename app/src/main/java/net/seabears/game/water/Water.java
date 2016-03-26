package net.seabears.game.water;

import net.seabears.game.util.FpsCalc;

public class Water {
  private final FpsCalc fps;
  private final float waveSpeed;
  private final float wavePeriod;
  private float moveFactor;

  public Water(FpsCalc fps, float waveSpeed, float wavePeriod) {
    this.fps = fps;
    this.waveSpeed = waveSpeed;
    this.wavePeriod = wavePeriod;
  }

  public float update() {
    moveFactor =  (moveFactor + waveSpeed * fps.get()) % wavePeriod;
    return moveFactor;
  }
}
