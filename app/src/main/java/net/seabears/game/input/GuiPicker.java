package net.seabears.game.input;

import java.util.Map;
import java.util.Optional;

import org.joml.Vector2f;

import net.seabears.game.entities.EntityTexture;
import net.seabears.game.guis.GuiTexture;

public class GuiPicker {
  private final MouseButton mouse;
  private final Map<EntityTexture, GuiTexture> guis;
  private Optional<EntityTexture> previous;
  private Optional<EntityTexture> selected;

  public GuiPicker(MouseButton mouse, MousePicker picker, Map<EntityTexture, GuiTexture> guis) {
    this.mouse = mouse;
    this.guis = guis;
    this.previous = Optional.empty();
    this.selected = Optional.empty();
  }

  public boolean update(int w, int h) {
    if (mouse.isPressed()) {
      final MousePosition mousePos = mouse.getPosition().normalize(w, h);
      final Optional<EntityTexture> selected = guis.entrySet().stream()
          .filter(e -> isMouseInBounds(mousePos, e.getValue().getPosition(), e.getValue().getScale()))
          .findFirst().flatMap(e -> Optional.of(e.getKey()));
      this.previous = this.selected;
      this.selected = selected;
      return selected.isPresent();
    }
    return false;
  }

  public Optional<EntityTexture> getSelection() {
    // if return last selection only when the user has left all the guis
    return selected.isPresent() ? Optional.empty() : previous;
  }

  private static boolean isMouseInBounds(MousePosition mouse, Vector2f pos, Vector2f size) {
    // invert y
    return mouse.getX() >= pos.x && mouse.getX() < pos.x + size.x
        && -mouse.getY() >= pos.y && -mouse.getY() < pos.y + size.y;
  }
}
