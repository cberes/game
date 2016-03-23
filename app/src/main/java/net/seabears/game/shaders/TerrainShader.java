package net.seabears.game.shaders;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Light;
import net.seabears.game.terrains.Terrain;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.util.TransformationMatrix;
import net.seabears.game.util.ViewMatrix;

public class TerrainShader extends ShaderProgram {
  private static final String SHADER_ROOT = "src/main/shaders/";

  private int locationFakeLighting;
  private int locationLightColor;
  private int locationLightPosition;
  private int locationProjectionMatrix;
  private int locationReflectivity;
  private int locationShineDamper;
  private int locationSkyColor;
  private int locationTransformationMatrix;
  private int locationViewMatrix;

  public TerrainShader() {
    super(SHADER_ROOT + "terrainVertexShader.txt", SHADER_ROOT + "terrainFragmentShader.txt");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(StaticShader.ATTR_POSITION, "position");
    super.bindAttribute(StaticShader.ATTR_TEXTURE, "textureCoords");
    super.bindAttribute(StaticShader.ATTR_NORMAL, "normal");
  }

  @Override
  protected void getAllUniformLocations() {
    locationFakeLighting = super.getUniformLocation("fakeLighting");
    locationLightColor = super.getUniformLocation("lightColor");
    locationLightPosition = super.getUniformLocation("lightPosition");
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationReflectivity = super.getUniformLocation("reflectivity");
    locationShineDamper = super.getUniformLocation("shineDamper");
    locationSkyColor = super.getUniformLocation("skyColor");
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
  }

  public void loadLight(Light light) {
    super.loadFloat(locationLightColor, light.getColor());
    super.loadFloat(locationLightPosition, light.getPosition());
  }

  public void loadProjectionMatrix(Matrix4f matrix) {
    super.loadMatrix(locationProjectionMatrix, matrix);
  }

  public void loadSky(Vector3f color) {
    super.loadFloat(locationSkyColor, color);
  }

  public void loadTexture(ModelTexture texture) {
    super.loadFloat(locationFakeLighting, texture.isFakeLighting());
    super.loadFloat(locationReflectivity, texture.getReflectivity());
    super.loadFloat(locationShineDamper, texture.getShineDamper());
  }

  public void loadTransformationMatrix(Terrain terrain) {
    loadTransformationMatrix(new TransformationMatrix(new Vector3f(terrain.getX(), 0.0f, terrain.getZ()), new Vector3f().zero(), 1.0f).toMatrix());
  }

  public void loadTransformationMatrix(Matrix4f matrix) {
    super.loadMatrix(locationTransformationMatrix, matrix);
  }

  public void loadViewMatrix(Camera camera) {
    loadViewMatrix(new ViewMatrix(camera).toMatrix());
  }

  public void loadViewMatrix(Matrix4f matrix) {
    super.loadMatrix(locationViewMatrix, matrix);
  }
}
