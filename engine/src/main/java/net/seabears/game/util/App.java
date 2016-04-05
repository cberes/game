package net.seabears.game.util;

import static java.util.stream.Collectors.toCollection;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.EntityRenderer;
import net.seabears.game.entities.EntityTexture;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.Player;
import net.seabears.game.entities.StaticShader;
import net.seabears.game.entities.normalmap.NormalMappingRenderer;
import net.seabears.game.entities.normalmap.NormalMappingShader;
import net.seabears.game.guis.GuiRenderer;
import net.seabears.game.guis.GuiShader;
import net.seabears.game.guis.GuiTexture;
import net.seabears.game.guis.fonts.FontRenderer;
import net.seabears.game.guis.fonts.FontShader;
import net.seabears.game.guis.fonts.GuiText;
import net.seabears.game.guis.fonts.TextMaster;
import net.seabears.game.input.ActionKeys;
import net.seabears.game.input.CameraPanTilt;
import net.seabears.game.input.DirectionKeys;
import net.seabears.game.input.MouseButton;
import net.seabears.game.input.MovementKeys;
import net.seabears.game.input.Scroll;
import net.seabears.game.particles.Particle;
import net.seabears.game.particles.ParticleMaster;
import net.seabears.game.particles.ParticleRenderer;
import net.seabears.game.particles.ParticleShader;
import net.seabears.game.particles.ParticleSystem;
import net.seabears.game.render.DisplayManager;
import net.seabears.game.render.FrameBuffer;
import net.seabears.game.render.Loader;
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.Renderer;
import net.seabears.game.shadows.ShadowBox;
import net.seabears.game.shadows.ShadowMapRenderer;
import net.seabears.game.shadows.ShadowMapShader;
import net.seabears.game.skybox.Skybox;
import net.seabears.game.skybox.SkyboxRenderer;
import net.seabears.game.skybox.SkyboxShader;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.terrains.TerrainRenderer;
import net.seabears.game.terrains.TerrainShader;
import net.seabears.game.water.WaterFrameBuffers;
import net.seabears.game.water.WaterRenderer;
import net.seabears.game.water.WaterShader;
import net.seabears.game.water.WaterTile;

public abstract class App {
  public static final float SKYBOX_SIZE = 500.0f;
  public static final int MAX_LIGHTS = 4;

  private static final Vector4f HIGH_PLANE = new Vector4f(0.0f, 1.0f, 0.0f, -1000.0f);
  private static final int MAX_PARTICLES = 10000;
  private static final int SHADOW_MAP_SIZE = 4096;

  private static final int WATER_REFLECTION_WIDTH = 320;
  private static final int WATER_REFLECTION_HEIGHT = 180;
  private static final int WATER_REFRACTION_WIDTH = 1280;
  private static final int WATER_REFRACTION_HEIGHT = 720;

  private final float fov;
  private final float nearPlane;
  private final float farPlane;
  private final Vector3f skyColor;
  private final ActionKeys actions;
  private final DirectionKeys directions;
  private final MovementKeys movement;
  private final BlockingQueue<Scroll> scrolls;

  public App(float fov, float nearPlane, float farPlane, Vector3f skyColor) {
    this.fov = fov;
    this.nearPlane = nearPlane;
    this.farPlane = farPlane;
    this.skyColor = skyColor;
    // TODO refactor this dependency inversion
    this.actions = new ActionKeys();
    this.directions = new DirectionKeys();
    this.movement = new MovementKeys();
    this.scrolls = new LinkedBlockingDeque<>();
  }

  public void run() {
    final Loader loader = new Loader();
    final List<Renderer> renderers = new ArrayList<>();
    try (DisplayManager display = new DisplayManager("Game Engine", 800, 600)) {
      final CameraPanTilt panTilt = new CameraPanTilt(display.getWidth(), display.getHeight(), MouseButton.RIGHT);
      init(display, panTilt);
      renderers.addAll(loop(display, loader, panTilt));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      renderers.forEach(r -> r.close());
      loader.close();
    }
  }

  protected abstract Camera getCamera();

  protected abstract Player getPlayer();

  protected abstract Map<EntityTexture, GuiTexture> getMenuGuis();

  protected abstract List<GuiTexture> getGuis();

  protected abstract List<GuiText> getGuiText();

