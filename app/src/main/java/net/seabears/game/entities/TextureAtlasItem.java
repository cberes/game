package net.seabears.game.entities;

import org.joml.Vector2f;

public class TextureAtlasItem {
  protected Vector2f getTextureOffset(int index, int rows) {
    return new Vector2f(getTextureOffsetX(index, rows), getTextureOffsetY(index, rows));
  }

  protected float getTextureOffsetX(int index, int rows) {
    final int column = index % rows;
    return (float) column / rows;
  }

  protected float getTextureOffsetY(int index, int rows) {
    // this integer division is intentional
    final int row = index / rows;
    return (float) row / rows;
  }
}
