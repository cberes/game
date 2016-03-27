package net.seabears.game.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.EntityRenderer;
import net.seabears.game.entities.Light;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.skybox.Skybox;
import net.seabears.game.skybox.SkyboxRenderer;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.terrains.TerrainRenderer;

public class MasterRenderer {
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
  private final SkyboxRenderer skyboxRenderer;
  private final Map<TexturedModel, List<Entity>> entities;

  public MasterRenderer(Vector3f skyColor, EntityRenderer renderer, TerrainRenderer terrainRenderer, SkyboxRenderer skyboxRenderer) {
    this.skyColor = skyColor;
    this.entityRenderer = renderer;
    this.terrainRenderer = terrainRenderer;
    this.skyboxRenderer = skyboxRenderer;
    this.entities = new HashMap<>();
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

  public void render(List<Entity> entities, List<Terrain> terrains, List<Light> lights, Skybox skybox, Camera camera, Vector4f clippingPlane) {
    entities.forEach(this::add);
    prepare();
    render(terrains, lights, skybox, camera, clippingPlane);
    this.entities.clear();
  }

  private void render(List<Terrain> terrains, List<Light> lights, Skybox skybox, Camera camera, Vector4f clippingPlane) {
    // entities
    entityRenderer.getShader().start();
    entityRenderer.getShader().loadClippingPlane(clippingPlane);
    entityRenderer.getShader().loadLights(lights);
    entityRenderer.getShader().loadSky(skyColor);
    entityRenderer.getShader().loadViewMatrix(camera);
    entityRenderer.render(entities);
    entityRenderer.getShader().stop();

    // terrain
    terrainRenderer.getShader().start();
    terrainRenderer.getShader().loadClippingPlane(clippingPlane);
    terrainRenderer.getShader().loadLights(lights);
    terrainRenderer.getShader().loadSky(skyColor);
    terrainRenderer.getShader().loadViewMatrix(camera);
    terrainRenderer.render(terrains);
    terrainRenderer.getShader().stop();

    // skybox
    skyboxRenderer.getShader().start();
    skyboxRenderer.getShader().loadSky(skyColor);
    skyboxRenderer.getShader().loadViewMatrix(camera);
    skyboxRenderer.render(skybox);
    skyboxRenderer.getShader().stop();
  }
}
