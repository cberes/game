package net.seabears.game.guis.fonts.creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides functionality for getting the values from a font file.
 * 
 * @author Karl
 *
 */
public class MetaFile {
  private static final int PAD_TOP = 0;
  private static final int PAD_LEFT = 1;
  private static final int PAD_BOTTOM = 2;
  private static final int PAD_RIGHT = 3;

  private final double aspectRatio;
  private final int desiredPadding;
  private final Map<Integer, Character> metaData = new HashMap<Integer, Character>();
  private final Map<String, String> values = new HashMap<String, String>();
  private double verticalPerPixelSize;
  private double horizontalPerPixelSize;
  private double spaceWidth;
  private int[] padding;
  private int paddingWidth;
  private int paddingHeight;

  /**
   * Opens a font file in preparation for reading.
   * 
   * @param file - the font file.
   * @throws IOException if I/O error occurs
   */
  public MetaFile(File file, int width, int height, int desiredPadding) throws IOException {
    this.aspectRatio = width / (double) height;
    this.desiredPadding = desiredPadding;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      loadPaddingData(reader);
      loadLineSizes(reader);
      loadCharacterData(reader, getValueOfVariable("scaleW"));
    }
  }

  protected double getSpaceWidth() {
    return spaceWidth;
  }

  protected Character getCharacter(int ascii) {
    return metaData.get(ascii);
  }

  /**
   * Read in the next line and store the variable values.
   * 
   * @return {@code true} if the end of the file hasn't been reached.
   * @throws IOException if I/O error occurs
   */
  private boolean processNextLine(BufferedReader reader) throws IOException {
    values.clear();
    String line = reader.readLine();
    if (line == null || line.startsWith("kerning")) {
      return false;
    }
    for (String part : line.split(" ")) {
      String[] valuePairs = part.split("=");
      if (valuePairs.length == 2) {
        values.put(valuePairs[0], valuePairs[1]);
      }
    }
    return true;
  }

  /**
   * Gets the {@code int} value of the variable with a certain name on the current line.
   * 
   * @param variable - the name of the variable.
   * @return The value of the variable.
   */
  private int getValueOfVariable(String variable) {
    final String value = values.get(variable);
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Failed to parse \"" + variable + "\" from: " + value, e);
    }
  }

  /**
   * Gets the array of ints associated with a variable on the current line.
   * 
   * @param variable - the name of the variable.
   * @return The int array of values associated with the variable.
   */
  private int[] getValuesOfVariable(String variable) {
    String[] numbers = values.get(variable).split(",");
    int[] actualValues = new int[numbers.length];
    for (int i = 0; i < actualValues.length; i++) {
      actualValues[i] = Integer.parseInt(numbers[i]);
    }
    return actualValues;
  }

  /**
   * Loads the data about how much padding is used around each character in the texture atlas.
   * @throws IOException if I/O error occurs
   */
  private void loadPaddingData(BufferedReader reader) throws IOException {
    processNextLine(reader);
    this.padding = getValuesOfVariable("padding");
    this.paddingWidth = padding[PAD_LEFT] + padding[PAD_RIGHT];
    this.paddingHeight = padding[PAD_TOP] + padding[PAD_BOTTOM];
  }

  /**
   * Loads information about the line height for this font in pixels, and uses this as a way to find
   * the conversion rate between pixels in the texture atlas and screen-space.
   * @throws IOException if I/O error occurs
   */
  private void loadLineSizes(BufferedReader reader) throws IOException {
    processNextLine(reader);
    int lineHeightPixels = getValueOfVariable("lineHeight") - paddingHeight;
    verticalPerPixelSize = TextMeshCreator.LINE_HEIGHT / (double) lineHeightPixels;
    horizontalPerPixelSize = verticalPerPixelSize / aspectRatio;
  }

  /**
   * Loads in data about each character and stores the data in the {@link Character} class.
   * 
   * @param imageWidth - the width of the texture atlas in pixels.
   * @throws IOException if I/O error occurs
   */
  private void loadCharacterData(BufferedReader reader, int imageWidth) throws IOException {
    processNextLine(reader);
    processNextLine(reader);
    while (processNextLine(reader)) {
      Character c = loadCharacter(imageWidth);
      if (c != null) {
        metaData.put(c.getId(), c);
      }
    }
  }

  /**
   * Loads all the data about one character in the texture atlas and converts it all from 'pixels'
   * to 'screen-space' before storing. The effects of padding are also removed from the data.
   * 
   * @param imageSize - the size of the texture atlas in pixels.
   * @return The data about the character.
   */
  private Character loadCharacter(int imageSize) {
    int id = getValueOfVariable("id");
    if (id == TextMeshCreator.SPACE_ASCII) {
      this.spaceWidth = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize;
      return null;
    }
    double xTex = ((double) getValueOfVariable("x") + (padding[PAD_LEFT] - desiredPadding)) / imageSize;
    double yTex = ((double) getValueOfVariable("y") + (padding[PAD_TOP] - desiredPadding)) / imageSize;
    int width = getValueOfVariable("width") - (paddingWidth - (2 * desiredPadding));
    int height = getValueOfVariable("height") - ((paddingHeight) - (2 * desiredPadding));
    double quadWidth = width * horizontalPerPixelSize;
    double quadHeight = height * verticalPerPixelSize;
    double xTexSize = (double) width / imageSize;
    double yTexSize = (double) height / imageSize;
    double xOff = (getValueOfVariable("xoffset") + padding[PAD_LEFT] - desiredPadding) * horizontalPerPixelSize;
    double yOff = (getValueOfVariable("yoffset") + (padding[PAD_TOP] - desiredPadding)) * verticalPerPixelSize;
    double xAdvance = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize;
    return new Character(id, xTex, yTex, xTexSize, yTexSize, xOff, yOff, quadWidth, quadHeight, xAdvance);
  }
}
