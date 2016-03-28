package net.seabears.game.guis.fonts;

import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.guis.fonts.creator.FontType;

/**
 * Represents a piece of text in the game.
 * 
 * @author Karl
 *
 */
public class GuiText {
  private final String textString;
  private final Vector2f position;
  private final float lineMaxSize;
  private final FontType font;
  private final float fontSize;
  private final boolean centerText;
  private final TextAttr attr;
  private final TextAttr borderAttr;
  private int textMeshVao;
  private int vertexCount;
  private int numberOfLines;

  public GuiText(String textString, Vector2f position, float lineMaxSize, boolean centerText,
      FontType font, float fontSize) {
    this(textString, position, lineMaxSize, centerText, font, fontSize, new TextAttr(new Vector3f(), 0.5f, 0.1f));
  }

  public GuiText(String textString, Vector2f position, float lineMaxSize, boolean centerText,
      FontType font, float fontSize, TextAttr attr) {
    this(textString, position, lineMaxSize, centerText, font, fontSize, attr, new TextAttr(new Vector3f(), 0.0f, 0.0f));
  }

  public GuiText(String textString, Vector2f position, float lineMaxSize, boolean centerText,
      FontType font, float fontSize, TextAttr attr, TextAttr borderAttr) {
    this.textString = textString;
    this.position = position;
    this.lineMaxSize = lineMaxSize;
    this.font = font;
    this.fontSize = fontSize;
    this.centerText = centerText;
    this.attr = attr;
    this.borderAttr = borderAttr;
  }

  /**
   * Set the VAO and vertex count for this text.
   * 
   * @param vao
   *            - the VAO containing all the vertex data for the quads on
   *            which the text will be rendered.
   * @param verticesCount
   *            - the total number of vertices in all of the quads.
   */
  public void setMeshInfo(int vao, int verticesCount) {
      this.textMeshVao = vao;
      this.vertexCount = verticesCount;
  }

  /**
   * Sets the number of lines that this text covers (method used only in loading).
   * 
   * @param number
   */
  public void setNumberOfLines(int number) {
    this.numberOfLines = number;
  }

  /**
   * @return The font used by this text.
   */
  public FontType getFont() {
    return font;
  }

  /**
   * @return The number of lines of text. This is determined when the text is loaded, based on the
   *         length of the text and the max line length that is set.
   */
  public int getNumberOfLines() {
    return numberOfLines;
  }

  /**
   * @return The position of the top-left corner of the text in screen-space. (0, 0) is the top left
   *         corner of the screen, (1, 1) is the bottom right.
   */
  public Vector2f getPosition() {
    return position;
  }

  /**
   * @return the ID of the text's VAO, which contains all the vertex data for the quads on which the
   *         text will be rendered.
   */
  public int getMesh() {
    return textMeshVao;
  }

  /**
   * @return The total number of vertices of all the text's quads.
   */
  public int getVertexCount() {
    return this.vertexCount;
  }

  /**
   * @return the font size of the text (a font size of 1 is normal).
   */
  public float getFontSize() {
    return fontSize;
  }

  /**
   * @return {@code true} if the text should be centered.
   */
  public boolean isCentered() {
    return centerText;
  }

  /**
   * @return The maximum length of a line of this text.
   */
  public float getMaxLineSize() {
    return lineMaxSize;
  }

  /**
   * @return The string of text.
   */
  public String getTextString() {
    return textString;
  }

  public TextAttr getAttr() {
    return attr;
  }

  public TextAttr getBorderAttr() {
    return borderAttr;
  }
}
