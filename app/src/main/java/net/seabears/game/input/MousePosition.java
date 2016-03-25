package net.seabears.game.input;

public class MousePosition {
  public static final MousePosition ZERO = new MousePosition(0, 0);

  private final double x, y;

  public MousePosition(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public MousePosition normalize(int w, int h) {
    return new MousePosition(2.0f * x / w - 1, 2.0f * y / h - 1);
  }
}
