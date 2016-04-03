package net.seabears.game.skybox;

import net.seabears.game.util.DayNightCycle;

public class Skybox {
  private final DayNightCycle cycle;
  private final int dayTextureId;
  private final int nightTextureId;

  public Skybox(DayNightCycle cycle, int dayTextureId, int nightTextureId) {
    this.cycle = cycle;
    this.dayTextureId = dayTextureId;
    this.nightTextureId = nightTextureId;
  }

  public float getBlendFactor() {
    return cycle.ratio();
  }

  public int getDayTextureId() {
    return dayTextureId;
  }

  public int getNightTextureId() {
    return nightTextureId;
  }
}
