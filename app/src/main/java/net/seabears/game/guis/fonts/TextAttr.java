package net.seabears.game.guis.fonts;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class TextAttr {
  private final Vector3f color;
  private final float edge;
  private final float width;
  private final Vector2f offset;

  public TextAttr(Vector3f color, float width, float edge) {
    this(color, width, edge, new Vector2f());
  }

  public TextAttr(Vector3f color, float width, float edge, Vector2f offset) {
    this.color = color;
    this.edge = edge;
    this.width = width;
    this.offset = offset;
  }

  public Vector3f getColor() {
    return color;
  }

  public float getEdge() {
    return edge;
  }

  public float getWidth() {
    return width;
  }

  public Vector2f getOffset() {
    return offset;
  }
}
