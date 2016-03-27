package net.seabears.game.entities;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.textures.ModelTexture;
import net.seabears.game.util.TransformationMatrix;

public class SimpleShader extends ShaderProgram {;
  private int locationProjectionMatrix;
  private int locationTextureRows;
  private int locationTextureOffset;
  private int locationTransformationMatrix;
  private int locationViewMatrix;

  public SimpleShader() throws IOException {
    super(SHADER_ROOT + "simple/");
  }

  @Override
  protected void bindAttributes() {
    super.bindAttribute(ATTR_POSITION, "position");
    super.bindAttribute(ATTR_TEXTURE, "textureCoords");
    super.bindAttribute(ATTR_NORMAL, "normal");
  }

  @Override
  protected void getAllUniformLocations() {
    locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
    locationTextureRows = super.getUniformLocation("textureRows");
    locationTextureOffset = super.getUniformLocation("textureOffset");
    locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
    locationViewMatrix = super.getUniformLocation("viewMatrix");
  }

  public void loadProjectionMatrix(Matrix4f matrix) {
    super.loadMatrix(locationProjectionMatrix, matrix);
  }

  public void loadTexture(ModelTexture texture) {
    super.loadFloat(locationTextureRows, texture.getRows());
  }

  public void loadEntity(Entity entity) {
    loadTransformationMatrix(new TransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale()).toMatrix());
    super.loadFloat(locationTextureOffset, entity.getTextureOffset());
  }

  public void loadTransformationMatrix(Matrix4f matrix) {
    super.loadMatrix(locationTransformationMatrix, matrix);
  }

  public void loadViewMatrix(Matrix4f matrix) {
    super.loadMatrix(locationViewMatrix, matrix);
  }
}
