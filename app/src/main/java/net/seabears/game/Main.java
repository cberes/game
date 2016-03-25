package net.seabears.game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.Player;
import net.seabears.game.guis.GuiRenderer;
import net.seabears.game.guis.GuiShader;
import net.seabears.game.guis.GuiTexture;
import net.seabears.game.input.CameraPanTilt;
import net.seabears.game.input.DirectionKeys;
import net.seabears.game.input.MouseButton;
import net.seabears.game.input.MovementKeys;
import net.seabears.game.input.Scroll;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.render.DisplayManager;
import net.seabears.game.render.Loader;
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.EntityRenderer;
import net.seabears.game.shaders.StaticShader;
import net.seabears.game.skybox.SkyboxRenderer;
import net.seabears.game.skybox.SkyboxShader;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.terrains.TerrainRenderer;
import net.seabears.game.terrains.TerrainShader;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.textures.TerrainTexture;
import net.seabears.game.textures.TerrainTexturePack;
import net.seabears.game.util.DayNightCycle;
import net.seabears.game.util.FpsCalc;
import net.seabears.game.util.ObjFileLoader;
import net.seabears.game.util.ProjectionMatrix;
import net.seabears.game.util.Volume;

public class Main {
  private static final float FOV = 70.0f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000.0f;
  private static final float GRAVITY = -32.0f;
  private static final float SKYBOX_SIZE = 500.0f;
  private static final long DAY_LENGTH_MS = TimeUnit.HOURS.toMillis(1L);
  private static final int MAX_LIGHTS = 4;

  public void run() {
    final DirectionKeys dirKeys = new DirectionKeys();
    final MovementKeys movKeys = new MovementKeys();
    final Loader loader = new Loader();
    final BlockingQueue<Scroll> scrolls = new LinkedBlockingDeque<>();
    MasterRenderer renderer = null;
    try (DisplayManager display = new DisplayManager("Game Engine", 800, 600)) {
      final CameraPanTilt panTilt = new CameraPanTilt(display.getWidth(), display.getHeight(), MouseButton.RIGHT);
      init(display, dirKeys, movKeys, scrolls, panTilt);
      renderer = loop(display, loader, dirKeys, movKeys, scrolls, panTilt);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (renderer != null) {
        renderer.close();
      }
      loader.close();
    }
  }

