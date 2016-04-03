package net.seabears.game.particles;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.shaders.ShaderProgram;

public class ParticleShader extends ShaderProgram {
  private int locationProjectionMatrix;
  private int locationTextureRows;

  public ParticleShader() throws IOException {
    super(SHADER_ROOT + "particles/");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(1, "modelViewMatrix");
    super.bindAttribute(5, "textureOffset");
    super.bindAttribute(6, "textureBlend");
  }

  @Override
  protected void getAllUniformLocations() {
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationTextureRows = super.getUniformLocation("textureRows");
  }

  public void loadProjectionMatrix(Matrix4f matrix) {
    super.loadMatrix(locationProjectionMatrix, matrix);
  }

  public void loadTexture(ParticleTexture texture) {
    super.loadFloat(locationTextureRows, texture.getRows());
  }
}
