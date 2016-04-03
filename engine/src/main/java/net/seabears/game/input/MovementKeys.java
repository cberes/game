package net.seabears.game.input;

import java.util.concurrent.atomic.AtomicBoolean;

public class MovementKeys {
  public final AtomicBoolean jump = new AtomicBoolean();
  public final AtomicBoolean forward = new AtomicBoolean();
  public final AtomicBoolean backward = new AtomicBoolean();
  public final AtomicBoolean left = new AtomicBoolean();
  public final AtomicBoolean right = new AtomicBoolean();
}
