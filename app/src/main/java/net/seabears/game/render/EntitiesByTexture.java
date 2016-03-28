package net.seabears.game.render;

import net.seabears.game.entities.Entity;
import net.seabears.game.models.TexturedModel;

public class EntitiesByTexture extends EntityClassifier<TexturedModel, Entity> {
  @Override
  protected TexturedModel getTexture(Entity entity) {
    return entity.getModel();
  }
}