  protected abstract List<Entity> getEntities();

  protected abstract List<Entity> getNormalMapEntities();

  protected abstract List<Terrain> getTerrain();

  protected abstract List<WaterTile> getWater();

  protected abstract List<ParticleSystem> getParticleSystems();

  protected abstract List<Light> getLights();

  protected abstract Skybox getSkybox();

  protected abstract void init(DisplayManager display, Loader loader, FpsCalc fps, Matrix4f projMatrix) throws IOException;

  protected Function<EntityTexture, Optional<Object>> getGuiAction() {
    return t -> Optional.empty();
  }

  protected Function<Vector3f, Optional<Object>> getMouseAction() {
    return v -> Optional.empty();
  }

  protected ActionKeys getActions() {
    return actions;
  }

  protected DirectionKeys getDirections() {
    return directions;
  }

  protected MovementKeys getMovement() {
    return movement;
  }

  protected void update(DisplayManager display, Matrix4f viewMatrix, float secondsDelta) {};

  protected void close() {};

  private List<Renderer> loop(final DisplayManager display, final Loader loader, final CameraPanTilt panTilt) throws IOException {
    // frame-rate stuff
    final FpsCalc fps = new FpsCalc();
    final FpsCounter fpsCount = new FpsCounter();

    // tell the app to fire it up
    final ProjectionMatrix projMatrix = new ProjectionMatrix(display.getWidth(), display.getHeight(), fov, nearPlane, farPlane);
    init(display, loader, fps, projMatrix.toMatrix());

    /*
     * player
     */
    final Player player = getPlayer();
    final Camera camera = getCamera();

    /*
     * rendering
     */
    final StaticShader shader = new StaticShader(MAX_LIGHTS);
    final EntityRenderer entityRenderer = new EntityRenderer(shader, projMatrix.toMatrix());
    final NormalMappingShader nmShader = new NormalMappingShader(MAX_LIGHTS);
    final NormalMappingRenderer nmRenderer = new NormalMappingRenderer(nmShader, projMatrix.toMatrix());
    final ParticleShader particleShader = new ParticleShader();
    final ParticleRenderer particleRenderer = new ParticleRenderer(loader, particleShader, projMatrix.toMatrix(), 0.5f, MAX_PARTICLES);
    final TerrainShader terrainShader = new TerrainShader(MAX_LIGHTS);
    final TerrainRenderer terrainRenderer = new TerrainRenderer(terrainShader, projMatrix.toMatrix());
    final Skybox skybox = getSkybox();
    final SkyboxRenderer skyboxRenderer = new SkyboxRenderer(loader, new SkyboxShader(fps, 1.0f),
        projMatrix.toMatrix(), SKYBOX_SIZE, skybox.getDayTextureId(), skybox.getNightTextureId());
    final ShadowMapRenderer shadowRenderer = new ShadowMapRenderer(new ShadowMapShader(),
        new ShadowBox(camera, fov, nearPlane, 150, 10, display.getWidth(), display.getHeight()),
        new FrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, display.getWidth(), display.getHeight()), 2);
    final MasterRenderer renderer = new MasterRenderer(skyColor, entityRenderer, nmRenderer, terrainRenderer, skyboxRenderer, shadowRenderer);
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
        nearPlane, farPlane);

    /*
     * particles
     */
    final ParticleMaster particles = new ParticleMaster(fps);
    getParticleSystems().forEach(particles::add);

    /*
     * text
     */
    final TextMaster textMaster = new TextMaster(loader, new FontRenderer(new FontShader()));
    getGuiText().forEach(textMaster::load);

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    final List<Scroll> currentScrolls = new LinkedList<>();
    while (display.isRunning()) {
      // update timing
      fps.update();

      // move player
      player.move(movement, (x, z) -> Terrain.getHeight(getTerrain(), x, z));

      // move camera (after player)
      currentScrolls.clear();
      scrolls.drainTo(currentScrolls);
      currentScrolls.forEach(camera::zoom);
      camera.panTilt(panTilt.get());
      camera.move();
      final Matrix4f viewMatrix = new ViewMatrix(camera).toMatrix();

      // let subclass update after camera and player have moved
      update(display, viewMatrix, fps.get());

      // particles
      particles.update(camera);

      // lights
      final List<Light> lights = getLights();

      // view-frustum culling
      final Frustum frustum = new Frustum(camera.getPosition(), viewMatrix, fov, nearPlane, farPlane, display.getWidth() / display.getHeight());
      final List<Entity> entitiesInView = getEntities().stream()
          .filter(e -> frustum.contains(e.getPosition(), 0.0f))
          .collect(toCollection(LinkedList::new));
      final List<Entity> nmEntitiesInView = getNormalMapEntities().stream()
          .filter(e -> frustum.contains(e.getPosition(), 0.0f))
          .collect(toCollection(LinkedList::new));
      final List<Particle> particlesInView = particles.getParticles().stream()
          .filter(p -> frustum.contains(p.getPosition(), 0.0f))
          .collect(toCollection(LinkedList::new));
      final List<Terrain> terrainsInView = getTerrain().stream()
          .filter(t -> frustum.contains(t.getPosition(), t.getRadius()))
          .collect(toCollection(LinkedList::new));
      final List<WaterTile> waterTilesInView = getWater().stream()
          .filter(w -> frustum.contains(w.getPosition(), w.getRadius()))
          .collect(toCollection(LinkedList::new));

      // render scene
      renderer.renderShadowMap(entitiesInView, nmEntitiesInView, lights, display.getWidth(), display.getHeight());
      // TODO some objects might be excluded when rendering water (because of frustum culling)
      final BiConsumer<Matrix4f, Vector4f> renderAction = (v, p) -> renderer.render(entitiesInView, nmEntitiesInView, terrainsInView, lights, skybox, v, p);
      waterRenderer.preRender(waterTilesInView, lights, camera, display, renderAction);
      renderAction.accept(viewMatrix, HIGH_PLANE);
      waterRenderer.render(waterTilesInView, lights, viewMatrix, camera.getPosition());
      particleRenderer.render(ParticleMaster.sortParticles(particlesInView), viewMatrix);
      guiRenderer.render(getGuis());
      textMaster.render();

      // update the screen
      display.update();

      // update rendering statistics
      if (fpsCount.update(fps.get())) {
        display.setTitle(String.format("FPS: %d, Entities: %d, Particles: %d, Terrain: %d, Water: %d",
            fpsCount.get(), entitiesInView.size() + nmEntitiesInView.size(), particlesInView.size(),
            terrainsInView.size(), waterTilesInView.size()));
      }

      // display some debugging info
      if (actions.debug.get()) {
        System.out.println(frustum);
        System.out.println(viewMatrix);
      }
    }
    close();
    return Arrays.asList(textMaster, guiRenderer, waterRenderer, entityRenderer, nmRenderer, particleRenderer, terrainRenderer, skyboxRenderer, shadowRenderer);
  }

  private void init(final DisplayManager display, final CameraPanTilt panTilt) {
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

        // modifiers
        final boolean shift = (mods & GLFW_MOD_SHIFT) > 0;

        // whether key is pressed or held down
        final boolean active = action == GLFW_PRESS || action == GLFW_REPEAT;

        // directions
        if (key == GLFW_KEY_UP) {
          directions.up.set(active);
        }
        if (key == GLFW_KEY_DOWN) {
          directions.down.set(active);
        }
        if (key == GLFW_KEY_RIGHT) {
          directions.right.set(active);
        }
        if (key == GLFW_KEY_LEFT) {
          directions.left.set(active);
        }

        // movement
        if (key == GLFW_KEY_W) {
          movement.forward.set(active);
        }
        if (key == GLFW_KEY_S) {
          movement.backward.set(active);
        }
        if (key == GLFW_KEY_D) {
          movement.right.set(active);
        }
        if (key == GLFW_KEY_A) {
          movement.left.set(active);
        }

        // actions
        if (key == GLFW_KEY_SPACE) {
          movement.jump.set(action == GLFW_PRESS);
        }
        if (key == GLFW_KEY_BACKSPACE) {
          actions.back.set(action == GLFW_PRESS);
        }
        if (key == GLFW_KEY_DELETE) {
          actions.delete.set(action == GLFW_PRESS);
        }
        if (key == GLFW_KEY_E) {
          actions.interact.set(action == GLFW_PRESS);
        }
        if (key == GLFW_KEY_GRAVE_ACCENT) {
          actions.debug.set(action == GLFW_PRESS && shift);
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
}
