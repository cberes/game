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
    return mouse.isPressed() ? mouse.getPosition().normalize(w, h) : MousePosition.ZERO;
  }
}
