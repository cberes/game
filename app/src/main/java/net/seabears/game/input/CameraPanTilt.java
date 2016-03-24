package net.seabears.game.input;

import java.util.concurrent.atomic.AtomicInteger;

public class CameraPanTilt {
  private final AtomicInteger x;
  private final AtomicInteger y;
  private final int w;
  private final int h;

  public CameraPanTilt(int w, int h) {
    this.x = new AtomicInteger(w / 2);
    this.y = new AtomicInteger(h / 2);
    this.w = w;
    this.h = h;
  }

  public void set(int x, int y) {
    this.x.set(x);
    this.y.set(y);
  }

  public void reset() {
    set(w / 2, h / 2);
  }

  public MousePosition get() {
    return new MousePosition(normalize(x.get(), w), normalize(y.get(), h));
  }

  /** Returns value from 0.5 to -0.5 */
  private static double normalize(int x, int range) {
    return (x - range / 2) / (double) range;
  }
}
