package net.seabears.game.shadows;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.shaders.ShaderProgram;

public abstract class ShadowShader extends ShaderProgram {
  private final int shadowMapTextureUnit;
  private int locationPcfCount;
  private int locationShadowMapSize;
  private int locationShadowDistance;
  private int locationTransitionDistance;
  private int locationToShadowMapSpace;
  private int locationShadowMap;

  public ShadowShader(String root, int shadowMapTextureUnit) throws IOException {
    super(root);
    this.shadowMapTextureUnit = shadowMapTextureUnit;
  }

  @Override
  protected void getAllUniformLocations() {
    locationPcfCount = super.getUniformLocation("pcfCount");
    locationShadowMapSize = super.getUniformLocation("shadowMapSize");
    locationToShadowMapSpace = super.getUniformLocation("toShadowMapSpace");
    locationShadowMap = super.getUniformLocation("shadowMap");
    locationShadowDistance = super.getUniformLocation("shadowDistance");
    locationTransitionDistance = super.getUniformLocation("transitionDistance");
  }

  public void loadShadows(ShadowMapRenderer renderer) {
    loadShadows();
    loadPercentageCloserFiltering(renderer.getPcfCount());
    loadShadowBox(renderer.getShadowBox());
    loadShadowMapSize(renderer.getSize());
    loadShadowMapSpaceMatrix(renderer.getToShadowMapSpaceMatrix());
  }

  public void loadPercentageCloserFiltering(int count) {
    super.loadInt(locationPcfCount, count);
  }

  public void loadShadows() {
    super.loadInt(locationShadowMap, shadowMapTextureUnit);
  }

  public void loadShadowBox(ShadowBox box) {
    super.loadFloat(locationShadowDistance, box.getShadowDistance());
    super.loadFloat(locationTransitionDistance, box.getTransitionDistance());
  }

  public void loadShadowMapSize(float size) {
    super.loadFloat(locationShadowMapSize, size);
  }

  public void loadShadowMapSpaceMatrix(Matrix4f matrix) {
    super.loadMatrix(locationToShadowMapSpace, matrix);
  }
}
