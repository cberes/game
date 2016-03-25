package net.seabears.game.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DayNightCycle {
  private final Supplier<Long> clock;
  private final long period;
  private final TimeUnit unit;

  public DayNightCycle(long period, TimeUnit unit, Supplier<Long> clock) {
    this.period = unit.toMillis(period);
    this.unit = unit;
    this.clock = clock;
  }

  public float ratio() {
    final long msNow = unit.toMillis(clock.get());
    return (float) Math.sin(Math.PI * (msNow % period) / period);
  }
}
