package net.seabears.game.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.terrains.Terrain;

public class MasterRenderer implements AutoCloseable {
  public static void enableCulling() {
    // don't render triangles facing away from the camera
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);
  }

  public static void disableCulling() {
    GL11.glDisable(GL11.GL_CULL_FACE);
  }

  private final Vector3f skyColor;
  private final EntityRenderer entityRenderer;
  private final TerrainRenderer terrainRenderer;
  private final Map<TexturedModel, List<Entity>> entities;
  private final List<Terrain> terrains;

  public MasterRenderer(Vector3f skyColor, EntityRenderer renderer, TerrainRenderer terrainRenderer) {
    this.skyColor = skyColor;
    this.entityRenderer = renderer;
    this.terrainRenderer = terrainRenderer;
    this.entities = new HashMap<>();
    this.terrains = new ArrayList<>();
    enableCulling();
  }

  public void prepare() {
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glClearColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
  }

  public void add(Entity entity) {
    TexturedModel model = entity.getModel();
    if (!entities.containsKey(model)) {
      entities.put(model, new ArrayList<>());
    }
    entities.get(model).add(entity);
  }

  public void add(Terrain terrain) {
    terrains.add(terrain);
  }

  public void render(final List<Light> lights, final Camera camera) {
    this.prepare();
    entityRenderer.getShader().start();
    entityRenderer.getShader().loadSky(skyColor);
    entityRenderer.getShader().loadLights(lights);
    entityRenderer.getShader().loadViewMatrix(camera);
    entityRenderer.render(entities);
    entityRenderer.getShader().stop();
    entities.clear();

    terrainRenderer.getShader().start();
    terrainRenderer.getShader().loadSky(skyColor);
    terrainRenderer.getShader().loadLights(lights);
    terrainRenderer.getShader().loadViewMatrix(camera);
    terrainRenderer.render(terrains);
    terrainRenderer.getShader().stop();
    terrains.clear();
  }

  @Override
  public void close() {
    entityRenderer.getShader().close();
    terrainRenderer.getShader().close();
  }
}
