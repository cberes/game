package net.seabears.game.input;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public enum MouseButton {
  LEFT, RIGHT;

  private final AtomicBoolean pressed = new AtomicBoolean();
  private final AtomicInteger x = new AtomicInteger();
  private final AtomicInteger y = new AtomicInteger();

  public boolean isPressed() {
    return pressed.get();
  }

  public void setPressed(boolean pressed) {
    this.pressed.set(pressed);
  }

  public MousePosition getPosition() {
    return new MousePosition(x.get(), y.get());
  }

  public void setPosition(int x, int y) {
    this.x.set(x);
    this.y.set(y);
  }
}
