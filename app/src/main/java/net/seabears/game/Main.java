package net.seabears.game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.input.DirectionKeys;
import net.seabears.game.input.MovementKeys;
import net.seabears.game.models.RawModel;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.render.DisplayManager;
import net.seabears.game.render.Loader;
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.ObjLoader;
import net.seabears.game.render.Renderer;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.shaders.StaticTextureShader;
import net.seabears.game.textures.ModelTexture;

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
      ShaderProgram shader = null;
      final DirectionKeys dirKeys = new DirectionKeys();
      final MovementKeys movKeys = new MovementKeys();
        try (DisplayManager display = new DisplayManager("Hello World!", 800, 600)) {
          init(display, dirKeys, movKeys);
          shader = new StaticTextureShader();
          loop(display, loader, shader, dirKeys, movKeys);
        } finally {
          if (shader != null) {
            shader.close();
          }
          loader.close();
        }
    }

    private static Vector3f cameraMovement(final DirectionKeys dir) {
        final Vector3f move = new Vector3f();
        if (dir.up.get()) {
            move.add(0.0f, 0.0f, -0.02f);
        }
        if (dir.down.get()) {
            move.add(0.0f, 0.0f,  0.02f);
        }
        if (dir.right.get()) {
            move.add( 0.02f, 0.0f, 0.0f);
        }
        if (dir.left.get()) {
            move.add(-0.02f, 0.0f, 0.0f);
        }
        return move;
    }

    private void loop(final DisplayManager display, final Loader loader, final ShaderProgram shader, final DirectionKeys dir, final MovementKeys mov) {
        // lights, camera, ...
        final Light light = new Light(new Vector3f(0.0f, 5.0f, -10.0f), new Vector3f(1.0f, 1.0f, 1.0f));
        final Camera camera = new Camera();

        // rendering
        final Renderer renderer = new Renderer(display.getWidth(), display.getHeight(), (StaticTextureShader) shader);
        final MasterRenderer master = new MasterRenderer((StaticTextureShader) shader, renderer);

        // models and entities
        final RawModel model = ObjLoader.loadObjModel("stall", loader);
        final ModelTexture texture = new ModelTexture(loader.loadTexture("stall"), 1.0f, 10.0f);
        final TexturedModel texturedModel = new TexturedModel(model, texture);
        final Entity entity = new Entity(texturedModel,
                new Vector3f(0.0f, -5.0f, -20.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                1.0f);

        long t = System.nanoTime();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (display.isRunning()) {
            if (mov.forward.get()) {
                light.toggle();
            }

            camera.move(cameraMovement(dir));
            entity.increaseRotation(new Vector3f(0.0f, 1.0f, 0.0f));
            master.add(entity);
            master.render(light, camera);

            // set the color of the quad (R,G,B,A)
//            if (c.get()) {
//                glColor3f(0.5f, 0.5f, 0.0f);
//            } else {
//                glColor3f(0.5f, 0.5f, 1.0f);
//            }
//
//            // time difference
//            final long tnext = System.nanoTime();
//            final double dt = (tnext - t) * 1E-8;
//            t = tnext;
//
//            // input force
//            double fx = x.get() * speed;
//            double fy = y.get() * speed;
//
//            // velocity
//            player.setVx(player.getVx() + fx * dt);
//            player.setVy(player.getVy() + fy * dt);
//
//            // friction
//            // TODO find a smarter way to do this
//            final double cf = 0.04;
//            if (player.getVx() > 0) {
//                player.setVx(Math.max(player.getVx() - cf, 0.0));
//            } else if (player.getVy() < 0) {
//                player.setVx(Math.min(player.getVx() + cf, 0.0));
//            }
//
//            if (player.getVy() > 0) {
//                player.setVy(Math.max(player.getVy() - cf, 0.0));
//            } else if (player.getVy() < 0) {
//                player.setVy(Math.min(player.getVy() + cf, 0.0));
//            }
//
//            // position
//            player.setX((int) Math.round(player.getX() + player.getVx() * dt));
//            player.setY((int) Math.round(player.getY() + player.getVy() * dt));
//
//            // update player position
//            // player.setX(player.getX() + x.get() * speed);
//            // player.setY(player.getY() + y.get() * speed);
//
//            // draw quad
//            glBegin(GL_QUADS);
//            glVertex2f(player.getX(), player.getY());
//            glVertex2f(player.getX() + player.getSize(), player.getY());
//            glVertex2f(player.getX() + player.getSize(), player.getY() + player.getSize());
//            glVertex2f(player.getX(), player.getY() + player.getSize());
//            glEnd();

            display.updateEnd();
        }
        master.close();
    }

    private void init(final DisplayManager display, final DirectionKeys dir, final MovementKeys mov) {
        // Setup a key callback. It will be called every time a key is pressed, repeated or
        // released.
        display.setKeyCallback(new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    // We will detect this in our rendering loop
                    glfwSetWindowShouldClose(window, GL_TRUE);
                }

                // whether key is pressed or held down
                final boolean active = action == GLFW_PRESS || action != GLFW_RELEASE;

                // directions
                if (key == GLFW_KEY_UP) {
                    dir.up.set(active);
                }
                if (key == GLFW_KEY_DOWN) {
                    dir.down.set(active);
                }
                if (key == GLFW_KEY_RIGHT) {
                    dir.right.set(active);
                }
                if (key == GLFW_KEY_LEFT) {
                    dir.left.set(active);
                }

                // movement
                if (key == GLFW_KEY_W) {
                    mov.forward.set(active);
                }
                if (key == GLFW_KEY_S) {
                    mov.backward.set(active);
                }
                if (key == GLFW_KEY_D) {
                    mov.right.set(active);
                }
                if (key == GLFW_KEY_A) {
                    mov.left.set(active);
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

    public static void main(String[] argv) {
        Main quadExample = new Main();
        quadExample.run();
    }
}