  private MasterRenderer loop(final DisplayManager display, final Loader loader, final DirectionKeys dir, final MovementKeys mov, final BlockingQueue<Scroll> scrolls, final CameraPanTilt panTilt) throws IOException {
    final FpsCalc fps = new FpsCalc();
    final DayNightCycle cycle = new DayNightCycle(DAY_LENGTH_MS, TimeUnit.MILLISECONDS, () -> System.currentTimeMillis());

    /*
     * player
     */
    final TexturedModel playerModel = new TexturedModel(loader.loadToVao(ObjFileLoader.load("bunny")), new ModelTexture(loader.loadTexture("bunny"), 1.0f, 5.0f));
    final Player player = new Player(playerModel, new Vector3f(0, 0, -40), new Vector3f().zero(), 0.5f, fps, new Volume(5, 4), 20.0f, 160.0f, -GRAVITY * 0.5f, GRAVITY);

    /*
     * lights, camera, ...
     */
    final List<Light> lights = new ArrayList<>();
    lights.add(new Light(new Vector3f(0.0f, 10000.0f, -7000.0f), new Vector3f(1.0f), () -> Math.max(cycle.ratio(), 0.2f)));
    lights.add(new Light(new Vector3f(185.0f, 10.0f, -293.0f), new Vector3f(2.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    lights.add(new Light(new Vector3f(370.0f, 17.0f, -300.0f), new Vector3f(0.0f, 2.0f, 2.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    lights.add(new Light(new Vector3f(293.0f,  7.0f, -305.0f), new Vector3f(2.0f, 2.0f, 0.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    final Camera camera = new Camera(player);

    /*
     * rendering
     */
    final ProjectionMatrix projMatrix = new ProjectionMatrix(display.getWidth(), display.getHeight(), FOV, NEAR_PLANE, FAR_PLANE);
    final StaticShader shader = new StaticShader(MAX_LIGHTS);
    final EntityRenderer renderer = new EntityRenderer(shader, projMatrix.toMatrix());
    final TerrainShader terrainShader = new TerrainShader(MAX_LIGHTS);
    final TerrainRenderer terrainRenderer = new TerrainRenderer(terrainShader, projMatrix.toMatrix());
    final SkyboxRenderer skyboxRenderer = new SkyboxRenderer(loader,
        new SkyboxShader(fps, 1.0f), projMatrix.toMatrix(), SKYBOX_SIZE,
        SkyboxRenderer.loadCube(loader, "skybox-stormy/"),
        SkyboxRenderer.loadCube(loader, "skybox-night/"));
    final GuiRenderer guiRenderer = new GuiRenderer(loader, new GuiShader());
    final MasterRenderer master = new MasterRenderer(new Vector3f(0.5f), renderer, terrainRenderer, skyboxRenderer);

    /*
     * models
     */
    final TexturedModel stall = new TexturedModel(loader.loadToVao(ObjFileLoader.load("stall")),
        new ModelTexture(loader.loadTexture("stall"), 1.0f, 10.0f));
    final TexturedModel lamp = new TexturedModel(loader.loadToVao(ObjFileLoader.load("lamp")),
        new ModelTexture(loader.loadTexture("lamp"), 1.0f, 5.0f));
    final TexturedModel tree = new TexturedModel(loader.loadToVao(ObjFileLoader.load("pine")),
        new ModelTexture(loader.loadTexture("pine"), true, true));
    final TexturedModel lowPolyTree = new TexturedModel(loader.loadToVao(ObjFileLoader.load("lowPolyTree")),
        new ModelTexture(loader.loadTexture("lowPolyTree"), 2));
    final TexturedModel fern = new TexturedModel(loader.loadToVao(ObjFileLoader.load("fern")),
        new ModelTexture(loader.loadTexture("fern"), 2, true, true));

    /*
     * terrains
     */
    final TerrainTexturePack terrainPack = new TerrainTexturePack(
            new TerrainTexture(loader.loadTexture("grass")),
            new TerrainTexture(loader.loadTexture("mud")),
            new TerrainTexture(loader.loadTexture("grass-flowers")),
            new TerrainTexture(loader.loadTexture("tile-path")));
    final TerrainTexture terrainBlend = new TerrainTexture(loader.loadTexture("blend-map"));
    final BufferedImage heightMap = loader.loadImage("heightmap");
    final List<Terrain> terrains = new ArrayList<>();
    terrains.add(new Terrain(0, -1, loader, terrainPack, terrainBlend, heightMap));
    terrains.add(new Terrain(-1, -1, loader, terrainPack, terrainBlend, heightMap));

    /*
     * entities
     */
    final List<Entity> entities = new ArrayList<>();
    entities.add(player);
    final Random rand = new Random();
    final int numTrees = 1000;
    entities.add(new Entity(lamp, new Vector3f(185.0f, -4.7f, -293.0f), new Vector3f(), 1.0f));
    entities.add(new Entity(lamp, new Vector3f(370.0f,  4.2f, -300.0f), new Vector3f(), 1.0f));
    entities.add(new Entity(lamp, new Vector3f(293.0f, -6.8f, -305.0f), new Vector3f(), 1.0f));
    for (int i = 0; i < numTrees; ++i) {
      entities.add(new Entity(tree, position(rand, terrains), new Vector3f(), 0.8f));
      entities.add(new Entity(lowPolyTree, rand.nextInt(4), position(rand, terrains), new Vector3f(), 0.4f));
    }
    for (int i = 0; i < numTrees * 4; ++i) {
      entities.add(new Entity(fern, rand.nextInt(4), position(rand, terrains), new Vector3f(), 0.6f));
    }
    for (int i = 0; i < 10; ++i) {
      entities.add(new Entity(stall, position(rand, terrains), new Vector3f(0.0f, rand.nextInt(360), 0.0f), 1.0f));
    }

    /*
     * GUIs
     */
    List<GuiTexture> guis = new ArrayList<>();
    guis.add(new GuiTexture(loader.loadTexture("winnie"), new Vector2f(0.7f, 0.7f),
            new Vector2f(0.1f, display.getWidth() / (float) display.getHeight() * 0.1f)));

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    final List<Scroll> currentScrolls = new LinkedList<>();
    while (display.isRunning()) {
      // update timing
      fps.update();

      // move player
      player.move(mov, (x, z) -> Terrain.getHeight(terrains, x, z));

      // move camera (after player)
      currentScrolls.clear();
      scrolls.drainTo(currentScrolls);
      currentScrolls.forEach(camera::zoom);
      camera.panTilt(panTilt.get());
      camera.move();

      // add entities
      entities.forEach(master::add);
      terrains.forEach(master::add);

      // render scene
      master.render(lights, camera, cycle);
      guiRenderer.render(guis);

      // I don't know if the stuff in here is necessary
      display.update();
    }
    guiRenderer.close();
    return master;
  }

  private static Vector3f position(Random rand, List<Terrain> terrains) {
    final float x = rand.nextFloat() * 1600 - 800;
    final float z = rand.nextFloat() * -800;
    return new Vector3f(x, Terrain.getHeight(terrains, x, z), z);
  }

  private void init(final DisplayManager display, final DirectionKeys dir, final MovementKeys mov, final Queue<Scroll> scrolls, final CameraPanTilt panTilt) {
    /*
     * Keyboard callback
     */
    display.setKeyCallback(new GLFWKeyCallback() {
      @Override
      public void invoke(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
          // We will detect this in our rendering loop
          glfwSetWindowShouldClose(window, GL_TRUE);
        }

        // whether key is pressed or held down
        final boolean active = action == GLFW_PRESS || action == GLFW_REPEAT;

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

        // actions
        if (key == GLFW_KEY_SPACE) {
          mov.jump.set(action == GLFW_PRESS);
        }
      }
    });

    /*
     * Mouse callbacks
     */
    display.setMouseButtonCallback(new GLFWMouseButtonCallback() {
      @Override
      public void invoke(long window, int button, int action, int mods) {
        final boolean pressed = action != GLFW_RELEASE;
        switch (button) {
          case GLFW_MOUSE_BUTTON_LEFT:
            MouseButton.LEFT.setPressed(pressed);
            break;
          case GLFW_MOUSE_BUTTON_RIGHT:
            MouseButton.RIGHT.setPressed(pressed);
            break;
        }
      }
    });

    display.setCursorPosCallback(new GLFWCursorPosCallback() {
      @Override
      public void invoke(long window, double xpos, double ypos) {
        MouseButton.LEFT.setPosition((int) xpos, (int) ypos);
        MouseButton.RIGHT.setPosition((int) xpos, (int) ypos);
      }
    });

    display.setScrollCallback(new GLFWScrollCallback() {
      @Override
      public void invoke(long window, double xoffset, double yoffset) {
        scrolls.offer(new Scroll(xoffset, yoffset));
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
