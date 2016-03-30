package net.seabears.game.util;

public class FpsCounter {
  private int lastFps;
  private int fps;
  private double secondsSinceUpdate;

  public boolean update(double delta) {
    secondsSinceUpdate += delta;
    final boolean update = secondsSinceUpdate >= 1.0;
    if (update) {
      secondsSinceUpdate = 0;
      lastFps = fps;
      fps = 0;
    }
    ++fps;
    return update;
  }

  public int get() {
    return lastFps;
  }
}
