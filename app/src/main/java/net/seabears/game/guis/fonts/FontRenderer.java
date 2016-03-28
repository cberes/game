package net.seabears.game.guis.fonts;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.guis.fonts.creator.FontType;
import net.seabears.game.guis.fonts.creator.GuiText;
import net.seabears.game.render.Renderer;
import net.seabears.game.shaders.ShaderProgram;

public class FontRenderer implements Renderer {
  private final FontShader shader;

  public FontRenderer(FontShader shader) {
    this.shader = shader;
    this.shader.init();
  }

  public void render(Map<FontType, List<GuiText>> text) {
    prepare();
    for (Map.Entry<FontType, List<GuiText>> entry : text.entrySet()) {
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, entry.getKey().getTextureAtlas());
      entry.getValue().forEach(this::renderText);
    }
    endRendering();
  }

  private void prepare() {
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    shader.start();
  }

  private void renderText(GuiText text) {
    GL30.glBindVertexArray(text.getMesh());
    GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_POSITION);
    GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
    shader.loadText(text);
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());
    GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_POSITION);
    GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
    GL30.glBindVertexArray(0);
  }

  private void endRendering() {
    shader.stop();
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glEnable(GL11.GL_DEPTH_TEST);
  }

  @Override
  public void close() {
    shader.close();
  }

}
