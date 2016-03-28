package net.seabears.game.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EntityClassifier<T, E> {
  private final Map<T, List<E>> entities;

  public EntityClassifier() {
    this.entities = new HashMap<>();
  }

  public void addAll(Collection<E> entities) {
    entities.forEach(this::add);
  }

  public void add(E entity) {
    final T model = getTexture(entity);
    if (!entities.containsKey(model)) {
      entities.put(model, new ArrayList<>());
    }
    entities.get(model).add(entity);
  }

  public Map<T, List<E>> get() {
    return Collections.unmodifiableMap(entities);
  }

  protected abstract T getTexture(E entity);
}
