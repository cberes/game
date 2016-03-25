package net.seabears.game.entities;

import java.util.function.Supplier;

import org.joml.Vector3f;

public class Light {
    private static final Vector3f OFF = new Vector3f();

    private final Vector3f position;
    private final Vector3f color;
    private final Vector3f attenuation;
    private final Supplier<Float> update;
    private boolean on;

    public Light(Vector3f position, Vector3f color) {
        this(position, color, new Vector3f(1.0f, 0.0f, 0.0f));
    }

    public Light(Vector3f position, Vector3f color, Supplier<Float> update) {
        this(position, color, new Vector3f(1.0f, 0.0f, 0.0f), update, true);
    }

    public Light(Vector3f position, Vector3f color, Vector3f attenuation) {
      this(position, color, attenuation, () -> 1.0f, true);
    }

    public Light(Vector3f position, Vector3f color, Vector3f attenuation, Supplier<Float> update, boolean on) {
        this.position = position;
        this.color = color;
        this.attenuation = attenuation;
        this.update = update;
        this.on = on;
    }

    public void toggle() {
        on = !on;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColor() {
        return on ? new Vector3f(update.get()).mul(color) : OFF;
    }

    public Vector3f getAttenuation() {
      return attenuation;
    }
}
