package net.seabears.game.render;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class DisplayManager implements AutoCloseable {
  private final long window;
  private final int width;
  private final int height;
  private final GLFWErrorCallback errorCallback;
  private final GLFWFramebufferSizeCallback framebufferSizeCallback;
  private GLFWCursorPosCallback cursorPosCallback;
  private GLFWKeyCallback keyCallback;
  private GLFWMouseButtonCallback mouseButtonCallback;
  private GLFWScrollCallback scrollCallback;

  public DisplayManager(String title, int width, int height) {
    this.width = width;
    this.height = height;

    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (glfwInit() != GL11.GL_TRUE) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    // Configure our window
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
    // anti-aliasing
    glfwWindowHint(GLFW_SAMPLES, 8);
    glfwWindowHint(GLFW_DEPTH_BITS, 24);
    // the window will stay hidden after creation
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
    // the window will be resizable
    glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

    // Create the window
    window = glfwCreateWindow(width, height, title, NULL, NULL);
    if (window == NULL) {
      throw new IllegalStateException("Failed to create the GLFW window");
    }

    glfwSetFramebufferSizeCallback(window,
        (framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
          @Override
          public void invoke(long window, int width, int height) {
            onResize(width, height);
          }
        }));
  }

  public void setCursorPosCallback(GLFWCursorPosCallback cursorPosCallback) {
    this.cursorPosCallback = cursorPosCallback;
  }

  public void setKeyCallback(GLFWKeyCallback keyCallback) {
    this.keyCallback = keyCallback;
  }

  public void setMouseButtonCallback(GLFWMouseButtonCallback mouseButtonCallback) {
    this.mouseButtonCallback = mouseButtonCallback;
  }

  public void setScrollCallback(GLFWScrollCallback scrollCallback) {
    this.scrollCallback = scrollCallback;
  }

  /**
   * To be called when the game's framebuffer is resized. Updates the projection matrix.
   * 
   * @param framebufferWidth The width of the new framebuffer
   * @param framebufferHeight The height of the new framebuffer
   */
  public void onResize(int framebufferWidth, int framebufferHeight) {
    glViewport(0, 0, framebufferWidth, framebufferHeight);
  }

  public void init() {
    // set callbacks
    if (cursorPosCallback != null) {
      glfwSetCursorPosCallback(window, cursorPosCallback);
    }
    if (keyCallback != null) {
      glfwSetKeyCallback(window, keyCallback);
    }
    if (mouseButtonCallback != null) {
      glfwSetMouseButtonCallback(window, mouseButtonCallback);
    }
    if (scrollCallback != null) {
      glfwSetScrollCallback(window, scrollCallback);
    }

    // Get the resolution of the primary monitor
    GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    // Center our window
    glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(window);

    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities();

    // Create buffers to put the framebuffer width and height into.
    IntBuffer framebufferWidth = BufferUtils.createIntBuffer(1);
    IntBuffer framebufferHeight = BufferUtils.createIntBuffer(1);
    // Put the framebuffer dimensions into these buffers.
    glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
    // Intialize the projection matrix with the framebuffer dimensions.
    onResize(framebufferWidth.get(), framebufferHeight.get());

    // Set the clear color
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean isRunning() {
    return glfwWindowShouldClose(window) == GL_FALSE;
  }

  public void update() {
    // swap the color buffers
    glfwSwapBuffers(window);

    // Poll for window events. The key callback above will only be invoked during this call.
    glfwPollEvents();
  }

  @Override
  public void close() {
    // Release input and window callbacks
    glfwDestroyWindow(window);
    framebufferSizeCallback.release();
    if (cursorPosCallback != null) {
      cursorPosCallback.release();
    }
    if (keyCallback != null) {
      keyCallback.release();
    }
    if (mouseButtonCallback != null) {
      mouseButtonCallback.release();
    }
    if (scrollCallback != null) {
      scrollCallback.release();
    }

    // Terminate GLFW and release the GLFWErrorCallback
    glfwTerminate();
    errorCallback.release();
  }
}
