package net.seabears.game.shadows;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.seabears.game.entities.Entity;
import net.seabears.game.entities.Light;
import net.seabears.game.models.RawModel;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.render.FrameBuffer;
import net.seabears.game.render.MasterRenderer;
import net.seabears.game.render.Renderer;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.util.CameraOrientation;
import net.seabears.game.util.TransformationMatrix;

/**
 * This class is in charge of using all of the classes in the shadows package to carry out the
 * shadow render pass, i.e. rendering the scene to the shadow map texture. This is the only class in
 * the shadows package which needs to be referenced from outside the shadows package.
 * 
 * @author Karl
 *
 */
public class ShadowMapRenderer implements Renderer {
  /**
   * Create the offset for part of the conversion to shadow map space. This conversion is necessary
   * to convert from one coordinate system to the coordinate system that we can use to sample to
   * shadow map.
   * 
   * @return The offset as a matrix (so that it's easy to apply to other matrices).
   */
  private static Matrix4f createOffset() {
    Matrix4f offset = new Matrix4f();
    offset.translate(new Vector3f(0.5f));
    offset.scale(new Vector3f(0.5f));
    return offset;
  }

  private final FrameBuffer shadowFbo;
  private final ShadowMapShader shader;
  private final ShadowBox shadowBox;
  private final int size;
  private final int pcfCount;
  private final Matrix4f projectionMatrix = new Matrix4f();
  private final Matrix4f lightViewMatrix;
  private final Matrix4f projectionViewMatrix = new Matrix4f();
  private final Matrix4f offset = createOffset();

  /**
   * Creates instances of the important objects needed for rendering the scene to the shadow map.
   * This includes the {@link ShadowBox} which calculates the position and size of the "view cuboid"
   * , the simple renderer and shader program that are used to render objects to the shadow map, and
   * the {@link ShadowFrameBuffer} to which the scene is rendered. The size of the shadow map is
   * determined here.
   * 
   * @param camera - the camera being used in the scene.
   */
  public ShadowMapRenderer(ShadowMapShader shader, ShadowBox box, FrameBuffer shadowFbo, int pcfCount) {
    assert shadowFbo.getWidth() == shadowFbo.getHeight();
    this.shadowBox = box;
    this.shadowFbo = shadowFbo;
    this.pcfCount = pcfCount;
    this.size = shadowFbo.getWidth();
    this.lightViewMatrix = box.getLightViewMatrix();
    this.shader = shader;
    this.shader.init();
  }

  /**
   * Carries out the shadow render pass. This renders the entities to the shadow map. First the
   * shadow box is updated to calculate the size and position of the "view cuboid". The light
   * direction is assumed to be "-lightPosition" which will be fairly accurate assuming that the
   * light is very far from the scene. It then prepares to render, renders the entities to the
   * shadow map, and finishes rendering.
   * 
   * @param entities - the lists of entities to be rendered. Each list is associated with the
   *        {@link TexturedModel} that all of the entities in that list use.
   * @param sun - the light acting as the sun in the scene.
   */
  public void render(Map<TexturedModel, List<Entity>> entities, CameraOrientation c, Light sun, int displayWidth, int displayHeight) {
    shadowBox.update(c);
    Vector3f sunPosition = sun.getPosition();
    Vector3f lightDirection = new Vector3f(-sunPosition.x, -sunPosition.y, -sunPosition.z);
    prepare(lightDirection, shadowBox);
    render(entities);
    finish(displayWidth, displayHeight);
  }

