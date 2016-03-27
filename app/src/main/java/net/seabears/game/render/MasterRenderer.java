package net.seabears.game.render;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import net.seabears.game.entities.Entity;
import net.seabears.game.entities.EntityRenderer;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.SimpleRenderer;
import net.seabears.game.entities.normalmap.NormalMappingRenderer;
import net.seabears.game.shadows.ShadowMapRenderer;
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
  private final NormalMappingRenderer nmRenderer;
  private final TerrainRenderer terrainRenderer;
  private final SkyboxRenderer skyboxRenderer;
  private final ShadowMapRenderer shadowRenderer;
  private final SimpleRenderer simpleRenderer;

  public MasterRenderer(Vector3f skyColor, EntityRenderer entityRenderer, NormalMappingRenderer nmRenderer, TerrainRenderer terrainRenderer, SkyboxRenderer skyboxRenderer, ShadowMapRenderer shadowRenderer, SimpleRenderer simpleRenderer) {
    this.skyColor = skyColor;
    this.entityRenderer = entityRenderer;
    this.nmRenderer = nmRenderer;
    this.terrainRenderer = terrainRenderer;
    this.skyboxRenderer = skyboxRenderer;
    this.shadowRenderer = shadowRenderer;
    this.simpleRenderer = simpleRenderer;
    enableCulling();
  }

  public void renderShadowMap(List<Entity> entities, List<Entity> nmEntities, List<Light> lights, int displayWidth, int displayHeight) {
      final EntitiesByTexture e = new EntitiesByTexture();
      e.addAll(entities);
      e.addAll(nmEntities);
      shadowRenderer.render(e.get(), lights.get(0), displayWidth, displayHeight);
  }

  public void renderSimple(List<Entity> entities, Matrix4f viewMatrix) {
    // simple-rendered entities
    if (!entities.isEmpty()) {
      final EntitiesByTexture e = new EntitiesByTexture();
      e.addAll(entities);
      simpleRenderer.getShader().start();
      simpleRenderer.getShader().loadViewMatrix(viewMatrix);
      simpleRenderer.render(e.get());
      simpleRenderer.getShader().stop();
    }
  }

  public void render(List<Entity> entities, List<Entity> nmEntities, List<Terrain> terrains, List<Light> lights, Skybox skybox, Matrix4f viewMatrix, Vector4f clippingPlane) {
    // prepare
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glClearColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);

    // entities
    if (!entities.isEmpty()) {
      final EntitiesByTexture e = new EntitiesByTexture();
      e.addAll(entities);
      entityRenderer.getShader().start();
      entityRenderer.getShader().loadClippingPlane(clippingPlane);
      entityRenderer.getShader().loadLights(lights);
      entityRenderer.getShader().loadSky(skyColor);
      entityRenderer.getShader().loadViewMatrix(viewMatrix);
      entityRenderer.getShader().loadShadows(shadowRenderer);
      entityRenderer.render(e.get(), shadowRenderer.getShadowMap());
      entityRenderer.getShader().stop();
    }

    // normal-mapped entities
    if (!nmEntities.isEmpty()) {
      final EntitiesByTexture e = new EntitiesByTexture();
      e.addAll(nmEntities);
      nmRenderer.getShader().start();
      nmRenderer.getShader().loadClippingPlane(clippingPlane);
      nmRenderer.getShader().loadLights(lights, viewMatrix);
      nmRenderer.getShader().loadSky(skyColor);
      nmRenderer.getShader().loadViewMatrix(viewMatrix);
      nmRenderer.getShader().loadShadows(shadowRenderer);
      nmRenderer.render(e.get(), shadowRenderer.getShadowMap());
      nmRenderer.getShader().stop();
    }

    // terrain
    if (!terrains.isEmpty()) {
      terrainRenderer.getShader().start();
      terrainRenderer.getShader().loadClippingPlane(clippingPlane);
      terrainRenderer.getShader().loadLights(lights);
      terrainRenderer.getShader().loadSky(skyColor);
      terrainRenderer.getShader().loadViewMatrix(viewMatrix);
      terrainRenderer.getShader().loadShadows(shadowRenderer);
      terrainRenderer.render(terrains, shadowRenderer.getShadowMap());
      terrainRenderer.getShader().stop();
    }

    // skybox
    if (skybox != null) {
      skyboxRenderer.getShader().start();
      skyboxRenderer.getShader().loadSky(skyColor);
      skyboxRenderer.getShader().loadViewMatrix(viewMatrix);
      skyboxRenderer.render(skybox);
      skyboxRenderer.getShader().stop();
    }
  }
}
