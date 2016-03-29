package net.seabears.game.shadows;

import java.io.IOException;

import org.joml.Matrix4f;

import net.seabears.game.shaders.ShaderProgram;

public class ShadowShader extends ShaderProgram {
    private int locationMvpMatrix;

    public ShadowShader() throws IOException {
        super(SHADER_ROOT + "shadows/");
    }

    @Override
    protected void getAllUniformLocations() {
        locationMvpMatrix = super.getUniformLocation("mvpMatrix");
    }

    public void loadMvpMatrix(Matrix4f mvpMatrix) {
        super.loadMatrix(locationMvpMatrix, mvpMatrix);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(ATTR_POSITION, "in_position");
    }
}
