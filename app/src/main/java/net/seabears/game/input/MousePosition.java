package net.seabears.game.input;

public class MousePosition {
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
}
