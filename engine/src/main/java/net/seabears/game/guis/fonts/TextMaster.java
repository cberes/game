package net.seabears.game.guis.fonts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.seabears.game.guis.fonts.creator.FontType;
import net.seabears.game.guis.fonts.creator.TextMeshData;
import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.render.Renderer;

public class TextMaster implements Renderer {
  private final Loader loader;
  private final Map<FontType, List<GuiText>> text;
  private final FontRenderer renderer;

  public TextMaster(Loader loader, FontRenderer renderer) {
    this.loader = loader;
    this.renderer = renderer;
    this.text = new HashMap<>();
  }

  public void load(GuiText text) {
    final FontType font = text.getFont();
    final TextMeshData data = font.loadText(text);
    final RawModel model = loader.loadToVao(data);
    text.setMeshInfo(model.getVaoId(), data.getVertexCount());
    if (!this.text.containsKey(font)) {
      this.text.put(font, new ArrayList<>());
    }
    this.text.get(font).add(text);
  }

  public void remove(GuiText text) {
    if (this.text.containsKey(text.getFont())) {
      // TODO delete VAOs and VBOs
      this.text.get(text.getFont()).remove(text);
      if (this.text.get(text.getFont()).isEmpty()) {
        this.text.remove(text.getFont());
      }
    }
  }

  public void render() {
    renderer.render(text);
  }

  @Override
  public void close() {
    renderer.close();
  }
}
