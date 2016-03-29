package net.seabears.game.render;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class FrameBuffer implements AutoCloseable {
  private final int width, height;
  private final int id;
  private final int texture;
  private final int depthBuffer;
  private final int depthTexture;
  private final int frameBufferTarget;

  public FrameBuffer(int width, int height, int displayWidth, int displayHeight, boolean depthBuffer) {
    final Map<Integer, Integer> params = new HashMap<>();
    params.put(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    params.put(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    this.frameBufferTarget = GL30.GL_FRAMEBUFFER;
    this.width = width;
    this.height = height;
    this.id = createFrameBuffer(GL30.GL_COLOR_ATTACHMENT0);
    this.texture = createTextureAttachment(params);
    if (depthBuffer) {
      this.depthBuffer = createDepthBufferAttachment();
      this.depthTexture = -1;
    } else {
      this.depthTexture = createDepthTextureAttachment(GL14.GL_DEPTH_COMPONENT32, params);
      this.depthBuffer = -1;
    }
    unbind(displayWidth, displayHeight);
  }

  public FrameBuffer(int width, int height, int displayWidth, int displayHeight) {
    final Map<Integer, Integer> params = new HashMap<>();
    params.put(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    params.put(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    params.put(GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
    params.put(GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
    this.frameBufferTarget = GL30.GL_DRAW_FRAMEBUFFER;
    this.width = width;
    this.height = height;
    this.id = createFrameBuffer(GL11.GL_NONE);
    this.texture = createDepthTextureAttachment(GL14.GL_DEPTH_COMPONENT16, params);
    this.depthBuffer = -1;
    this.depthTexture = -1;
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
    GL30.glBindFramebuffer(frameBufferTarget, id);
    GL11.glViewport(0, 0, width, height);
  }

  /** switch to default frame buffer */
  public final void unbind(int displayWidth, int displayHeight) {
    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    GL11.glViewport(0, 0, displayWidth, displayHeight);
  }

  private final int createFrameBuffer(int buf) {
    // generate name for frame buffer
    final int frameBuffer = GL30.glGenFramebuffers();
    // create the frame buffer
    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
    // indicate the buffer that we will render to
    GL11.glDrawBuffer(buf);
    return frameBuffer;
  }

  private final int createDepthBufferAttachment() {
    final int depthBuffer = GL30.glGenRenderbuffers();
    GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
    GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
    GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
    return depthBuffer;
  }

  private final int createDepthTextureAttachment(int formatInternal, Map<Integer, Integer> params) {
    return createTextureAttachment(formatInternal, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, GL30.GL_DEPTH_ATTACHMENT, params);
  }

  private final int createTextureAttachment(Map<Integer, Integer> params) {
    return createTextureAttachment(GL11.GL_RGB, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, GL30.GL_COLOR_ATTACHMENT0, params);
  }

  private final int createTextureAttachment(int formatInternal, int format, int type, int attachment, Map<Integer, Integer> params) {
    final int texture = GL11.glGenTextures();
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, formatInternal, width, height, 0, format, type, (ByteBuffer) null);
    for (Map.Entry<Integer, Integer> param : params.entrySet()) {
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, param.getKey(), param.getValue());
    }
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
