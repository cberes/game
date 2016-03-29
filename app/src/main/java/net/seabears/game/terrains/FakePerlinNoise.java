package net.seabears.game.terrains;

import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

import java.util.Random;

import org.joml.Vector2d;

public class FakePerlinNoise implements HeightGenerator {
  public static long[] makeSeeds() {
    return new long[] {
        (long) (System.currentTimeMillis() * Math.random()),
        (long) (System.currentTimeMillis() * Math.random()),
        (long) (System.currentTimeMillis() * Math.random())
        };
  }

  private final double amplitude;
  private final int octaves;
  private final double roughness;
  private final Random random;
  private final long[] seeds;
  private final Vector2d offset;
  private final int vertexCount;

  public FakePerlinNoise(int gridX, int gridZ, double amplitude, int octaves, double roughness, Random random, long[] seeds) {
    this(gridX, gridZ, amplitude, octaves, roughness, random, seeds, 128);
  }

  public FakePerlinNoise(int gridX, int gridZ, double amplitude, int octaves, double roughness, Random random, long[] seeds, int vertexCount) {
    this.amplitude = amplitude;
    this.octaves = octaves;
    this.roughness = roughness;
    this.random = random;
    this.seeds = seeds;
    this.vertexCount = vertexCount;
    this.offset = new Vector2d(Math.abs(gridX) * (vertexCount - 1), Math.abs(gridZ) * (vertexCount - 1));
  }

  @Override
  public float generate(final int x, final int z) {
    return (float) range(0, octaves)
        .mapToDouble(i -> getInterpolatedNoise((x + offset.x) * Math.pow(2, i) / Math.pow(2, octaves - 1),
                                               (z + offset.y) * Math.pow(2, i) / Math.pow(2, octaves - 1))
            * amplitude * Math.pow(roughness, i)).sum();
  }

  @Override
  public int getVertexCount() {
    return vertexCount;
  }

  /** return a random number in [-1,1] but always the same number for given coordinates (while the game is running) */
  private double getNoise(int x, int z) {
    random.setSeed(x * seeds[1] + z * seeds[2] + seeds[0]);
    return random.nextDouble() * 2.0f - 1.0f;
  }

  public float getSmoothNoise(final int x, final int z) {
    return (float) (rangeClosed(x - 1, x + 1).mapToObj(i -> i).flatMapToDouble(i -> rangeClosed(z - 1, z + 1).mapToDouble(j -> getNoise(i, j))).average().orElse(0.0));
  }

  private double getInterpolatedNoise(double x, double z) {
    int ix = (int) x;
    int iz = (int) z;
    double fx = x - ix;
    double fz = z - iz;

    float v1 = getSmoothNoise(ix, iz);
    float v2 = getSmoothNoise(ix + 1, iz);
    float v3 = getSmoothNoise(ix, iz + 1);
    float v4 = getSmoothNoise(ix + 1, iz + 1);
    float i1 = interpolate(v1, v2, fx);
    float i2 = interpolate(v3, v4, fx);
    return interpolate(i1, i2, fz);
  }

  private float interpolate(float a, float b, double blend) {
    double theta = blend * Math.PI;
    float f = (float) (1.0f - Math.cos(theta)) * 0.5f;
    return a * (1.0f - f) + b * f;
  }
}
