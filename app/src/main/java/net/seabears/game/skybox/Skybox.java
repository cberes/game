package net.seabears.game.skybox;

import net.seabears.game.util.DayNightCycle;

public class Skybox {
  private final DayNightCycle cycle;

  public Skybox(DayNightCycle cycle) {
    this.cycle = cycle;
  }

  public float getBlendFactor() {
    return cycle.ratio();
  }
}
