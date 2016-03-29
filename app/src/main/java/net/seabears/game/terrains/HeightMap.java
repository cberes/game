package net.seabears.game.terrains;

import java.awt.image.BufferedImage;

public class HeightMap implements HeightGenerator {
  private static final double HALF_MAX_PIXEL_COLOR = Math.pow(2, 23);

  private final BufferedImage map;
  private final double maxHeight;

  public HeightMap(BufferedImage map, double maxHeight) {
    this.map = map;
    this.maxHeight = maxHeight;
  }

  @Override
  public float generate(int x, int z) {
    if (x < 0 || x >= map.getWidth() || z < 0 || z >= map.getHeight()) {
      return 0.0f;
    }
    return (float) ((map.getRGB(x, z) + HALF_MAX_PIXEL_COLOR) / HALF_MAX_PIXEL_COLOR * maxHeight);
  }

  @Override
  public int getVertexCount() {
    return map.getHeight();
  }
}
