package net.seabears.game.water;

public class WaterFrameBuffers implements AutoCloseable {
  private final FrameBuffer reflection;
  private final FrameBuffer refraction;
  private FrameBuffer bound;

  public WaterFrameBuffers(FrameBuffer reflection, FrameBuffer refraction) {
    this.reflection = reflection;
    this.refraction = refraction;
  }

  public void bindReflection() {
    reflection.bind();
    bound = reflection;
  }

  public void bindRefraction() {
    refraction.bind();
    bound = refraction;
  }

  public void unbind(int w, int h) {
    bound.unbind(w, h);
    bound = null;
  }

  public int getReflectionTexture() {
    return reflection.getTexture();
  }

  public int getRefractionTexture() {
    return refraction.getTexture();
  }

  public int getRefractionDepthTexture() {
    return refraction.getDepthTexture();
  }

  @Override
  public void close() {
    reflection.close();
    refraction.close();
  }
}
