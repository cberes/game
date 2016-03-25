package net.seabears.game.input;

public class CameraPanTilt {
  private final int w;
  private final int h;
  private final MouseButton mouse;

  public CameraPanTilt(int w, int h, MouseButton mouse) {
    this.w = w;
    this.h = h;
    this.mouse = mouse;
  }

  public MousePosition get() {
    if (mouse.isPressed()) {
      final MousePosition pos = mouse.getPosition();
      return new MousePosition(normalize(pos.getX(), w), normalize(pos.getY(), h));
    } else {
      return MousePosition.ZERO;
    }
  }

  /** Returns value from 0.5 to -0.5 */
  private static double normalize(double x, int range) {
    return (x - range / 2.0) / range;
  }
}
