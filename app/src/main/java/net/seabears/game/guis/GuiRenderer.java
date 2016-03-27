package net.seabears.game.guis;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.StaticShader;
import net.seabears.game.models.RawModel;
import net.seabears.game.render.Loader;
import net.seabears.game.render.Renderer;
import net.seabears.game.util.TransformationMatrix;

public class GuiRenderer implements Renderer {
  private final RawModel quad;
  private final GuiShader shader;

  public GuiRenderer(Loader loader, GuiShader shader) {
    this.quad = loader.loadToVao(new float[] {-1, 1, -1, -1, 1, 1, 1, -1});
    this.shader = shader;
    this.shader.init();
  }

  public void render(List<GuiTexture> guis) {
    shader.start();
    GL30.glBindVertexArray(quad.getVaoId());
    GL20.glEnableVertexAttribArray(StaticShader.ATTR_POSITION);
    // enable alpha-blending
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    // disable depth-test so that overlapping guis can work
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    for (GuiTexture gui : guis) {
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, gui.getTextureId());
      shader.loadTransformation(new TransformationMatrix(gui.getPosition(), gui.getScale()).toMatrix());
      GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
    }
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glDisable(GL11.GL_BLEND);
    GL20.glDisableVertexAttribArray(StaticShader.ATTR_POSITION);
    GL30.glBindVertexArray(0);
    shader.stop();
  }

  @Override
  public void close() {
    shader.close();
  }
}
