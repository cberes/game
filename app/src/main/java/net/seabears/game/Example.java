package net.seabears.game;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copied from http://wiki.lwjgl.org/wiki/LWJGL_Basics_3_(The_Quad) Physics info:
 * http://www.toptal.com/game/video-game-physics-part-iii-constrained-rigid-body-simulation
 */
public class Example {
    /**
     * A struct representing a framebuffer.
     */
    public static class Framebuffer {
        int width, height;
    }

    /**
     * A struct representing an orthographic projection.
     */
    public static class Projection {
        float left, right, bottom, top;
    }

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private static final float SCREEN_WIDTH = 800;
    private static final float SCREEN_HEIGHT = 600;

    /**
     * Wrapper for the framebuffer dimensions. For transforming mouse click coords.
     */
    private final Framebuffer framebuffer = new Framebuffer();

    /**
     * Wrapper for the orthographic projection currently used. For transforming mouse click coords.
     */
    private final Projection projection = new Projection();

    /**
     * The buffer representing the projection matrix.
     */
    private FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;

    /** The window handle */
    private long window;

    private final Player player = new Player(50, 100, 100);
    private final double speed = 1.0;
    private final AtomicInteger x = new AtomicInteger(0);
    private final AtomicInteger y = new AtomicInteger(0);
    private final AtomicBoolean c = new AtomicBoolean(false);

    public void run() {
        try {
            init();
            loop();

            // Release input and window callbacks
            glfwDestroyWindow(window);
            framebufferSizeCallback.release();
            keyCallback.release();
            mouseButtonCallback.release();
        } finally {
            // Terminate GLFW and release the GLFWErrorCallback
            glfwTerminate();
            errorCallback.release();
        }
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Hello World!", NULL, NULL);
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

        // Setup a key callback. It will be called every time a key is pressed, repeated or
        // released.
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    // We will detect this in our rendering loop
                    glfwSetWindowShouldClose(window, GL_TRUE);
                }

                // up
                if (key == GLFW_KEY_W && action == GLFW_PRESS) {
                    y.incrementAndGet();
                } else if (key == GLFW_KEY_W && action == GLFW_RELEASE) {
                    y.decrementAndGet();
                }

                // down
                if (key == GLFW_KEY_S && action == GLFW_PRESS) {
                    y.decrementAndGet();
                } else if (key == GLFW_KEY_S && action == GLFW_RELEASE) {
                    y.incrementAndGet();
                }

                // right
                if (key == GLFW_KEY_D && action == GLFW_PRESS) {
                    x.incrementAndGet();
                } else if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
                    x.decrementAndGet();
                }

                // left
                if (key == GLFW_KEY_A && action == GLFW_PRESS) {
                    x.decrementAndGet();
                } else if (key == GLFW_KEY_A && action == GLFW_RELEASE) {
                    x.incrementAndGet();
                }

                if (key == GLFW_KEY_ENTER && action == GLFW_RELEASE) {
                    player.setVx(0);
                    player.setVy(0);
                    player.setX(0);
                    player.setY(0);
                }
            }
        });

        // Setup the cursor pos callback.
        glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button == 0) {
                    // If this event is down event and no current to-add-ball.
                    // Else If this event is up event and there is a current to-add-ball.
                    if (action == GLFW_PRESS) {
                        c.set(true);
                    } else if (action == GLFW_RELEASE) {
                        c.set(false);
                    }
                }
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(window, (vidmode.width() - WINDOW_WIDTH) / 2, (vidmode.height() - WINDOW_HEIGHT) / 2);

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

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * To be called when the game's framebuffer is resized. Updates the projection matrix.
     * 
     * @param framebufferWidth The width of the new framebuffer
     * @param framebufferHeight The height of the new framebuffer
     */
    public void onResize(int framebufferWidth, int framebufferHeight) {
        framebuffer.width = framebufferWidth;
        framebuffer.height = framebufferHeight;
        float aspectRatio = (float) framebufferHeight / framebufferWidth;
        float desiredAspectRatio = SCREEN_HEIGHT / SCREEN_WIDTH;
        projection.left = 0;
        projection.right = SCREEN_WIDTH;
        projection.bottom = 0;
        projection.top = SCREEN_HEIGHT;
        if (aspectRatio == desiredAspectRatio) {
        } else if (aspectRatio > desiredAspectRatio) {
            float newScreenHeight = SCREEN_WIDTH * aspectRatio;
            projection.bottom = -(newScreenHeight - SCREEN_HEIGHT) / 2f;
            projection.top = newScreenHeight + projection.bottom;
        } else if (aspectRatio < desiredAspectRatio) {
            float newScreenWidth = SCREEN_HEIGHT / aspectRatio;
            projection.left = -(newScreenWidth - SCREEN_WIDTH) / 2f;
            projection.right = newScreenWidth + projection.left;
        }
        glMatrixMode(GL_PROJECTION);
        setOrtho2D(projectionMatrix, projection);
        glLoadMatrixf(projectionMatrix);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glViewport(0, 0, framebufferWidth, framebufferHeight);
    }

    /**
     * Sets the contents of the specified buffer to an orthographic projection matrix.
     * 
     * @param dest The buffer to set.
     * @param p The projection to use.
     */
    public static void setOrtho2D(FloatBuffer dest, Projection p) {
        float f1 = p.right - p.left;
        float f2 = p.top - p.bottom;
        dest.put(new float[] {2f / f1, 0, 0, 0, 0, 2f / f2, 0, 0, 0, 0, -1, 0,
                -(p.right + p.left) / f1, -(p.top + p.bottom) / f2, 0, 1});
        dest.flip();
    }

    private void loop() {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        long t = System.nanoTime();
        while (glfwWindowShouldClose(window) == GL_FALSE) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // set the color of the quad (R,G,B,A)
            if (c.get()) {
                glColor3f(0.5f, 0.5f, 0.0f);
            } else {
                glColor3f(0.5f, 0.5f, 1.0f);
            }

            // time difference
            final long tnext = System.nanoTime();
            final double dt = (tnext - t) * 1E-8;
            t = tnext;

            // input force
            double fx = x.get() * speed;
            double fy = y.get() * speed;

            // velocity
            player.setVx(player.getVx() + fx * dt);
            player.setVy(player.getVy() + fy * dt);

            // friction
            // TODO find a smarter way to do this
            final double cf = 0.04;
            if (player.getVx() > 0) {
                player.setVx(Math.max(player.getVx() - cf, 0.0));
            } else if (player.getVy() < 0) {
                player.setVx(Math.min(player.getVx() + cf, 0.0));
            }

            if (player.getVy() > 0) {
                player.setVy(Math.max(player.getVy() - cf, 0.0));
            } else if (player.getVy() < 0) {
                player.setVy(Math.min(player.getVy() + cf, 0.0));
            }

            // position
            player.setX((int) Math.round(player.getX() + player.getVx() * dt));
            player.setY((int) Math.round(player.getY() + player.getVy() * dt));

            // update player position
            // player.setX(player.getX() + x.get() * speed);
            // player.setY(player.getY() + y.get() * speed);

            // draw quad
            glBegin(GL_QUADS);
            glVertex2f(player.getX(), player.getY());
            glVertex2f(player.getX() + player.getSize(), player.getY());
            glVertex2f(player.getX() + player.getSize(), player.getY() + player.getSize());
            glVertex2f(player.getX(), player.getY() + player.getSize());
            glEnd();

            // swap the color buffers
            glfwSwapBuffers(window);

            // Poll for window events. The key callback above will only be invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] argv) {
        Example quadExample = new Example();
        quadExample.run();
    }
}
