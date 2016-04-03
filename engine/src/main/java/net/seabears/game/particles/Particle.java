package net.seabears.game.particles;

import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.TextureAtlasItem;

public class Particle extends TextureAtlasItem {
  private final ParticleTexture texture;
  private final Vector3f position;
  private final Vector3f velocity;
  private final float gravity;
  private final float ttl;
  private final float rotation;
  private final float scale;
  private float timeAlive;
  private Vector2f textureOffset;
  private Vector2f textureOffsetNext;
  private float blend;
  private float distance;

  public Particle(ParticleTexture texture, Vector3f position, Vector3f velocity, float gravity, float ttl, float rotation, float scale) {
    this.texture = texture;
    this.textureOffset = new Vector2f();
    this.textureOffsetNext = new Vector2f();
    this.position = new Vector3f(position);
    this.velocity = new Vector3f(velocity);
    this.gravity = gravity;
    this.ttl = ttl;
    this.rotation = rotation;
    this.scale = scale;
  }

  public ParticleTexture getTexture() {
    return texture;
  }

  public Vector2f getTextureOffset() {
    return textureOffset;
  }

  public Vector2f getTextureOffsetNext() {
    return textureOffsetNext;
  }

  public float getDistance() {
    return distance;
  }

  public float getBlend() {
    return blend;
  }

  public Vector3f getPosition() {
    return position;
  }

  public float getRotation() {
    return rotation;
  }

  public float getScale() {
    return scale;
  }

  public boolean update(float t, Camera camera) {
    velocity.y += gravity * t;
    position.add(new Vector3f(velocity).mul(t));
    distance = camera.getPosition().sub(position, new Vector3f()).lengthSquared();
    updateTexture();
    timeAlive += t;
    return timeAlive < ttl;
  }

  private void updateTexture() {
    final float lifeFactor = timeAlive / ttl;
    final int stageCount = texture.getRows() * texture.getRows();
    final float progress = lifeFactor * stageCount;
    final int index = (int) Math.floor(progress);
    final int indexNext = index < stageCount - 1 ? index + 1 : index;
    this.blend = progress - index;
    this.textureOffset.set(getTextureOffset(index, texture.getRows()));
    this.textureOffsetNext.set(getTextureOffset(indexNext, texture.getRows()));
  }
}
