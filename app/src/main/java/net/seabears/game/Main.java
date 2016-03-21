package net.seabears.game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

/**
 * Copied from http://wiki.lwjgl.org/wiki/LWJGL_Basics_3_(The_Quad) Physics info:
 * http://www.toptal.com/game/video-game-physics-part-iii-constrained-rigid-body-simulation
 */
public class Main {
    private final Player player = new Player(50, 100, 100);
    private final double speed = 1.0;
    private final AtomicInteger x = new AtomicInteger(0);
    private final AtomicInteger y = new AtomicInteger(0);
    private final AtomicBoolean c = new AtomicBoolean(false);

    public void run() {
        final Loader loader = new Loader();
        final Renderer renderer = new Renderer();
        try (DisplayManager display = new DisplayManager("Hello World!", 800, 600)) {
            init(display);
            loop(display, loader, renderer);
        } finally {
            loader.close();
        }
    }

    private void init(final DisplayManager display) {
        // Setup a key callback. It will be called every time a key is pressed, repeated or
        // released.
        display.setKeyCallback(new GLFWKeyCallback() {
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
        display.setMouseButtonCallback(new GLFWMouseButtonCallback() {
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

        // tell the display to finish its initialization
        display.init();
    }

    private void loop(final DisplayManager display, final Loader loader, final Renderer renderer) {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        final float[] vertices = {
                -0.5f, 0.5f, 0f,
                -0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                0.5f, 0.5f, 0f,
                -0.5f, 0.5f, 0f};
        RawModel model = loader.loadToVao(vertices);
        long t = System.nanoTime();
        while (display.isRunning()) {
            renderer.prepare();
            renderer.render(model);

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

            display.updateEnd();
        }
    }

    public static void main(String[] argv) {
        Main quadExample = new Main();
        quadExample.run();
    }
}
