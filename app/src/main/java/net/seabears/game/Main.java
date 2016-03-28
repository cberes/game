package net.seabears.game;

import static java.util.stream.IntStream.range;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.EntityLight;
import net.seabears.game.entities.EntityRenderer;
import net.seabears.game.entities.EntityTexture;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.Player;
import net.seabears.game.entities.StaticShader;
import net.seabears.game.entities.normalmap.NormalMappingRenderer;
import net.seabears.game.entities.normalmap.NormalMappingShader;
import net.seabears.game.guis.GuiBuilder;
import net.seabears.game.guis.GuiRenderer;
import net.seabears.game.guis.GuiShader;
import net.seabears.game.guis.GuiTexture;
import net.seabears.game.guis.fonts.FontRenderer;
import net.seabears.game.guis.fonts.FontShader;
import net.seabears.game.guis.fonts.GuiText;
import net.seabears.game.guis.fonts.TextAttr;
import net.seabears.game.guis.fonts.TextMaster;
import net.seabears.game.guis.fonts.creator.FontType;
import net.seabears.game.guis.fonts.creator.MetaFile;
import net.seabears.game.guis.fonts.creator.TextMeshCreator;
import net.seabears.game.input.CameraPanTilt;
import net.seabears.game.input.DirectionKeys;
import net.seabears.game.input.GuiPicker;
import net.seabears.game.input.MouseButton;
import net.seabears.game.input.MousePicker;
import net.seabears.game.input.MovementKeys;
import net.seabears.game.input.Scroll;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.particles.ParticleMaster;
import net.seabears.game.particles.ParticleRenderer;
import net.seabears.game.particles.ParticleShader;
import net.seabears.game.particles.ParticleTexture;
import net.seabears.game.particles.SpiralParticleSystem;
import net.seabears.game.render.DisplayManager;
import net.seabears.game.render.Loader;
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.Renderer;
import net.seabears.game.render.FrameBuffer;
import net.seabears.game.skybox.Skybox;
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
import net.seabears.game.util.normalmap.NormalMappedObjFileLoader;
import net.seabears.game.water.Water;
import net.seabears.game.water.WaterFrameBuffers;
import net.seabears.game.water.WaterRenderer;
import net.seabears.game.water.WaterShader;
import net.seabears.game.water.WaterTile;

public class Main {
  private static final float FOV = 70.0f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000.0f;
  private static final float GRAVITY = -32.0f;
  private static final Vector3f SKY_COLOR = new Vector3f(0.5f);
  private static final float SKYBOX_SIZE = 500.0f;
  private static final long DAY_LENGTH_MS = TimeUnit.HOURS.toMillis(1L);
  private static final int MAX_LIGHTS = 4;
  private static final float MAX_TERRAIN_RANGE = 600.0f;
  private static final Vector4f HIGH_PLANE = new Vector4f(0.0f, 1.0f, 0.0f, -1000.0f);

  private static final int WATER_REFLECTION_WIDTH = 320;
  private static final int WATER_REFLECTION_HEIGHT = 180;
  private static final int WATER_REFRACTION_WIDTH = 1280;
  private static final int WATER_REFRACTION_HEIGHT = 720;

  public void run() {
    final DirectionKeys dirKeys = new DirectionKeys();
    final MovementKeys movKeys = new MovementKeys();
    final BlockingQueue<Scroll> scrolls = new LinkedBlockingDeque<>();
    final Loader loader = new Loader();
    final List<Renderer> renderers = new ArrayList<>();
    try (DisplayManager display = new DisplayManager("Game Engine", 800, 600)) {
      final CameraPanTilt panTilt = new CameraPanTilt(display.getWidth(), display.getHeight(), MouseButton.RIGHT);
      init(display, dirKeys, movKeys, scrolls, panTilt);
      renderers.addAll(loop(display, loader, dirKeys, movKeys, scrolls, panTilt));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      renderers.forEach(r -> r.close());
      loader.close();
    }
  }

