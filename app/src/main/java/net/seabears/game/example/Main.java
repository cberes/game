package net.seabears.game.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.entities.Entity;
import net.seabears.game.entities.EntityLight;
import net.seabears.game.entities.EntityTexture;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.Player;
import net.seabears.game.guis.GuiTexture;
import net.seabears.game.guis.fonts.GuiText;
import net.seabears.game.guis.fonts.TextAttr;
import net.seabears.game.guis.fonts.creator.FontType;
import net.seabears.game.guis.fonts.creator.MetaFile;
import net.seabears.game.guis.fonts.creator.TextMeshCreator;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.particles.ParticleSystem;
import net.seabears.game.particles.ParticleTexture;
import net.seabears.game.render.DisplayManager;
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
import net.seabears.game.util.Volume;
import net.seabears.game.util.normalmap.NormalMappedObjFileLoader;
import net.seabears.game.water.Water;
import net.seabears.game.water.WaterTile;

public class Main extends App {
  private static final float FOV = 70.0f;
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 1000.0f;
  private static final float GRAVITY = -32.0f;
  private static final Vector3f SKY_COLOR = new Vector3f(0.5f);
  private static final long DAY_LENGTH_MS = TimeUnit.HOURS.toMillis(1L);
  private static final int NUM_ENTITIES = 100;

  private Player player;
  private Skybox skybox;
  private final List<Entity> entities = new ArrayList<>();
  private final List<Entity> nmEntities = new ArrayList<>();
  private final List<Terrain> terrains = new ArrayList<>();
  private final List<WaterTile> water = new ArrayList<>();
  private final List<ParticleSystem> particles = new ArrayList<>();
  private final List<Light> lights = new ArrayList<>();
  private final List<GuiTexture> guis = new ArrayList<>();
  private final List<GuiText> text = new ArrayList<>();

  public Main() {
    super(FOV, NEAR_PLANE, FAR_PLANE, SKY_COLOR);
  }

  @Override
  protected void init(final DisplayManager display, final Loader loader, final FpsCalc fps, final Matrix4f projMatrix) throws IOException {
    final DayNightCycle cycle = new DayNightCycle(DAY_LENGTH_MS, TimeUnit.MILLISECONDS, () -> System.currentTimeMillis());
    skybox = new Skybox(cycle, SkyboxRenderer.loadCube(loader, "skybox-stormy/"), SkyboxRenderer.loadCube(loader, "skybox-night/"));

    /*
     * lights, camera, ...
     */
    lights.add(new Light(new Vector3f(0.0f, 10000.0f, -7000.0f), new Vector3f(1.0f), () -> Math.max(cycle.ratio(), 0.2f)));
    lights.add(new Light(new Vector3f(), new Vector3f(2.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    lights.add(new Light(new Vector3f(), new Vector3f(0.0f, 2.0f, 2.0f), new Vector3f(1.0f, 0.01f, 0.002f)));
    lights.add(new Light(new Vector3f(), new Vector3f(2.0f, 2.0f, 0.0f), new Vector3f(1.0f, 0.01f, 0.002f)));

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
     * entities
     */
    entities.add(player);
    final Random rand = new Random();
    entities.add(new Entity(new EntityTexture(lamp), position(800, 100, terrains), new Vector3f(), 1.0f, new EntityLight(lights.get(1), new Vector3f(0.0f, 10.0f, 0.0f))));
    entities.add(new Entity(new EntityTexture(lamp), position(750, 50, terrains), new Vector3f(), 1.0f, new EntityLight(lights.get(2), new Vector3f(0.0f, 10.0f, 0.0f))));
    entities.add(new Entity(new EntityTexture(lamp), position(850, 50, terrains), new Vector3f(), 1.0f, new EntityLight(lights.get(3), new Vector3f(0.0f, 10.0f, 0.0f))));
    for (int i = 0; i < NUM_ENTITIES; ++i) {
      entities.add(new Entity(new EntityTexture(tree), position(rand, terrains), new Vector3f(), 0.8f));
      entities.add(new Entity(new EntityTexture(lowPolyTree, rand.nextInt(4)), position(rand, terrains), new Vector3f(), 0.4f));
    }
    for (int i = 0; i < NUM_ENTITIES * 4; ++i) {
      entities.add(new Entity(new EntityTexture(fern, rand.nextInt(4)), position(rand, terrains), new Vector3f(), 0.6f));
    }
    for (int i = 0; i < 10; ++i) {
      entities.add(new Entity(new EntityTexture(stall), position(rand, terrains), new Vector3f(0.0f, rand.nextInt(360), 0.0f), 1.0f));
    }

    /*
     * normal-mapped entities
     */
    nmEntities.add(new Entity(new EntityTexture(barrel), position(800, 25, terrains), new Vector3f(), 1.0f));

    /*
     * particles
     */
    particles.add(new SimpleParticleSystem(
        new ParticleTexture(loader.loadTexture("flare-particle"), 4),
        new Vector3f(player.getPosition()), 1.0f, GRAVITY, 2.0f, 5));

    /*
     * GUIs
     */
    final Vector2f guiScale = new Vector2f(0.1f, display.getWidth() / (float) display.getHeight() * 0.1f);
    guis.add(new GuiTexture(loader.loadTexture("winnie"), new Vector2f(0.7f, 0.7f), guiScale));

    /*
     * text
     */
    final FontType liberation = new FontType(loader.loadTexture("fonts/liberation"), new TextMeshCreator(new MetaFile(new File("src/main/res/fonts/liberation.fnt"), display.getWidth(), display.getHeight(), 8)));
    text.add(new GuiText("Winnie Land!", new Vector2f(0.0f, 0.0f), 1.0f, true, liberation, 3.0f,
        new TextAttr(new Vector3f(0.0f, 1.0f, 1.0f), 0.5f, 0.1f),
        new TextAttr(new Vector3f(1.0f, 0.0f, 1.0f), 0.4f, 0.5f, new Vector2f(0.006f))));
  }

  @Override
  protected Player getPlayer() {
    return player;
  }

  @Override
  protected Map<EntityTexture, GuiTexture> getMenuGuis() {
    return Collections.emptyMap();
  }

  @Override
  protected List<GuiTexture> getGuis() {
    return guis;
  }

  @Override
  protected List<GuiText> getGuiText() {
    return text;
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
  protected List<ParticleSystem> getParticleSystems() {
    return particles;
  }

  @Override
  protected List<WaterTile> getWater() {
    return water;
  }

  @Override
  protected List<Light> getLights() {
    return lights;
  }

  @Override
  protected Skybox getSkybox() {
    return skybox;
  }

  private static Vector3f position(float x, float z, List<Terrain> terrains) {
    return new Vector3f(x, Terrain.getHeight(terrains, x, z), z);
  }

  private static Vector3f position(Random rand, List<Terrain> terrains) {
    final float x = rand.nextFloat() * 1600;
    final float z = rand.nextFloat() * 800;
    return position(x, z, terrains);
  }

  public static void main(String[] argv) {
    new Main().run();
  }
}
