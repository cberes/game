package net.seabears.game.particles;

import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Light;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.util.ViewMatrix;

public class ParticleShader extends ShaderProgram {
  private final int lights;
  private int locationClippingPlane;
  private int locationFakeLighting;
  private int[] locationLightAttenuation;
  private int[] locationLightColor;
  private int[] locationLightPosition;
  private int locationProjectionMatrix;
  private int locationReflectivity;
  private int locationShineDamper;
  private int locationSkyColor;
  private int locationTextureBlend;
  private int locationTextureRows;
  private int locationTextureOffset;
  private int locationTextureOffsetNext;
  private int locationTransformationMatrix;
  private int locationModelViewMatrix;
  private int locationViewMatrix;

  public ParticleShader(int lights) throws IOException {
    super(SHADER_ROOT + "particles/");
    this.lights = lights;
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(ATTR_TEXTURE, "textureCoords");
    super.bindAttribute(ATTR_NORMAL, "normal");
  }

  @Override
  protected void getAllUniformLocations() {
    locationClippingPlane = super.getUniformLocation("clippingPlane");
    locationFakeLighting = super.getUniformLocation("fakeLighting");
    locationLightAttenuation = super.getUniformLocations("attenuation", lights);
    locationLightColor = super.getUniformLocations("lightColor", lights);
    locationLightPosition = super.getUniformLocations("lightPosition", lights);
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationReflectivity = super.getUniformLocation("reflectivity");
    locationShineDamper = super.getUniformLocation("shineDamper");
    locationSkyColor = super.getUniformLocation("skyColor");
    locationTextureBlend = super.getUniformLocation("textureBlend");
    locationTextureRows = super.getUniformLocation("textureRows");
    locationTextureOffset = super.getUniformLocation("textureOffset");
    locationTextureOffsetNext = super.getUniformLocation("textureOffsetNext");
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
    locationModelViewMatrix = super.getUniformLocation("modelViewMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
  }

  public void loadClippingPlane(Vector4f plane) {
    this.loadFloat(locationClippingPlane, plane);
  }

  public void loadLights(final List<Light> lights) {
    range(0, this.lights).forEach(i -> loadLight(i < lights.size() ? lights.get(i) : OFF_LIGHT, i));
  }

  private void loadLight(Light light, int index) {
    super.loadFloat(locationLightAttenuation[index], light.getAttenuation());
    super.loadFloat(locationLightColor[index], light.getColor());
    super.loadFloat(locationLightPosition[index], light.getPosition());
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

  public void loadParticle(Particle particle, Matrix4f viewMatrix) {
    // get model-view matrix, but transpose some elements so there is no rotation
    // this keeps particles always visible to the camera
    Matrix4f modelMatrix = new Matrix4f();
    modelMatrix.translate(particle.getPosition());
    modelMatrix.m00 = viewMatrix.m00;
    modelMatrix.m01 = viewMatrix.m10;
    modelMatrix.m02 = viewMatrix.m20;
    modelMatrix.m10 = viewMatrix.m01;
    modelMatrix.m11 = viewMatrix.m11;
    modelMatrix.m12 = viewMatrix.m21;
    modelMatrix.m20 = viewMatrix.m02;
    modelMatrix.m21 = viewMatrix.m12;
    modelMatrix.m22 = viewMatrix.m22;
    modelMatrix.rotate((float) Math.toRadians(particle.getRotation()), new Vector3f(0.0f, 0.0f, 1.0f));
    modelMatrix.scale(particle.getScale());
    super.loadMatrix(locationModelViewMatrix, viewMatrix.mul(modelMatrix, new Matrix4f()));
    super.loadFloat(locationTextureBlend, particle.getBlend());
    super.loadFloat(locationTextureRows, particle.getTexture().getRows());
    super.loadFloat(locationTextureOffset, particle.getTextureOffset());
    super.loadFloat(locationTextureOffsetNext, particle.getTextureOffsetNext());
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
