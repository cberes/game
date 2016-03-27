package net.seabears.game.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.seabears.game.entities.Entity;
import net.seabears.game.models.TexturedModel;

public class EntitiesByTexture {
  private final Map<TexturedModel, List<Entity>> entities;

  public EntitiesByTexture() {
    this.entities = new HashMap<>();
  }

  public void addAll(Collection<Entity> entities) {
    entities.forEach(this::add);
  }

  public void add(Entity entity) {
    TexturedModel model = entity.getModel();
    if (!entities.containsKey(model)) {
      entities.put(model, new ArrayList<>());
    }
    entities.get(model).add(entity);
  }

  public Map<TexturedModel, List<Entity>> get() {
    return Collections.unmodifiableMap(entities);
  }
}
