package net.seabears.game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.input.DirectionKeys;
import net.seabears.game.input.MovementKeys;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.render.DisplayManager;
import net.seabears.game.render.Loader;
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.ObjLoader;
import net.seabears.game.render.TerrainRenderer;
import net.seabears.game.render.EntityRenderer;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.shaders.TerrainShader;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.util.ProjectionMatrix;

public class Main {
  private static final float FOV = 70.0f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000.0f;

  public void run() {
    final DirectionKeys dirKeys = new DirectionKeys();
    final MovementKeys movKeys = new MovementKeys();
    final Loader loader = new Loader();
    MasterRenderer renderer = null;
    try (DisplayManager display = new DisplayManager("Hello World!", 800, 600)) {
      init(display, dirKeys, movKeys);
      renderer = loop(display, loader, dirKeys, movKeys);
    } finally {
      if (renderer != null) {
        renderer.close();
      }
      loader.close();
    }
  }

  private static Vector3f cameraMovement(final DirectionKeys dir) {
    final float mag = 0.05f;
    final Vector3f move = new Vector3f();
    if (dir.up.get()) {
      move.add(0.0f, 0.0f, -mag);
    }
    if (dir.down.get()) {
      move.add(0.0f, 0.0f, mag);
    }
    if (dir.right.get()) {
      move.add(mag, 0.0f, 0.0f);
    }
    if (dir.left.get()) {
      move.add(-mag, 0.0f, 0.0f);
    }
    return move;
  }

  private MasterRenderer loop(final DisplayManager display, final Loader loader, final DirectionKeys dir, final MovementKeys mov) {
    /*
     * lights, camera, ...
     */
    final Light light = new Light(new Vector3f(3000.0f, 2000.0f, 2000.0f), new Vector3f(1.0f, 1.0f, 1.0f));
    final Camera camera = new Camera();
    camera.move(new Vector3f(0.0f, 10.0f, 0.0f));

    /*
     * rendering
     */
    final ProjectionMatrix projMatrix = new ProjectionMatrix(display.getWidth(), display.getHeight(), FOV, NEAR_PLANE, FAR_PLANE);
    final StaticShader shader = new StaticShader();
    final EntityRenderer renderer = new EntityRenderer(shader, projMatrix.toMatrix());
    final TerrainShader terrainShader = new TerrainShader();
    final TerrainRenderer terrainRenderer = new TerrainRenderer(terrainShader, projMatrix.toMatrix());
    final MasterRenderer master = new MasterRenderer(renderer, terrainRenderer);

    /*
     * models
     */
    final TexturedModel stall = new TexturedModel(ObjLoader.loadObjModel("stall", loader),
        new ModelTexture(loader.loadTexture("stall"), 1.0f, 10.0f));
    final TexturedModel tree = new TexturedModel(ObjLoader.loadObjModel("tree", loader),
        new ModelTexture(loader.loadTexture("tree")));
    final TexturedModel grass = new TexturedModel(ObjLoader.loadObjModel("grassModel", loader),
        new ModelTexture(loader.loadTexture("grassTexture"), true, true));
    final TexturedModel fern = new TexturedModel(ObjLoader.loadObjModel("fern", loader),
        new ModelTexture(loader.loadTexture("fern"), true, true));

    /*
     * entities
     */
    final List<Entity> entities = new ArrayList<>();
    entities.add(new Entity(stall, new Vector3f(0.0f, 0.0f, -50.0f), new Vector3f(0.0f, 180.0f, 0.0f), 1.0f));
    final Random rand = new Random();
    for (int i = 0; i < 500; ++i) {
      entities.add(new Entity(tree, new Vector3f(rand.nextFloat() * 800 - 400, 0, rand.nextFloat() * -600), new Vector3f().zero(), 3.0f));
      entities.add(new Entity(grass, new Vector3f(rand.nextFloat() * 800 - 400, 0, rand.nextFloat() * -600), new Vector3f().zero(), 1.0f));
      entities.add(new Entity(fern, new Vector3f(rand.nextFloat() * 800 - 400, 0, rand.nextFloat() * -600), new Vector3f().zero(), 0.6f));
    }

    /*
     * terrain
     */
    final Terrain terrain1 = new Terrain(0, -1, loader, new ModelTexture(loader.loadTexture("grass")));
    final Terrain terrain2 = new Terrain(-1, -1, loader, new ModelTexture(loader.loadTexture("grass")));

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while (display.isRunning()) {
      if (mov.forward.get()) {
        light.toggle();
      }

      camera.move(cameraMovement(dir));
      entities.forEach(master::add);
      master.add(terrain1);
      master.add(terrain2);
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
