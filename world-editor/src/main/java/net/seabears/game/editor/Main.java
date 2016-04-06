package net.seabears.game.editor;

import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.EntityTexture;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.Player;
import net.seabears.game.entities.SimpleRenderer;
import net.seabears.game.entities.SimpleShader;
import net.seabears.game.guis.GuiBuilder;
import net.seabears.game.guis.GuiTexture;
import net.seabears.game.guis.fonts.GuiText;
import net.seabears.game.input.GuiPicker;
import net.seabears.game.input.MouseButton;
import net.seabears.game.input.MousePicker;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.particles.ParticleSystem;
import net.seabears.game.render.DisplayManager;
import net.seabears.game.render.FrameBuffer;
import net.seabears.game.render.Loader;
import net.seabears.game.skybox.Skybox;
import net.seabears.game.skybox.SkyboxRenderer;
import net.seabears.game.terrains.FakePerlinNoise;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.textures.TerrainTexture;
import net.seabears.game.textures.TerrainTexturePack;
import net.seabears.game.util.App;
import net.seabears.game.util.DayNightCycle;
import net.seabears.game.util.FpsCalc;
import net.seabears.game.util.ObjFileLoader;
import net.seabears.game.util.ViewMatrix;
import net.seabears.game.util.Volume;
import net.seabears.game.water.Water;
import net.seabears.game.water.WaterTile;

public class Main extends App {
  private static final float FOV = 70.0f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000.0f;
  private static final float GRAVITY = -32.0f;
  private static final Vector3f SKY_COLOR = new Vector3f(0.5f);
  private static final long DAY_LENGTH_MS = TimeUnit.HOURS.toMillis(1L);
  private static final float MAX_TERRAIN_RANGE = 600.0f;

  private Camera camera;
  private Player player;
  private Skybox skybox;
  private GuiPicker guiPicker;
  private MousePicker mousePicker;
  private float debounce;
  private final List<Entity> entities = new ArrayList<>();
  private final List<Entity> nmEntities = new ArrayList<>();
  private final List<Terrain> terrains = new ArrayList<>();
  private final List<WaterTile> water = new ArrayList<>();
  private final List<Light> lights = new ArrayList<>();
  private final Map<EntityTexture, GuiTexture> menuGuis = new HashMap<>();
  private final List<GuiTexture> guis = new ArrayList<>();
  private final List<FrameBuffer> fbs = new ArrayList<>();
  private final Map<EntityTexture, Vector3f> savedRotation = new HashMap<>();
  private final Map<EntityTexture, Float> savedScale = new HashMap<>();

  public Main() {
    super(FOV, NEAR_PLANE, FAR_PLANE, SKY_COLOR);
  }

  @Override
  protected void init(final DisplayManager display, final Loader loader, final FpsCalc fps, final Matrix4f projMatrix) throws IOException {
    final DayNightCycle cycle = new DayNightCycle(DAY_LENGTH_MS, TimeUnit.MILLISECONDS, () -> System.currentTimeMillis());
    skybox = new Skybox(cycle, SkyboxRenderer.loadCube(loader, "skybox-stormy/"), SkyboxRenderer.loadCube(loader, "skybox-night/"));
    lights.add(new Light(new Vector3f(0.0f, 10000.0f, -7000.0f), new Vector3f(1.0f)));

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
    final long[] seeds = FakePerlinNoise.makeSeeds();
    terrains.add(new Terrain(0, 0, loader, terrainPack, terrainBlend, new FakePerlinNoise(0, 0, 70.0, 3, 0.3, new Random(), seeds)));
    terrains.add(new Terrain(1, 0, loader, terrainPack, terrainBlend, new FakePerlinNoise(1, 0, 70.0, 3, 0.3, new Random(), seeds)));

    /*
     * player
     */
    final TexturedModel playerModel = new TexturedModel(loader.loadToVao(ObjFileLoader.load("bunny")), new ModelTexture(loader.loadTexture("bunny"), 1.0f, 5.0f));
    player = new Player(new EntityTexture(playerModel), new Vector3f(), new Vector3f(), 0.5f, fps, new Volume(5, 4), 20.0f, 160.0f, -GRAVITY * 0.5f, GRAVITY);
    player.place(position(800, 0, terrains));
    camera = new Camera(player);

    /*
     * water
     */
    int dx = 100;
    int dz = 100;
    int x = 0;
    while (x < 1600) {
      int z = 0;
      while (z < 800) {
        water.add(new WaterTile(new Water(fps, 0.03f, 1.0f), x + (dx / 2), z + (dz / 2), -5, dx / 2, dz / 2));
        z += dz;
      }
      x += dx;
    }

    /*
     * textures that can be added to the world
     */
    final Map<EntityTexture, Float> textures = new HashMap<EntityTexture, Float>();
    textures.put(new EntityTexture(lamp), 0.5f);
    textures.put(new EntityTexture(tree), 0.25f);
    textures.put(new EntityTexture(stall), 0.5f);
    range(0, 4).forEach(i -> textures.put(new EntityTexture(lowPolyTree, i), 0.25f));
    range(0, 4).forEach(i -> textures.put(new EntityTexture(fern, i), 1.0f));

    /*
     * GUIs
     */
    // add GUIs for models that can be added to the scene
    final Vector2f guiScale = new Vector2f(0.1f, display.getWidth() / (float) display.getHeight() * 0.1f);
    final GuiBuilder guiBuilder = new GuiBuilder(guiScale.x, guiScale.y);
    // TODO make GUI take generic "action" objects... one of which will be to create new entities
    final Camera fakeCamera = new Camera(null, 0.0f, 0.0f);
    fakeCamera.move(new Vector3f(0.0f, 2.5f, 5.0f));
    fakeCamera.rotate(new Vector3f(0.0f, 0.0f, 180.0f));
    final Matrix4f fakeViewMatrix = new ViewMatrix(fakeCamera).toMatrix();
    final SimpleRenderer renderer = new SimpleRenderer(new SimpleShader(), projMatrix);
    for (Map.Entry<EntityTexture, Float> texture : textures.entrySet()) {
      final FrameBuffer fb = new FrameBuffer(display.getWidth(), display.getHeight(), display.getWidth(), display.getHeight(), true);
      fbs.add(fb);
      fb.bind();
      renderer.render(Collections.singletonList(new Entity(texture.getKey(), new Vector3f(), new Vector3f(), texture.getValue())), fakeViewMatrix);
      fb.unbind(display.getWidth(), display.getHeight());
      guiBuilder.add(texture.getKey(), fb.getTexture());
    }
    menuGuis.putAll(guiBuilder.getGuis());
    renderer.close();

    // create final list of GUIs for the window
    guis.addAll(guiBuilder.getGuis().values());

    // pickers
    mousePicker = new MousePicker(MouseButton.LEFT, camera, projMatrix);
    guiPicker = new GuiPicker(MouseButton.LEFT, mousePicker, getMenuGuis());
  }

