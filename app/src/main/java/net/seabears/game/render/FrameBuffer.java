package net.seabears.game.render;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class FrameBuffer implements AutoCloseable {
  private final int width, height;
  private final int id;
  private final int texture;
  private final int depthBuffer;
  private final int depthTexture;

  public FrameBuffer(int width, int height, int displayWidth, int displayHeight, boolean depthBuffer) {
    this.width = width;
    this.height = height;
    this.id = createFrameBuffer();
    this.texture = createTextureAttachment();
    if (depthBuffer) {
      this.depthBuffer = createDepthBufferAttachment();
      this.depthTexture = -1;
    } else {
      this.depthTexture = createDepthTextureAttachment();
      this.depthBuffer = -1;
    }
    unbind(displayWidth, displayHeight);
  }

  public int getId() {
    return id;
  }

  public int getTexture() {
    return texture;
  }

  public int getDepthBuffer() {
    return depthBuffer;
  }

  public int getDepthTexture() {
    return depthTexture;
  }

  public void bind() {
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
    GL11.glViewport(0, 0, width, height);
  }

  /** switch to default frame buffer */
  public final void unbind(int displayWidth, int displayHeight) {
    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    GL11.glViewport(0, 0, displayWidth, displayHeight);
  }

  private final int createFrameBuffer() {
    // generate name for frame buffer
    final int frameBuffer = GL30.glGenFramebuffers();
    // create the frame buffer
    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
    // indicate that we will always render to color attachment 0
    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
    return frameBuffer;
  }

  private final int createDepthBufferAttachment() {
    final int depthBuffer = GL30.glGenRenderbuffers();
    GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
    GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
    GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
    return depthBuffer;
  }

  private final int createDepthTextureAttachment() {
    return createTextureAttachment(GL14.GL_DEPTH_COMPONENT32, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, GL30.GL_DEPTH_ATTACHMENT);
  }

  private final int createTextureAttachment() {
    return createTextureAttachment(GL11.GL_RGB, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, GL30.GL_COLOR_ATTACHMENT0);
  }

  private final int createTextureAttachment(int formatInternal, int format, int type, int attachment) {
    final int texture = GL11.glGenTextures();
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, formatInternal, width, height, 0, format, type, (ByteBuffer) null);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, attachment, texture, 0);
    return texture;
  }

  @Override
  public void close() {
    GL30.glDeleteFramebuffers(id);
    GL11.glDeleteTextures(texture);
    if (depthBuffer != -1) {
      GL30.glDeleteRenderbuffers(depthBuffer);
    }
    if (depthTexture != -1) {
      GL11.glDeleteTextures(depthTexture);
    }
  }
}
