package net.seabears.game.entities;

import org.joml.Vector3f;

public class Light {
    private static final Vector3f OFF = new Vector3f().zero();

    private final Vector3f position;
    private final Vector3f color;
    private boolean on;

    public Light(Vector3f position, Vector3f color) {
        this(position, color, true);
    }

    public Light(Vector3f position, Vector3f color, boolean on) {
        this.position = position;
        this.color = color;
        this.on = on;
    }

    public void toggle() {
        on = !on;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColor() {
        return on ? color : OFF;
    }
}