  /**
   * Renders entieis to the shadow map. Each model is first bound and then all of the entities using
   * that model are rendered to the shadow map.
   * 
   * @param entities - the entities to be rendered to the shadow map.
   */
  protected void render(Map<TexturedModel, List<Entity>> entities) {
    for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
      final TexturedModel model = entry.getKey();
      final RawModel rawModel = model.getRawModel();
      bindModel(rawModel);
      GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getTextureId());
      if (model.getTexture().isTransparent()) {
        MasterRenderer.disableCulling();
      }
      for (Entity entity : entry.getValue()) {
        prepareInstance(entity);
        GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
      }
      if (model.getTexture().isTransparent()) {
        MasterRenderer.enableCulling();
      }
    }
    GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_POSITION);
    GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
    GL30.glBindVertexArray(0);
  }

  /**
   * Binds a raw model before rendering. Only the attribute 0 is enabled here because that is where
   * the positions are stored in the VAO, and only the positions are required in the vertex shader.
   * 
   * @param rawModel - the model to be bound.
   */
  private void bindModel(RawModel rawModel) {
    GL30.glBindVertexArray(rawModel.getVaoId());
    GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_POSITION);
    GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_TEXTURE);
  }

  /**
   * Prepares an entity to be rendered. The model matrix is created in the usual way and then
   * multiplied with the projection and view matrix (often in the past we've done this in the vertex
   * shader) to create the mvp-matrix. This is then loaded to the vertex shader as a uniform.
   * 
   * @param entity - the entity to be prepared for rendering.
   */
  private void prepareInstance(Entity entity) {
    Matrix4f modelMatrix = new TransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale()).toMatrix();
    shader.loadMvpMatrix(projectionViewMatrix.mul(modelMatrix, new Matrix4f()));
  }

  /**
   * Prepare for the shadow render pass. This first updates the dimensions of the orthographic
   * "view cuboid" based on the information that was calculated in the {@link SHadowBox} class. The
   * light's "view" matrix is also calculated based on the light's direction and the center position
   * of the "view cuboid" which was also calculated in the {@link ShadowBox} class. These two
   * matrices are multiplied together to create the projection-view matrix. This matrix determines
   * the size, position, and orientation of the "view cuboid" in the world. This method also binds
   * the shadows FBO so that everything rendered after this gets rendered to the FBO. It also
   * enables depth testing, and clears any data that is in the FBOs depth attachment from last
   * frame. The simple shader program is also started.
   * 
   * @param lightDirection - the direction of the light rays coming from the sun.
   * @param box - the shadow box, which contains all the info about the "view cuboid".
   */
  private void prepare(Vector3f lightDirection, ShadowBox box) {
    updateOrthoProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength());
    updateLightViewMatrix(lightDirection, box.getCenter());
    projectionMatrix.mul(lightViewMatrix, projectionViewMatrix);
    shadowFbo.bind();
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    shader.start();
  }

  /**
   * Finish the shadow render pass. Stops the shader and unbinds the shadow FBO, so everything
   * rendered after this point is rendered to the screen, rather than to the shadow FBO.
   */
  private void finish(int displayWidth, int displayHeight) {
    shader.stop();
    shadowFbo.unbind(displayWidth, displayHeight);
  }

  /**
   * Updates the "view" matrix of the light. This creates a view matrix which will line up the
   * direction of the "view cuboid" with the direction of the light. The light itself has no
   * position, so the "view" matrix is centered at the center of the "view cuboid". The created view
   * matrix determines where and how the "view cuboid" is positioned in the world. The size of the
   * view cuboid, however, is determined by the projection matrix.
   * 
   * @param direction - the light direction, and therefore the direction that the "view cuboid"
   *        should be pointing.
   * @param center - the center of the "view cuboid" in world space.
   */
  private void updateLightViewMatrix(Vector3f direction, Vector3f center) {
    direction.normalize();
    center.negate();
    float pitch = (float) Math.acos(new Vector2f(direction.x, direction.z).length());
    lightViewMatrix.rotation(pitch, new Vector3f(1, 0, 0));
    float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
    yaw = direction.z > 0 ? yaw - 180 : yaw;
    lightViewMatrix.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0));
    lightViewMatrix.translate(center);
  }

  /**
   * Creates the orthographic projection matrix. This projection matrix basically sets the width,
   * length and height of the "view cuboid", based on the values that were calculated in the
   * {@link ShadowBox} class.
   * 
   * @param width - shadow box width.
   * @param height - shadow box height.
   * @param length - shadow box length.
   */
  private void updateOrthoProjectionMatrix(float width, float height, float length) {
    projectionMatrix.identity();
    projectionMatrix.m00 = 2f / width;
    projectionMatrix.m11 = 2f / height;
    projectionMatrix.m22 = -2f / length;
    projectionMatrix.m33 = 1;
  }

  /**
   * This biased projection-view matrix is used to convert fragments into "shadow map space" when
   * rendering the main render pass. It converts a world space position into a 2D coordinate on the
   * shadow map. This is needed for the second part of shadow mapping.
   * 
   * @return The to-shadow-map-space matrix.
   */
  public Matrix4f getToShadowMapSpaceMatrix() {
    return offset.mul(projectionViewMatrix, new Matrix4f());
  }

  /**
   * @return The ID of the shadow map texture. The ID will always stay the same, even when the
   *         contents of the shadow map texture change each frame.
   */
  public int getShadowMap() {
    return shadowFbo.getTexture();
  }

  /**
   * @return The light's "view" matrix.
   */
  protected Matrix4f getLightSpaceTransform() {
    return lightViewMatrix;
  }

  public ShadowBox getShadowBox() {
    return shadowBox;
  }

  public int getSize() {
    return size;
  }

  public int getPcfCount() {
    return pcfCount;
  }

  /**
   * Clean up the shader and FBO on closing.
   */
  @Override
  public void close() {
    shader.close();
    shadowFbo.close();
  }
}
