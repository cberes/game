package net.seabears.game.util;

import org.joml.Vector3f;

public abstract class Tile {
    private final Vector3f center;
    private final Vector3f size;
    private float radius;

    public Tile(Vector3f center, Vector3f size) {
        this.center = new Vector3f(center);
        this.size = new Vector3f(size);
        this.radius = Math.max(size.x, size.z) / (float) Math.cos(Math.PI * 0.25);
    }

    public Vector3f getPosition() {
        return center;
    }

    public Vector3f getSize() {
        return size;
    }

    public float getRadius() {
        return radius;
    }
}
