package net.seabears.game.guis.fonts.creator;

import net.seabears.game.guis.fonts.GuiText;

/**
 * Represents a font. It holds the font's texture atlas as well as having the ability to create the
 * quad vertices for any text using this font.
 * 
 * @author Karl
 *
 */
public class FontType {
  private final int textureAtlas;
  private final TextMeshCreator loader;

  /**
   * Creates a new font and loads up the data about each character from the font file.
   * 
   * @param textureAtlas - the ID of the font atlas texture.
   * @param fontFile - the font file containing information about each character in the texture
   *        atlas.
   */
  public FontType(int textureAtlas, TextMeshCreator loader) {
    this.textureAtlas = textureAtlas;
    this.loader = loader;
  }

  /**
   * @return The font texture atlas.
   */
  public int getTextureAtlas() {
    return textureAtlas;
  }

  /**
   * Takes in an unloaded text and calculate all of the vertices for the quads on which this text
   * will be rendered. The vertex positions and texture coords and calculated based on the
   * information from the font file.
   * 
   * @param text - the unloaded text.
   * @return Information about the vertices of all the quads.
   */
  public TextMeshData loadText(GuiText text) {
    return loader.createTextMesh(text);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + textureAtlas;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    final FontType other = (FontType) obj;
    return textureAtlas == other.textureAtlas;
  }
}
