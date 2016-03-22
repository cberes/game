package net.seabears.game.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.shaders.StaticShader;

public class MasterRenderer implements AutoCloseable {
  private final StaticShader shader;
  private final Renderer renderer;
  private final Map<TexturedModel, List<Entity>> entities;

  public MasterRenderer(StaticShader shader, Renderer renderer) {
    this.shader = shader;
    this.renderer = renderer;
    this.entities = new HashMap<>();
  }

  public void add(Entity entity) {
    TexturedModel model = entity.getModel();
    if (!entities.containsKey(model)) {
      entities.put(model, new ArrayList<>());
    }
    entities.get(model).add(entity);
  }

  public void render(final Light light, final Camera camera) {
    renderer.prepare();
    shader.start();
    shader.loadLight(light);
    shader.loadViewMatrix(camera);
    renderer.render(entities);
    shader.stop();
    entities.clear();
  }

  @Override
  public void close() {
    shader.close();
  }
}