  private static Vector3f position(float x, float z, List<Terrain> terrains) {
    return new Vector3f(x, Terrain.getHeight(terrains, x, z), z);
  }

  @Override
  protected Camera getCamera() {
    return camera;
  }

  @Override
  protected Player getPlayer() {
    return player;
  }

  @Override
  protected Map<EntityTexture, GuiTexture> getMenuGuis() {
    return menuGuis;
  }

  @Override
  protected List<GuiTexture> getGuis() {
    return guis;
  }

  @Override
  protected List<GuiText> getGuiText() {
    return Collections.emptyList();
  }

  @Override
  protected List<Entity> getEntities() {
    return entities;
  }

  @Override
  protected List<Entity> getNormalMapEntities() {
    return nmEntities;
  }

  @Override
  protected List<Terrain> getTerrain() {
    return terrains;
  }

  @Override
  protected List<WaterTile> getWater() {
    return water;
  }

  @Override
  protected List<ParticleSystem> getParticleSystems() {
    return Collections.emptyList();
  }

  @Override
  protected List<Light> getLights() {
    return lights;
  }

  @Override
  protected Skybox getSkybox() {
    return skybox;
  }

  @Override
  protected Function<EntityTexture, Optional<Object>> getGuiAction() {
    return t -> {
      entities.add(new Entity(t, new Vector3f(), new Vector3f(), 1.0f));
      return Optional.empty();
    };
  }

  @Override
  protected Function<Vector3f, Optional<Object>> getMouseAction() {
    return p -> {
      entities.get(entities.size() - 1).place(p);
      return Optional.empty();
    };
  }

  @Override
  protected void update(final DisplayManager display, final Matrix4f viewMatrix, final float secondsDelta) {
    // GUI picker to create new entities in the scene
    final boolean guiActive = guiPicker.update(display.getWidth(), display.getHeight());
    guiPicker.getSelection().map(t -> {
      entities.add(new Entity(t, new Vector3f(), savedRotation.getOrDefault(t, new Vector3f()), savedScale.getOrDefault(t, 1.0f)));
      return null;
    });

    // mouse picker to move the previously created entity
    mousePicker.update(display.getWidth(), display.getHeight(), viewMatrix);
    if (!guiActive) {
      mousePicker.findTerrainPoint(getTerrain(), MAX_TERRAIN_RANGE).map(p -> {
        if (!entities.isEmpty()) {
          entities.get(entities.size() - 1).place(p);
        }
        return null;
      });
    }

    // modify last entity
    if (!entities.isEmpty()) {
      final int index = entities.size() - 1;
      boolean changed = false;

      // scale
      if (getDirections().up.get()) {
        entities.get(index).increaseScale(1.0f * secondsDelta);
        changed = true;
      }
      if (getDirections().down.get()) {
        entities.get(index).increaseScale(-1.0f * secondsDelta);
        changed = true;
      }

      // rotate
      if (getDirections().right.get()) {
        entities.get(index).increaseRotation(new Vector3f(0.0f, 10.0f * secondsDelta, 0.0f));
        changed = true;
      }
      if (getDirections().left.get()) {
        entities.get(index).increaseRotation(new Vector3f(0.0f, -10.0f * secondsDelta, 0.0f));
        changed = true;
      }

      if (changed) {
        final EntityTexture tex = entities.get(index).getTexture();
        savedRotation.put(tex, entities.get(index).getRotation());
        savedScale.put(tex, entities.get(index).getScale());
      }

      // remove
      // TODO move button debouncing to a class
      if (getActions().delete.get()) {
        // delete at most one entity per second
        if (debounce == 0.0f || secondsDelta + debounce >= 1.0f) {
          entities.remove(index);
          debounce = 0.0f;
        }
        debounce += secondsDelta;
      } else {
        debounce = 0.0f;
      }
    }
  }

  @Override
  protected void close() {
    fbs.forEach(fb -> fb.close());
  }

  public static void main(String[] argv) {
    new Main().run();
  }
}
