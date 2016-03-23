package net.seabears.game.entities;

import org.joml.Vector3f;

public class Camera {
    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private float pitch, yaw, roll;

    public void move(Vector3f position) {
        this.position.add(position);
    }

    public void rotate(Vector3f rotation) {
        this.pitch += rotation.x;
        this.yaw += rotation.y;
        this.roll += rotation.z;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }
}
