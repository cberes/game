package net.seabears.game.guis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;

import net.seabears.game.entities.EntityTexture;

public class GuiBuilder {
  private final Map<EntityTexture, GuiTexture> guis;
  private final float width;
  private final float height;
  private float lastX = -1.0f;
  private float lastY = -1.0f;

  public GuiBuilder(float width, float height) {
    this.width = width;
    this.height = height;
    this.guis = new HashMap<>();
  }

  public void add(EntityTexture t, int textureId) {
    if (lastY + height * 2 > 1.0f) {
      lastY = -1.0f;
      lastX += width * 2;
    }
    if (lastX + width * 2 > 1.0f) {
      throw new IllegalStateException("too many GUIs");
    }

    final float centerX = lastX + width;
    final float centerY = lastY + height;
    guis.put(t, new GuiTexture(textureId, new Vector2f(centerX, centerY), new Vector2f(width, height)));
    lastY += height * 2;
  }

  public Map<EntityTexture, GuiTexture> getGuis() {
    return Collections.unmodifiableMap(guis);
  }
}
