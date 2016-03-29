package net.seabears.game.shadows;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.seabears.game.entities.Entity;
import net.seabears.game.models.RawModel;
import net.seabears.game.models.TexturedModel;
import net.seabears.game.shaders.ShaderProgram;
import net.seabears.game.util.TransformationMatrix;

public class ShadowMapEntityRenderer {

    private Matrix4f projectionViewMatrix;
    private ShadowShader shader;

    /**
     * @param shader - the simple shader program being used for the shadow render pass.
     * @param projectionViewMatrix - the orthographic projection matrix multiplied by the light's
     *        "view" matrix.
     */
    protected ShadowMapEntityRenderer(ShadowShader shader, Matrix4f projectionViewMatrix) {
        this.shader = shader;
        this.projectionViewMatrix = projectionViewMatrix;
    }

    /**
     * Renders entieis to the shadow map. Each model is first bound and then all of the entities
     * using that model are rendered to the shadow map.
     * 
     * @param entities - the entities to be rendered to the shadow map.
     */
    protected void render(Map<TexturedModel, List<Entity>> entities) {
        for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
            RawModel rawModel = entry.getKey().getRawModel();
            bindModel(rawModel);
            for (Entity entity : entry.getValue()) {
                prepareInstance(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
            }
        }
        GL20.glDisableVertexAttribArray(ShaderProgram.ATTR_POSITION);
        GL30.glBindVertexArray(0);
    }

    /**
     * Binds a raw model before rendering. Only the attribute 0 is enabled here because that is
     * where the positions are stored in the VAO, and only the positions are required in the vertex
     * shader.
     * 
     * @param rawModel - the model to be bound.
     */
    private void bindModel(RawModel rawModel) {
        GL30.glBindVertexArray(rawModel.getVaoId());
        GL20.glEnableVertexAttribArray(ShaderProgram.ATTR_POSITION);
    }

    /**
     * Prepares an entity to be rendered. The model matrix is created in the usual way and then
     * multiplied with the projection and view matrix (often in the past we've done this in the
     * vertex shader) to create the mvp-matrix. This is then loaded to the vertex shader as a
     * uniform.
     * 
     * @param entity - the entity to be prepared for rendering.
     */
    private void prepareInstance(Entity entity) {
        Matrix4f modelMatrix = new TransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale()).toMatrix();
        shader.loadMvpMatrix(projectionViewMatrix.mul(modelMatrix, new Matrix4f()));
    }

}