  private List<Renderer> loop(final DisplayManager display, final Loader loader, final DirectionKeys dir, final MovementKeys mov, final BlockingQueue<Scroll> scrolls, final CameraPanTilt panTilt) throws IOException {
    final FpsCalc fps = new FpsCalc();
    final DayNightCycle cycle = new DayNightCycle(DAY_LENGTH_MS, TimeUnit.MILLISECONDS, () -> System.currentTimeMillis());
    final Skybox skybox = new Skybox(cycle);

    /*
     * player
     */
    final TexturedModel playerModel = new TexturedModel(loader.loadToVao(ObjFileLoader.load("bunny")), new ModelTexture(loader.loadTexture("bunny"), 1.0f, 5.0f));
    final Player player = new Player(new EntityTexture(playerModel), new Vector3f(0, 0, -40), new Vector3f(0.0f, 180.0f, 0.0f), 0.5f, fps, new Volume(5, 4), 20.0f, 160.0f, -GRAVITY * 0.5f, GRAVITY);

    /*
     * lights, camera, ...
     */
    final List<Light> lights = new ArrayList<>();
    lights.add(new Light(new Vector3f(0.0f, 10000.0f, -7000.0f), new Vector3f(1.0f), () -> Math.max(cycle.ratio(), 0.2f)));
    lights.add(new Light(new Vector3f(), new Vector3f(2.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    lights.add(new Light(new Vector3f(), new Vector3f(0.0f, 2.0f, 2.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    lights.add(new Light(new Vector3f(), new Vector3f(2.0f, 2.0f, 0.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    final Camera camera = new Camera(player);

    /*
     * rendering
     */
    final ProjectionMatrix projMatrix = new ProjectionMatrix(display.getWidth(), display.getHeight(), FOV, NEAR_PLANE, FAR_PLANE);
    final StaticShader shader = new StaticShader(MAX_LIGHTS);
    final EntityRenderer entityRenderer = new EntityRenderer(shader, projMatrix.toMatrix());
    final NormalMappingShader nmShader = new NormalMappingShader(MAX_LIGHTS);
    final NormalMappingRenderer nmRenderer = new NormalMappingRenderer(nmShader, projMatrix.toMatrix());
    final ParticleShader particleShader = new ParticleShader(MAX_LIGHTS);
    final ParticleRenderer particleRenderer = new ParticleRenderer(loader, particleShader, projMatrix.toMatrix(), 0.5f);
    final TerrainShader terrainShader = new TerrainShader(MAX_LIGHTS);
    final TerrainRenderer terrainRenderer = new TerrainRenderer(terrainShader, projMatrix.toMatrix());
    final SkyboxRenderer skyboxRenderer = new SkyboxRenderer(loader,
        new SkyboxShader(fps, 1.0f), projMatrix.toMatrix(), SKYBOX_SIZE,
        SkyboxRenderer.loadCube(loader, "skybox-stormy/"),
        SkyboxRenderer.loadCube(loader, "skybox-night/"));
    final MasterRenderer renderer = new MasterRenderer(SKY_COLOR, entityRenderer, nmRenderer, terrainRenderer, skyboxRenderer);
    final GuiRenderer guiRenderer = new GuiRenderer(loader, new GuiShader());

    /*
     * water rendering
     */
    final FrameBuffer reflection = new FrameBuffer(WATER_REFLECTION_WIDTH, WATER_REFLECTION_HEIGHT, display.getWidth(), display.getHeight(), true);
    final FrameBuffer refraction = new FrameBuffer(WATER_REFRACTION_WIDTH, WATER_REFRACTION_HEIGHT, display.getWidth(), display.getHeight(), false);
    final WaterFrameBuffers waterFbs = new WaterFrameBuffers(reflection, refraction);
    final WaterRenderer waterRenderer = new WaterRenderer(loader,
        new WaterShader(MAX_LIGHTS), projMatrix.toMatrix(),
        waterFbs, loader.loadTexture("water/dudv"),
        loader.loadTexture("water/normal"),
        NEAR_PLANE, FAR_PLANE);

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
    final TexturedModel barrel = new TexturedModel(loader.loadToVao(NormalMappedObjFileLoader.load("barrel")),
        new ModelTexture(loader.loadTexture("barrel"), loader.loadTexture("barrel-normal"), 1, 0.5f, 10.0f, false, false));

    /*
     * textures that can be added to the world
     */
    final List<EntityTexture> textures = new ArrayList<>();
    textures.add(new EntityTexture(lamp));
    textures.add(new EntityTexture(tree));
    textures.add(new EntityTexture(stall));
    range(0, 4).forEach(i -> textures.add(new EntityTexture(lowPolyTree, i)));
    range(0, 4).forEach(i -> textures.add(new EntityTexture(fern, i)));

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
    entities.add(new Entity(new EntityTexture(lamp), new Vector3f(185.0f, -4.7f, -293.0f), new Vector3f(), 1.0f, new EntityLight(lights.get(1), new Vector3f(0.0f, 10.0f, 0.0f))));
    entities.add(new Entity(new EntityTexture(lamp), new Vector3f(370.0f,  4.2f, -300.0f), new Vector3f(), 1.0f, new EntityLight(lights.get(2), new Vector3f(0.0f, 10.0f, 0.0f))));
    entities.add(new Entity(new EntityTexture(lamp), new Vector3f(293.0f, -6.8f, -305.0f), new Vector3f(), 1.0f, new EntityLight(lights.get(3), new Vector3f(0.0f, 10.0f, 0.0f))));
    for (int i = 0; i < numTrees; ++i) {
      entities.add(new Entity(new EntityTexture(tree), position(rand, terrains), new Vector3f(), 0.8f));
      entities.add(new Entity(new EntityTexture(lowPolyTree, rand.nextInt(4)), position(rand, terrains), new Vector3f(), 0.4f));
    }
    for (int i = 0; i < numTrees * 4; ++i) {
      entities.add(new Entity(new EntityTexture(fern, rand.nextInt(4)), position(rand, terrains), new Vector3f(), 0.6f));
    }
    for (int i = 0; i < 10; ++i) {
      entities.add(new Entity(new EntityTexture(stall), position(rand, terrains), new Vector3f(0.0f, rand.nextInt(360), 0.0f), 1.0f));
    }

    /*
     * normal-mapped entities
     */
    final List<Entity> nmEntities = new ArrayList<>();
    nmEntities.add(new Entity(new EntityTexture(barrel), new Vector3f(0, 10, -75), new Vector3f(), 1.0f));

    /*
     * particles
     */
    final ParticleMaster particles = new ParticleMaster(fps);
        final SpiralParticleSystem system = new SpiralParticleSystem(
                new ParticleTexture(loader.loadTexture("flare-particle"), 4), player, 1.0f, GRAVITY, 2.0f);

    /*
     * water
     */
    final List<WaterTile> waterTiles = new ArrayList<>();
    waterTiles.add(new WaterTile(new Water(fps, 0.03f, 1.0f), 115, -60, -2, 25, 30));

    /*
     * text
     */
    final FontType liberation = new FontType(loader.loadTexture("fonts/liberation"), new TextMeshCreator(new MetaFile(new File("src/main/res/fonts/liberation.fnt"), display.getWidth(), display.getHeight(), 8)));
    final List<GuiText> text = new ArrayList<>();
    final TextMaster textMaster = new TextMaster(loader, new FontRenderer(new FontShader()));
    text.add(new GuiText("Winnie Land!", new Vector2f(0.5f), 0.5f, false, liberation, 3.0f,
        new TextAttr(new Vector3f(0.0f, 1.0f, 1.0f), 0.5f, 0.1f),
        new TextAttr(new Vector3f(1.0f, 0.0f, 1.0f), 0.4f, 0.5f, new Vector2f(0.006f))));
    text.forEach(textMaster::load);

    /*
     * GUIs
     */
    // add GUIs for models that can be added to the scene
    final Vector2f guiScale = new Vector2f(0.1f, display.getWidth() / (float) display.getHeight() * 0.1f);
    final GuiBuilder guiBuilder = new GuiBuilder(guiScale.x, guiScale.y);
    // TODO make GUI take generic "action" objects... one of which will be to create new entities
    textures.forEach(guiBuilder::add);

    // create final list of GUIs for the window
    List<GuiTexture> guis = new ArrayList<>();
    guis.add(new GuiTexture(loader.loadTexture("winnie"), new Vector2f(0.7f, 0.7f), guiScale));
    guis.addAll(guiBuilder.getGuis().values());

    // pickers
    final MousePicker mousePicker = new MousePicker(MouseButton.LEFT, camera, projMatrix.toMatrix());
    final GuiPicker guiPicker = new GuiPicker(MouseButton.LEFT, mousePicker, guiBuilder.getGuis());

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

      // update mouse picker after camera has moved
      // GUI picker to create new entities in the scene
      final boolean guiActive = guiPicker.update(display.getWidth(), display.getHeight());
      guiPicker.getSelection().flatMap(t -> {
        entities.add(new Entity(t, new Vector3f(), new Vector3f(), 1.0f));
        return Optional.empty();
      });
      // mouse picker to move the previously created entity
      mousePicker.update(display.getWidth(), display.getHeight());
      if (!guiActive) {
        mousePicker.findTerrainPoint(terrains, MAX_TERRAIN_RANGE).flatMap(p -> {
          entities.get(entities.size() - 1).place(p);
          return Optional.empty();
        });
      }
      // TODO something something to resize and rotate the selected entity

      // particles
      particles.update(system.generate(fps.get()), camera);

      // render scene
      final Consumer<Vector4f> renderAction = p -> renderer.render(entities, nmEntities, terrains, lights, skybox, camera, p);
      waterRenderer.preRender(waterTiles, lights, camera, display, renderAction);
      renderAction.accept(HIGH_PLANE);
      waterRenderer.render(waterTiles, lights, camera);
      particleRenderer.render(particles.getParticles(), camera);
      guiRenderer.render(guis);
      textMaster.render();

      // I don't know if the stuff in here is necessary
      display.update();
    }
    return Arrays.asList(textMaster, guiRenderer, waterRenderer, entityRenderer, nmRenderer, particleRenderer, terrainRenderer, skyboxRenderer);
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
