package net.seabears.game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

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
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.textures.ModelTexture;

public class Main {
  public void run() {
    final DirectionKeys dirKeys = new DirectionKeys();
    final MovementKeys movKeys = new MovementKeys();
    final Loader loader = new Loader();
    MasterRenderer renderer = null;
    try (DisplayManager display = new DisplayManager("Hello World!", 800, 600)) {
      init(display, dirKeys, movKeys);
      renderer = loop(display, loader, new StaticShader(), dirKeys, movKeys);
    } finally {
      if (renderer != null) {
        renderer.close();
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
      move.add(0.0f, 0.0f, 0.02f);
    }
    if (dir.right.get()) {
      move.add(0.02f, 0.0f, 0.0f);
    }
    if (dir.left.get()) {
      move.add(-0.02f, 0.0f, 0.0f);
    }
    return move;
  }

  private MasterRenderer loop(final DisplayManager display, final Loader loader,
      final ShaderProgram shader, final DirectionKeys dir, final MovementKeys mov) {
    // lights, camera, ...
    final Light light = new Light(new Vector3f(0.0f, 5.0f, -10.0f), new Vector3f(1.0f, 1.0f, 1.0f));
    final Camera camera = new Camera();

    // rendering
    final Renderer renderer =
        new Renderer(display.getWidth(), display.getHeight(), (StaticShader) shader);
    final MasterRenderer master = new MasterRenderer((StaticShader) shader, renderer);

    // models and entities
    final RawModel model = ObjLoader.loadObjModel("stall", loader);
    final ModelTexture texture = new ModelTexture(loader.loadTexture("stall"), 1.0f, 10.0f);
    final TexturedModel texturedModel = new TexturedModel(model, texture);
    final Entity entity = new Entity(texturedModel, new Vector3f(0.0f, -5.0f, -20.0f),
        new Vector3f(0.0f, 0.0f, 0.0f), 1.0f);

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

      display.update();
    }
    return master;
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
            // do nothing right now
          } else if (action == GLFW_RELEASE) {
            // also do nothing
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
