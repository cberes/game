package net.seabears.game.water;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class WaterFrameBuffers implements AutoCloseable {
  private final FrameBuffer reflection;
  private final FrameBuffer refraction;
  private FrameBuffer bound;

  public WaterFrameBuffers(FrameBuffer reflection, FrameBuffer refraction) {
    this.reflection = reflection;
    this.refraction = refraction;
  }

  public void bindReflection() {
    GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
    reflection.bind();
    bound = reflection;
  }

  public void bindRefraction() {
    GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
    refraction.bind();
    bound = refraction;
  }

  public void unbind(int w, int h) {
    GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
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
