package net.seabears.game.util;

public class FpsCalc {
  private long lastFrameTime;
  private float delta;

  public void update() {
    final boolean first = lastFrameTime == 0L;
    final long t = System.nanoTime();
    if (!first) {
      delta = t - lastFrameTime;
    }
    lastFrameTime = t;
  }

  public float get() {
    return (float) (delta * 1E-9);
  }
}
