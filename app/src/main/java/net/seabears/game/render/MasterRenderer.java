package net.seabears.game.render;

import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.EntityRenderer;
import net.seabears.game.entities.Light;
import net.seabears.game.entities.normalmap.NormalMappingRenderer;
import net.seabears.game.shadows.ShadowMapMasterRenderer;
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
  private final ShadowMapMasterRenderer shadowRenderer;

  public MasterRenderer(Vector3f skyColor, EntityRenderer entityRenderer, NormalMappingRenderer nmRenderer, TerrainRenderer terrainRenderer, SkyboxRenderer skyboxRenderer, ShadowMapMasterRenderer shadowRenderer) {
    this.skyColor = skyColor;
    this.entityRenderer = entityRenderer;
    this.nmRenderer = nmRenderer;
    this.terrainRenderer = terrainRenderer;
    this.skyboxRenderer = skyboxRenderer;
    this.shadowRenderer = shadowRenderer;
    enableCulling();
  }

  public void renderShadowMap(List<Entity> entities, List<Entity> nmEntities, List<Light> lights, int displayWidth, int displayHeight) {
      final EntitiesByTexture e = new EntitiesByTexture();
      e.addAll(entities);
      e.addAll(nmEntities);
      shadowRenderer.render(e.get(), lights.get(0), displayWidth, displayHeight);
  }

  public void render(List<Entity> entities, List<Entity> nmEntities, List<Terrain> terrains, List<Light> lights, Skybox skybox, Camera camera, Vector4f clippingPlane) {
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
      entityRenderer.getShader().loadViewMatrix(camera);
      entityRenderer.render(e.get());
      entityRenderer.getShader().stop();
    }

    // normal-mapped entities
    if (!nmEntities.isEmpty()) {
      final EntitiesByTexture e = new EntitiesByTexture();
      e.addAll(nmEntities);
      nmRenderer.getShader().start();
      nmRenderer.getShader().loadClippingPlane(clippingPlane);
      nmRenderer.getShader().loadLights(lights, camera);
      nmRenderer.getShader().loadSky(skyColor);
      nmRenderer.getShader().loadViewMatrix(camera);
      nmRenderer.render(e.get());
      nmRenderer.getShader().stop();
    }

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
