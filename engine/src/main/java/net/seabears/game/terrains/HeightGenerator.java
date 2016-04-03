package net.seabears.game.terrains;

public interface HeightGenerator {
  float generate(int x, int z);

  int getVertexCount();
}
